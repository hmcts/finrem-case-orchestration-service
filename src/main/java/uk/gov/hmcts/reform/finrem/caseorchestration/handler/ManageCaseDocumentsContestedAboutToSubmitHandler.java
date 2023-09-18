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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.UploadedDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.DocumentHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement.EvidenceManagementDeleteService;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class ManageCaseDocumentsContestedAboutToSubmitHandler extends FinremCallbackHandler {

    public static final String CHOOSE_A_DIFFERENT_PARTY = " not present on the case, please choose a different party";
    public static final String INTERVENER_1 = "Intervener 1 ";
    public static final String INTERVENER_2 = "Intervener 2 ";
    public static final String INTERVENER_3 = "Intervener 3 ";
    public static final String INTERVENER_4 = "Intervener 4 ";
    private final List<DocumentHandler> documentHandlers;
    private final UploadedDocumentService uploadedDocumentHelper;

    private final EvidenceManagementDeleteService evidenceManagementDeleteService;
    private final FeatureToggleService featureToggleService;


    @Autowired
    public ManageCaseDocumentsContestedAboutToSubmitHandler(FinremCaseDetailsMapper mapper,
                                                            List<DocumentHandler> documentHandlers,
                                                            UploadedDocumentService uploadedDocumentHelper,
                                                            EvidenceManagementDeleteService evidenceManagementDeleteService,
                                                            FeatureToggleService featureToggleService) {
        super(mapper);
        this.documentHandlers = documentHandlers;
        this.uploadedDocumentHelper = uploadedDocumentHelper;
        this.evidenceManagementDeleteService = evidenceManagementDeleteService;
        this.featureToggleService = featureToggleService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.MANAGE_CASE_DOCUMENTS.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        FinremCaseData caseData = callbackRequest.getCaseDetails().getData();
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = getValidatedResponse(caseData);
        if (response.hasErrors()) {
            return response;
        }


        FinremCaseData caseDataBefore = callbackRequest.getCaseDetailsBefore().getData();
        List<UploadCaseDocumentCollection> managedCollections = caseData.getManageCaseDocumentCollection();
        documentHandlers.forEach(documentCollectionService ->
            documentCollectionService.replaceManagedDocumentsInCollectionType(callbackRequest, managedCollections));
        uploadedDocumentHelper.addUploadDateToNewDocuments(caseData, caseDataBefore);

        Optional.ofNullable(caseData.getConfidentialDocumentsUploaded()).ifPresent(List::clear);

        if (featureToggleService.isSecureDocEnabled()) {
            deleteRemovedDocuments(caseData, caseDataBefore, userAuthorisation);
        }

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().data(caseData).build();
    }

    private GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> getValidatedResponse(FinremCaseData caseData) {
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response =
            GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().data(caseData).build();
        List<UploadCaseDocumentCollection> manageCaseDocumentCollection = caseData.getManageCaseDocumentCollection();

        if (StringUtils.isBlank(caseData.getIntervenerOneWrapper().getIntervenerName())
            && isIntervernerPartySelected(CaseDocumentParty.INTERVENER_ONE, manageCaseDocumentCollection)) {
            response.getErrors().add(INTERVENER_1 + CHOOSE_A_DIFFERENT_PARTY);
        } else if (StringUtils.isBlank(caseData.getIntervenerTwoWrapper().getIntervenerName())
            && isIntervernerPartySelected(CaseDocumentParty.INTERVENER_TWO, manageCaseDocumentCollection)) {
            response.getErrors().add(INTERVENER_2 + CHOOSE_A_DIFFERENT_PARTY);
        } else if (StringUtils.isBlank(caseData.getIntervenerThreeWrapper().getIntervenerName())
            && isIntervernerPartySelected(CaseDocumentParty.INTERVENER_THREE, manageCaseDocumentCollection)) {
            response.getErrors().add(INTERVENER_3 + CHOOSE_A_DIFFERENT_PARTY);
        } else if (StringUtils.isBlank(caseData.getIntervenerFourWrapper().getIntervenerName())
            && isIntervernerPartySelected(CaseDocumentParty.INTERVENER_FOUR, manageCaseDocumentCollection)) {
            response.getErrors().add(INTERVENER_4 + CHOOSE_A_DIFFERENT_PARTY);
        }
        return response;
    }

    private boolean isIntervernerPartySelected(CaseDocumentParty caseDocumentParty,
                                               List<UploadCaseDocumentCollection> manageCaseDocumentCollection) {
        return manageCaseDocumentCollection.stream().anyMatch(documentCollection ->
            caseDocumentParty.equals(documentCollection.getUploadCaseDocument().getCaseDocumentParty()));
    }

    private void deleteRemovedDocuments(FinremCaseData caseData,
                                        FinremCaseData caseDataBefore,
                                        String userAuthorisation) {
        List<UploadCaseDocumentCollection> allCollectionsBefore =
            caseDataBefore.getUploadCaseDocumentWrapper().getAllManageableCollections();
        allCollectionsBefore.removeAll(caseData.getUploadCaseDocumentWrapper().getAllManageableCollections());

        allCollectionsBefore.stream().map(this::getDocumentUrl)
            .forEach(docUrl -> evidenceManagementDeleteService.delete(docUrl, userAuthorisation));
    }

    private String getDocumentUrl(UploadCaseDocumentCollection documentCollection) {
        return documentCollection.getUploadCaseDocument().getCaseDocuments().getDocumentUrl();
    }
}