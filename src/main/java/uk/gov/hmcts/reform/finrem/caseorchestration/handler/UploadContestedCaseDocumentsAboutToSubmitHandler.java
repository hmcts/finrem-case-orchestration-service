package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
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

@Slf4j
@Service
public class UploadContestedCaseDocumentsAboutToSubmitHandler extends FinremCallbackHandler {

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
        FinremCaseData caseData = callbackRequest.getCaseDetails().getData();
        FinremCaseData caseDataBefore = callbackRequest.getCaseDetailsBefore().getData();
        List<UploadCaseDocumentCollection> screenCollections = caseData.getManageCaseDocumentCollection();

        documentHandlers.forEach(documentCollectionService ->
            documentCollectionService.addManagedDocumentToSelectedCollection(callbackRequest, screenCollections));

        screenCollections.sort(Comparator.comparing(
            UploadCaseDocumentCollection::getUploadCaseDocument, Comparator.comparing(
                UploadCaseDocument::getCaseDocumentUploadDateTime, Comparator.nullsLast(
                    Comparator.reverseOrder()))));

        uploadedDocumentHelper.addUploadDateToNewDocuments(caseData, caseDataBefore);

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().data(caseData).build();
    }

    private CaseDocumentParty getActiveUser(Long caseId, String userAuthorisation) {
        String logMessage = "Logged in user role {} caseId {}";
        String activeUserCaseRole = accessService.getActiveUserCaseRole(String.valueOf(caseId), userAuthorisation);
        if (activeUserCaseRole.contains(CaseRole.APP_SOLICITOR.getValue())
            || activeUserCaseRole.contains(CaseRole.APP_BARRISTER.getValue())) {
            log.info(logMessage, APPLICANT, caseId);
            return APPLICANT;
        } else if (activeUserCaseRole.contains(CaseRole.RESP_SOLICITOR.getValue())
            || activeUserCaseRole.contains(CaseRole.RESP_BARRISTER.getValue())) {
            log.info(logMessage, RESPONDENT, caseId);
            return RESPONDENT;
        } else if (activeUserCaseRole.contains(CaseRole.INTVR_SOLICITOR_1.getValue())
            || activeUserCaseRole.contains(CaseRole.INTVR_BARRISTER_1.getValue())) {
            log.info(logMessage, INTERVENER_ONE, caseId);
            return INTERVENER_ONE;
        } else if (activeUserCaseRole.contains(CaseRole.INTVR_SOLICITOR_2.getValue())
            || activeUserCaseRole.contains(CaseRole.INTVR_BARRISTER_2.getValue())) {
            log.info(logMessage, INTERVENER_TWO, caseId);
            return INTERVENER_TWO;
        } else if (activeUserCaseRole.contains(CaseRole.INTVR_SOLICITOR_3.getValue())
            || activeUserCaseRole.contains(CaseRole.INTVR_BARRISTER_3.getValue())) {
            log.info(logMessage, INTERVENER_THREE, caseId);
            return INTERVENER_THREE;
        } else if (activeUserCaseRole.contains(CaseRole.INTVR_SOLICITOR_4.getValue())
            || activeUserCaseRole.contains(CaseRole.INTVR_BARRISTER_4.getValue())) {
            log.info(logMessage, INTERVENER_FOUR, caseId);
            return INTERVENER_FOUR;
        }
        return CASE;
    }
}
