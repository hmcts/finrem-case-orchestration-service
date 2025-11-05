package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managecasedocuments.ManageCaseDocumentsAction;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerFour;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerOne;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerThree;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerTwo;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.UploadedDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.DocumentHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement.EvidenceManagementDeleteService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.util.Optional.ofNullable;
import static org.apache.commons.collections4.ListUtils.emptyIfNull;
import static uk.gov.hmcts.reform.finrem.caseorchestration.utils.ListUtils.nullIfEmpty;

@Slf4j
@Service
public class NewManageCaseDocumentsContestedAboutToSubmitHandler extends FinremCallbackHandler {
    private static final String CHOOSE_A_DIFFERENT_PARTY = "not present on the case, do you want to continue?";
    private static final String INTERVENER_1 = "Intervener 1 ";
    private static final String INTERVENER_2 = "Intervener 2 ";
    private static final String INTERVENER_3 = "Intervener 3 ";
    private static final String INTERVENER_4 = "Intervener 4 ";

    private static final List<CaseDocumentType> administrativeCaseDocumentTypes = List.of(
        CaseDocumentType.ATTENDANCE_SHEETS,
        CaseDocumentType.JUDICIAL_NOTES,
        CaseDocumentType.JUDGMENT,
        CaseDocumentType.WITNESS_SUMMONS,
        CaseDocumentType.TRANSCRIPT
    );

    private final List<DocumentHandler> documentHandlers;
    private final UploadedDocumentService uploadedDocumentService;
    private final EvidenceManagementDeleteService evidenceManagementDeleteService;
    private final FeatureToggleService featureToggleService;

    @Autowired
    public NewManageCaseDocumentsContestedAboutToSubmitHandler(FinremCaseDetailsMapper mapper,
                                                               List<DocumentHandler> documentHandlers,
                                                               UploadedDocumentService uploadedDocumentService,
                                                               EvidenceManagementDeleteService evidenceManagementDeleteService,
                                                               FeatureToggleService featureToggleService) {
        super(mapper);
        this.documentHandlers = documentHandlers;
        this.uploadedDocumentService = uploadedDocumentService;
        this.evidenceManagementDeleteService = evidenceManagementDeleteService;
        this.featureToggleService = featureToggleService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.NEW_MANAGE_CASE_DOCUMENTS.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        log.info(CallbackHandlerLogger.aboutToSubmit(callbackRequest));

        final List<String> warnings = new ArrayList<>();
        final FinremCaseData caseData = getFinremCaseData(callbackRequest);
        final FinremCaseData caseDataBefore = getFinremCaseDataBefore(callbackRequest);

        calculateWarnings(caseData, warnings);
        moveInputManageCaseDocumentsToManagedCollections(caseData);
        addDefaultsToAdministrativeDocuments(getManagedCollections(caseData));
        replaceManagedDocumentsInCollectionType(caseData);
        addUploadDateToNewDocuments(caseData, caseDataBefore);
        clearLegacyCollections(caseData);
        deleteRemovedDocuments(caseData, caseDataBefore, userAuthorisation);
        clearTemporaryField(caseData);

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().data(caseData).warnings(warnings).build();
    }

    private void addUploadDateToNewDocuments(FinremCaseData caseData, FinremCaseData caseDataBefore) {
        uploadedDocumentService.addUploadDateToNewDocuments(caseData, caseDataBefore);
    }

    private void replaceManagedDocumentsInCollectionType(FinremCaseData caseData) {
        emptyIfNull(documentHandlers).forEach(documentHandler ->
            documentHandler.replaceManagedDocumentsInCollectionType(caseData, getManagedCollections(caseData),
                false));
    }

    private List<UploadCaseDocumentCollection> getManagedCollections(FinremCaseData caseData) {
        return emptyIfNull(caseData.getManageCaseDocumentsWrapper().getManageCaseDocumentCollection());
    }

    private void moveInputManageCaseDocumentsToManagedCollections(FinremCaseData caseData) {
        final ManageCaseDocumentsAction action = caseData.getManageCaseDocumentsWrapper().getManageCaseDocumentsActionSelection();
        if (action == ManageCaseDocumentsAction.ADD_NEW) {
            List<UploadCaseDocumentCollection> newManageCaseDocumentCollection =
                ofNullable(caseData.getManageCaseDocumentsWrapper().getManageCaseDocumentCollection())
                    .orElse(new ArrayList<>());
            newManageCaseDocumentCollection.addAll(
                nullIfEmpty(caseData.getManageCaseDocumentsWrapper().getInputManageCaseDocumentCollection())
            );
            caseData.getManageCaseDocumentsWrapper().setManageCaseDocumentCollection(newManageCaseDocumentCollection);
        }
    }

    private FinremCaseData getFinremCaseData(FinremCallbackRequest callbackRequest) {
        return callbackRequest.getCaseDetails().getData();
    }

