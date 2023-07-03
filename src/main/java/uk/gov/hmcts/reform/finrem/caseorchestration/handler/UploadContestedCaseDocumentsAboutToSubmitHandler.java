package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignCaseAccessService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.UploadedDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.DocumentHandler;

import java.util.Comparator;
import java.util.List;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty.CASE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty.INTERVENER_FOUR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty.INTERVENER_ONE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty.INTERVENER_THREE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty.INTERVENER_TWO;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty.RESPONDENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType.TRIAL_BUNDLE;

@Slf4j
@Service
public class UploadContestedCaseDocumentsAboutToSubmitHandler extends FinremCallbackHandler {

    public static final String TRIAL_BUNDLE_SELECTED_ERROR =
        "To upload a hearing bundle please use the Manage hearing "
            + "bundles event which can be found on the drop-down list on the home page";
    public static final String NO_DOCUMENT_ERROR = "In order to proceed at least one document must be added";
    private final List<DocumentHandler> documentHandlers;
    private final UploadedDocumentService uploadedDocumentHelper;

    private final AssignCaseAccessService accessService;

    public UploadContestedCaseDocumentsAboutToSubmitHandler(FinremCaseDetailsMapper mapper,
                                                            List<DocumentHandler> documentHandlers,
                                                            UploadedDocumentService uploadedDocumentHelper,
                                                            AssignCaseAccessService accessService) {
        super(mapper);
        this.documentHandlers = documentHandlers;
        this.uploadedDocumentHelper = uploadedDocumentHelper;
        this.accessService = accessService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.UPLOAD_CASE_FILES.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        FinremCaseData caseData = caseDetails.getData();

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = getValidatedResponse(caseData);
        if (response.hasErrors()) {
            return response;
        }

        List<UploadCaseDocumentCollection> managedCollections = caseData.getManageCaseDocumentCollection();

        CaseDocumentParty loggedInParty = getActiveUser(caseDetails.getId(), userAuthorisation);

        managedCollections.forEach(doc -> doc.getUploadCaseDocument().setCaseDocumentParty(loggedInParty));

        documentHandlers.forEach(documentCollectionService ->
            documentCollectionService.addManagedDocumentToSelectedCollection(callbackRequest, managedCollections));

        managedCollections.sort(Comparator.comparing(
            UploadCaseDocumentCollection::getUploadCaseDocument, Comparator.comparing(
                UploadCaseDocument::getCaseDocumentUploadDateTime, Comparator.nullsLast(
                    Comparator.reverseOrder()))));

        FinremCaseData caseDataBefore = callbackRequest.getCaseDetailsBefore().getData();
        uploadedDocumentHelper.addUploadDateToNewDocuments(caseData, caseDataBefore);

        return response;
    }

    private GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> getValidatedResponse(FinremCaseData caseData) {
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response =
            GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().data(caseData).build();
        if (!isAnyDocumentPresent(caseData)) {
            response.getErrors().add(NO_DOCUMENT_ERROR);
        }
        if (isAnyTrialBundleDocumentPresent(caseData)) {
            response.getErrors().add(TRIAL_BUNDLE_SELECTED_ERROR);
        }
        return response;
    }

    private boolean isAnyDocumentPresent(FinremCaseData caseData) {
        return CollectionUtils.isNotEmpty(caseData.getManageCaseDocumentCollection());
    }

    private boolean isAnyTrialBundleDocumentPresent(FinremCaseData caseData) {
        return caseData.getManageCaseDocumentCollection().stream()
            .map(uploadCaseDocumentCollection ->
                uploadCaseDocumentCollection.getUploadCaseDocument().getCaseDocumentType())
            .anyMatch(caseDocumentType -> caseDocumentType.equals(TRIAL_BUNDLE));
    }

    private CaseDocumentParty getActiveUser(Long caseId, String userAuthorisation) {
        String logMessage = "Logged in user role {} caseId {}";
        String activeUserCaseRole = accessService.getActiveUserCaseRole(String.valueOf(caseId), userAuthorisation);
        if (activeUserCaseRole.contains(CaseRole.APP_SOLICITOR.getCcdCode())
            || activeUserCaseRole.contains(CaseRole.APP_BARRISTER.getCcdCode())) {
            log.info(logMessage, APPLICANT, caseId);
            return APPLICANT;
        } else if (activeUserCaseRole.contains(CaseRole.RESP_SOLICITOR.getCcdCode())
            || activeUserCaseRole.contains(CaseRole.RESP_BARRISTER.getCcdCode())) {
            log.info(logMessage, RESPONDENT, caseId);
            return RESPONDENT;
        } else if (activeUserCaseRole.contains(CaseRole.INTVR_SOLICITOR_1.getCcdCode())
            || activeUserCaseRole.contains(CaseRole.INTVR_BARRISTER_1.getCcdCode())) {
            log.info(logMessage, INTERVENER_ONE, caseId);
            return INTERVENER_ONE;
        } else if (activeUserCaseRole.contains(CaseRole.INTVR_SOLICITOR_2.getCcdCode())
            || activeUserCaseRole.contains(CaseRole.INTVR_BARRISTER_2.getCcdCode())) {
            log.info(logMessage, INTERVENER_TWO, caseId);
            return INTERVENER_TWO;
        } else if (activeUserCaseRole.contains(CaseRole.INTVR_SOLICITOR_3.getCcdCode())
            || activeUserCaseRole.contains(CaseRole.INTVR_BARRISTER_3.getCcdCode())) {
            log.info(logMessage, INTERVENER_THREE, caseId);
            return INTERVENER_THREE;
        } else if (activeUserCaseRole.contains(CaseRole.INTVR_SOLICITOR_4.getCcdCode())
            || activeUserCaseRole.contains(CaseRole.INTVR_BARRISTER_4.getCcdCode())) {
            log.info(logMessage, INTERVENER_FOUR, caseId);
            return INTERVENER_FOUR;
        }
        return CASE;
    }
}
