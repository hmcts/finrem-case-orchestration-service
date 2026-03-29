package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
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
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.Optional.ofNullable;
import static org.apache.commons.collections4.ListUtils.emptyIfNull;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static uk.gov.hmcts.reform.finrem.caseorchestration.utils.ListUtils.nullIfEmpty;

/**
 * Handles the "about to submit" callback for managing case documents
 * in contested financial remedy cases.
 *
 * <p>This handler contains logic migrated from
 * {@link ManageCaseDocumentsContestedAboutToSubmitHandler}, which is
 * scheduled for removal. It preserves the existing behaviour while
 * aligning with the updated handler structure.
 *
 * <p>Responsible for processing document-related updates prior to
 * submission, ensuring case data is correctly prepared and validated.
 *
 * <p><strong>Note:</strong> This class supersedes the legacy handler and
 * should be used for all contested case document management flows.
 */
@Slf4j
@Service
public class NewManageCaseDocumentsContestedAboutToSubmitHandler extends FinremAboutToSubmitCallbackHandler {
    private static final String PARTY_NOT_PRESENT_ERROR_MESSAGE = "%s not present on the case, do you want to continue?";
    private static final String INTERVENER_1 = "Intervener 1";
    private static final String INTERVENER_2 = "Intervener 2";
    private static final String INTERVENER_3 = "Intervener 3";
    private static final String INTERVENER_4 = "Intervener 4";

    private static final List<CaseDocumentType> ADMINISTRATIVE_CASE_DOCUMENT_TYPES = List.of(
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

        final FinremCaseData caseData = callbackRequest.getFinremCaseData();
        final FinremCaseData caseDataBefore = callbackRequest.getFinremCaseDataBefore();

        final List<String> warnings = new ArrayList<>(buildSelectedPartyNotPresentWarnings(caseData));
        moveInputManageCaseDocumentsToManagedCollections(caseData);
        addDefaultsToAdministrativeDocuments(getInputCollections(caseData));
        replaceManagedDocumentsInCollectionType(caseData);
        addUploadDateToNewDocuments(caseData, caseDataBefore);
        clearLegacyCollections(caseData);
        deleteRemovedDocuments(caseData, caseDataBefore, userAuthorisation);

        return response(caseData, warnings, null);
    }

    private void addUploadDateToNewDocuments(FinremCaseData caseData, FinremCaseData caseDataBefore) {
        uploadedDocumentService.addUploadDateToNewDocuments(caseData, caseDataBefore);
    }

    private void replaceManagedDocumentsInCollectionType(FinremCaseData caseData) {
        final ManageCaseDocumentsAction action = caseData.getManageCaseDocumentsWrapper().getManageCaseDocumentsActionSelection();

        if (ManageCaseDocumentsAction.AMEND.equals(action)) {
            List<UploadCaseDocumentCollection> inputDocuments =
                caseData.getManageCaseDocumentsWrapper().getManageCaseDocumentCollection();

            emptyIfNull(documentHandlers).forEach(documentHandler ->
                documentHandler.replaceManagedDocumentsInCollectionType(caseData, inputDocuments, true));

        } else if (ManageCaseDocumentsAction.ADD_NEW.equals(action)) {
            List<UploadCaseDocumentCollection> inputDocuments =
                caseData.getManageCaseDocumentsWrapper().getManageCaseDocumentCollection();

            emptyIfNull(documentHandlers).forEach(documentHandler ->
                documentHandler.replaceManagedDocumentsInCollectionType(caseData, inputDocuments, false));
        }
    }

    private List<UploadCaseDocumentCollection> getInputCollections(FinremCaseData caseData) {
        return emptyIfNull(caseData.getManageCaseDocumentsWrapper().getInputManageCaseDocumentCollection());
    }

    private void moveInputManageCaseDocumentsToManagedCollections(FinremCaseData caseData) {
        final ManageCaseDocumentsAction action = caseData.getManageCaseDocumentsWrapper().getManageCaseDocumentsActionSelection();
        if (ManageCaseDocumentsAction.ADD_NEW.equals(action)) {
            List<UploadCaseDocumentCollection> newManageCaseDocumentCollection =
                ofNullable(caseData.getManageCaseDocumentsWrapper().getManageCaseDocumentCollection())
                    .orElse(new ArrayList<>());
            newManageCaseDocumentCollection.addAll(
                nullIfEmpty(caseData.getManageCaseDocumentsWrapper().getInputManageCaseDocumentCollection())
            );
            caseData.getManageCaseDocumentsWrapper().setManageCaseDocumentCollection(newManageCaseDocumentCollection);
        } else if (ManageCaseDocumentsAction.AMEND.equals(action)) {
            List<UploadCaseDocumentCollection> amendedCollection =
                ofNullable(caseData.getManageCaseDocumentsWrapper().getInputManageCaseDocumentCollection())
                    .orElse(new ArrayList<>());
            caseData.getManageCaseDocumentsWrapper().setManageCaseDocumentCollection(new ArrayList<>(amendedCollection));
        }
    }

