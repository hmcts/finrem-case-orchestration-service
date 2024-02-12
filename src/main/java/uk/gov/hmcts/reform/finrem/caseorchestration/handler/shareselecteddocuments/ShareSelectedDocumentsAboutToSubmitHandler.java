package uk.gov.hmcts.reform.finrem.caseorchestration.handler.shareselecteddocuments;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignCaseAccessService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IntervenerShareDocumentsService;

import java.util.List;

@Slf4j
@Service
public class ShareSelectedDocumentsAboutToSubmitHandler extends FinremCallbackHandler {

    private final IntervenerShareDocumentsService intervenerShareDocumentsService;
    private final AssignCaseAccessService assignCaseAccessService;


    public ShareSelectedDocumentsAboutToSubmitHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                      IntervenerShareDocumentsService intervenerShareDocumentsService,
                                                      AssignCaseAccessService assignCaseAccessService) {
        super(finremCaseDetailsMapper);
        this.intervenerShareDocumentsService = intervenerShareDocumentsService;
        this.assignCaseAccessService = assignCaseAccessService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && (EventType.SHARE_SELECTED_DOCUMENTS.equals(eventType));
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        Long caseId = caseDetails.getId();
        log.info("Invoking contested {} about to submit callback for Case ID: {}",
            callbackRequest.getEventType(), caseId);

        FinremCaseData caseData = caseDetails.getData();

        String activeUser = assignCaseAccessService.getActiveUserCaseRole(caseId.toString(), userAuthorisation);
        caseData.setCurrentUserCaseRoleType(activeUser);
        List<String> warnings = intervenerShareDocumentsService.checkThatApplicantAndRespondentAreBothSelected(caseData);

        intervenerShareDocumentsService.shareSelectedDocumentWithOtherSelectedSolicitors(caseData);

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(caseData).warnings(warnings).build();
    }


}