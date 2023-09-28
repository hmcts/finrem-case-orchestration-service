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
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IntervenerShareDocumentsService;

import java.util.List;

@Slf4j
@Service
public class ShareSelectedDocumentsAboutToSubmitHandler extends FinremCallbackHandler {

    private final IntervenerShareDocumentsService intervenerShareDocumentsService;


    public ShareSelectedDocumentsAboutToSubmitHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                      IntervenerShareDocumentsService intervenerShareDocumentsService) {
        super(finremCaseDetailsMapper);
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

        FinremCaseData caseData = caseDetails.getData();

        List<String> warnings = intervenerShareDocumentsService.checkThatApplicantAndRespondentAreBothSelected(caseData);

        intervenerShareDocumentsService.shareSelectedDocumentWithOtherSelectedSolicitors(caseData);

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(caseData).warnings(warnings).build();
    }


}