    private FinremCaseData getFinremCaseDataBefore(FinremCallbackRequest callbackRequest) {
        return callbackRequest.getCaseDetailsBefore().getData();
    }

    private void clearTemporaryField(FinremCaseData caseData) {
        caseData.getManageCaseDocumentsWrapper().setInputManageCaseDocumentCollection(null);
    }

    private void clearLegacyCollections(FinremCaseData caseData) {
        // clear legacy confidentialDocumentsUploaded
        ofNullable(caseData.getConfidentialDocumentsUploaded()).ifPresent(List::clear);
    }

    private void calculateWarnings(FinremCaseData caseData, List<String> warnings) {
        List<UploadCaseDocumentCollection> manageCaseDocumentCollection = emptyIfNull(
            caseData.getManageCaseDocumentsWrapper().getInputManageCaseDocumentCollection());

        Map<CaseDocumentParty, String> interveners = Map.of(
            CaseDocumentParty.INTERVENER_ONE, INTERVENER_1,
            CaseDocumentParty.INTERVENER_TWO, INTERVENER_2,
            CaseDocumentParty.INTERVENER_THREE, INTERVENER_3,
            CaseDocumentParty.INTERVENER_FOUR, INTERVENER_4
        );

        interveners.forEach((party, namePrefix) -> {
            String intervenerName = switch (party) {
                case INTERVENER_ONE -> ofNullable(caseData.getIntervenerOne())
                    .map(IntervenerOne::getIntervenerName)
                    .orElse(null);
                case INTERVENER_TWO -> ofNullable(caseData.getIntervenerTwo())
                    .map(IntervenerTwo::getIntervenerName)
                    .orElse(null);
                case INTERVENER_THREE -> ofNullable(caseData.getIntervenerThree())
                    .map(IntervenerThree::getIntervenerName)
                    .orElse(null);
                case INTERVENER_FOUR -> ofNullable(caseData.getIntervenerFour())
                    .map(IntervenerFour::getIntervenerName)
                    .orElse(null);
                default -> null;
            };

            if (StringUtils.isBlank(intervenerName)
                && isIntervenerPartySelected(party, manageCaseDocumentCollection)) {
                warnings.add(namePrefix + CHOOSE_A_DIFFERENT_PARTY);
            }
        });
        // sort warnings
        Collections.sort(warnings);
    }

    private boolean isIntervenerPartySelected(CaseDocumentParty caseDocumentParty,
                                              List<UploadCaseDocumentCollection> manageCaseDocumentCollection) {
        return manageCaseDocumentCollection.stream().anyMatch(documentCollection -> {
            if (documentCollection.getUploadCaseDocument().getCaseDocumentParty() != null) {
                return caseDocumentParty.equals(documentCollection.getUploadCaseDocument().getCaseDocumentParty());
            }
            return false;
        });
    }

    private void deleteRemovedDocuments(FinremCaseData caseData,
                                        FinremCaseData caseDataBefore,
                                        String userAuthorisation) {
        if (featureToggleService.isSecureDocEnabled()) {
            List<UploadCaseDocumentCollection> allCollectionsBefore =
                caseDataBefore.getUploadCaseDocumentWrapper().getAllManageableCollections();
            List<UploadCaseDocumentCollection> allCollections =
                caseData.getUploadCaseDocumentWrapper().getAllManageableCollections();
            allCollectionsBefore.removeAll(allCollections);

            allCollectionsBefore.stream().map(this::getDocumentUrl)
                .forEach(docUrl -> evidenceManagementDeleteService.delete(docUrl, userAuthorisation));
        }
    }

    private String getDocumentUrl(UploadCaseDocumentCollection documentCollection) {
        return documentCollection.getUploadCaseDocument().getCaseDocuments().getDocumentUrl();
    }

    private void addDefaultsToAdministrativeDocuments(List<UploadCaseDocumentCollection> managedCollections) {
        managedCollections.forEach(this::setDefaultsForDocumentTypes);
    }

    private void setDefaultsForDocumentTypes(UploadCaseDocumentCollection document) {
        UploadCaseDocument uploadCaseDocument = document.getUploadCaseDocument();
        if (administrativeCaseDocumentTypes.contains(uploadCaseDocument.getCaseDocumentType())) {
            uploadCaseDocument.setCaseDocumentParty(CaseDocumentParty.CASE);
            uploadCaseDocument.setCaseDocumentConfidentiality(YesOrNo.NO);
            uploadCaseDocument.setCaseDocumentFdr(YesOrNo.NO);
        } else if (CaseDocumentType.WITHOUT_PREJUDICE_OFFERS.equals(uploadCaseDocument.getCaseDocumentType())) {
            uploadCaseDocument.setCaseDocumentConfidentiality(YesOrNo.NO);
            uploadCaseDocument.setCaseDocumentFdr(YesOrNo.YES);
        }
    }
}
