package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Schedule1OrMatrimonialAndCpList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ScheduleOneWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.OnlineFormDocumentService;

@Slf4j
@Service
public class IssueApplicationContestedAboutToSubmitHandler extends FinremCallbackHandler {

    private final OnlineFormDocumentService service;

    public IssueApplicationContestedAboutToSubmitHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                         OnlineFormDocumentService service) {
        super(finremCaseDetailsMapper);
        this.service = service;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && (EventType.ISSUE_APPLICATION.equals(eventType));
    }


    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {

        Long caseId = callbackRequest.getCaseDetails().getId();
        log.info("Invoking contested {} about to submit callback for case id: {}",
            callbackRequest.getEventType(), caseId);


        FinremCaseData caseData = callbackRequest.getCaseDetails().getData();

        CaseDocument document = service.generateContestedMiniForm(userAuthorisation, callbackRequest.getCaseDetails());
        log.info("Issue application generated document {} for caseId {}", document, caseId);
        caseData.setMiniFormA(document);

        if (ObjectUtils.isEmpty(caseData.getDivorceCaseNumber())) {
            caseData.setDivorceCaseNumber(caseId.toString());
        }
        if (ObjectUtils.isEmpty(caseData.getScheduleOneWrapper().getTypeOfApplication())) {
            caseData.setScheduleOneWrapper(ScheduleOneWrapper.builder()
                .typeOfApplication(Schedule1OrMatrimonialAndCpList.MATRIMONIAL_AND_CIVIL_PARTNERSHIP_PROCEEDINGS).build());
        }

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(callbackRequest.getCaseDetails().getData()).build();
    }
}