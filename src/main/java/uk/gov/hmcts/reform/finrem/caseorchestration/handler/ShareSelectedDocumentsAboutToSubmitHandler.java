package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
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
    private final AssignCaseAccessService accessService;
    private final IntervenerShareDocumentsService intervenerShareDocumentsService;

    public ShareSelectedDocumentsAboutToSubmitHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                      AssignCaseAccessService accessService,
                                                      IntervenerShareDocumentsService intervenerShareDocumentsService) {
        super(finremCaseDetailsMapper);
        this.accessService = accessService;
        this.intervenerShareDocumentsService = intervenerShareDocumentsService;
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
        log.info("Invoking contested {} about to submit callback for case id: {}",
            callbackRequest.getEventType(), caseId);

        String loggedInUserCaseRole = accessService.getLoggedInUserCaseRole(String.valueOf(caseId), userAuthorisation);
        FinremCaseData caseData = caseDetails.getData();
        if (loggedInUserCaseRole == null) {
            return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().data(caseData)
                .errors(List.of("Logged in user do not have sufficient role to execute this event")).build();
        }

        log.info("loggedInUserCaseRole {} for case {} ", loggedInUserCaseRole, caseId);
        intervenerShareDocumentsService.shareSelectedDocumentWithOtherSelectedSolicitors(caseData);

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(caseData).build();
    }


}