    private void clearLegacyCollections(FinremCaseData caseData) {
        // clear legacy confidentialDocumentsUploaded
        ofNullable(caseData.getConfidentialDocumentsUploaded()).ifPresent(List::clear);
    }

    private List<String> buildSelectedPartyNotPresentWarnings(FinremCaseData caseData) {
        List<String> ret = new ArrayList<>();
        List<UploadCaseDocumentCollection> manageCaseDocumentCollection = emptyIfNull(
            caseData.getManageCaseDocumentsWrapper().getInputManageCaseDocumentCollection());

        Map.of(
            CaseDocumentParty.INTERVENER_ONE, INTERVENER_1,
            CaseDocumentParty.INTERVENER_TWO, INTERVENER_2,
            CaseDocumentParty.INTERVENER_THREE, INTERVENER_3,
            CaseDocumentParty.INTERVENER_FOUR, INTERVENER_4
        ).forEach((intervenerParty, namePrefix) -> {
            String intervenerName = switch (intervenerParty) {
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
                default -> throw new IllegalStateException("Unexpected value: " + intervenerParty);
            };

            if (isBlank(intervenerName) && anySelectedDocumentsMatchIntervenerParty(intervenerParty, manageCaseDocumentCollection)) {
                ret.add(PARTY_NOT_PRESENT_ERROR_MESSAGE.formatted(namePrefix));
            }
        });
        return ret;
    }

    private boolean anySelectedDocumentsMatchIntervenerParty(CaseDocumentParty caseDocumentParty,
                                                             List<UploadCaseDocumentCollection> documents) {
        return documents.stream()
            .map(UploadCaseDocumentCollection::getUploadCaseDocument)
            .filter(Objects::nonNull)
            .anyMatch(doc -> caseDocumentParty.equals(doc.getCaseDocumentParty()));
    }

    private void deleteRemovedDocuments(FinremCaseData caseData,
                                        FinremCaseData caseDataBefore,
                                        String userAuthorisation) {
        if (featureToggleService.isManageCaseDocsDeleteEnabled()) {
            List<UploadCaseDocumentCollection> allCollectionsBefore =
                caseDataBefore.getUploadCaseDocumentWrapper().getAllManageableCollections();
            allCollectionsBefore.removeAll(caseData.getUploadCaseDocumentWrapper().getAllManageableCollections());
            allCollectionsBefore.stream().map(this::getDocumentUrl)
                .forEach(docUrl -> evidenceManagementDeleteService.delete(docUrl, userAuthorisation));
        }
    }

    private String getDocumentUrl(UploadCaseDocumentCollection documentCollection) {
        return ofNullable(documentCollection)
            .map(UploadCaseDocumentCollection::getUploadCaseDocument)
            .map(UploadCaseDocument::getCaseDocuments)
            .map(CaseDocument::getDocumentUrl)
            .orElseThrow(() -> new IllegalStateException("Document URL is missing"));
    }

    private void addDefaultsToAdministrativeDocuments(List<UploadCaseDocumentCollection> managedCollections) {
        managedCollections.forEach(this::setDefaultsForDocumentTypes);
    }

    private void setDefaultsForDocumentTypes(UploadCaseDocumentCollection document) {
        UploadCaseDocument uploadCaseDocument = document.getUploadCaseDocument();
        if (ADMINISTRATIVE_CASE_DOCUMENT_TYPES.contains(uploadCaseDocument.getCaseDocumentType())) {
            uploadCaseDocument.setCaseDocumentParty(CaseDocumentParty.CASE);
            uploadCaseDocument.setCaseDocumentFdr(YesOrNo.NO);
        } else if (CaseDocumentType.WITHOUT_PREJUDICE_OFFERS.equals(uploadCaseDocument.getCaseDocumentType())) {
            uploadCaseDocument.setCaseDocumentConfidentiality(YesOrNo.NO);
            uploadCaseDocument.setCaseDocumentFdr(YesOrNo.YES);
        }
    }
}
