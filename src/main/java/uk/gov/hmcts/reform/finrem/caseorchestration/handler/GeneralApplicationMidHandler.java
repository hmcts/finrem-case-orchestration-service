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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralApplicationWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralApplicationsCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignCaseAccessService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralApplicationService;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER1;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER2;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER3;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER4;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT;

@Slf4j
@Service
public class GeneralApplicationMidHandler extends FinremCallbackHandler {

    private final GeneralApplicationService service;
    private final AssignCaseAccessService assignCaseAccessService;

    public GeneralApplicationMidHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                        GeneralApplicationService service, AssignCaseAccessService assignCaseAccessService) {
        super(finremCaseDetailsMapper);
        this.service = service;
        this.assignCaseAccessService = assignCaseAccessService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.MID_EVENT.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && (EventType.GENERAL_APPLICATION.equals(eventType));
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();

        log.info("Mid callback event type {} for case id: {}", EventType.GENERAL_APPLICATION, caseDetails.getId());
        FinremCaseData caseData = caseDetails.getData();

        String loggedInUserCaseRole = assignCaseAccessService.getActiveUser(
            caseDetails.getId().toString(), userAuthorisation);
        log.info("Logged in user case role type {}", loggedInUserCaseRole);
        caseData.setCurrentUserCaseRoleType(loggedInUserCaseRole);

        FinremCaseDetails caseDetailsBefore = callbackRequest.getCaseDetailsBefore();
        FinremCaseData caseDataBefore = caseDetailsBefore.getData();
        GeneralApplicationWrapper wrapper = caseData.getGeneralApplicationWrapper();
        GeneralApplicationWrapper wrapperBefore = caseDataBefore.getGeneralApplicationWrapper();

        List<GeneralApplicationsCollection> generalApplicationsBefore;
        List<GeneralApplicationsCollection> generalApplications;
        log.info(loggedInUserCaseRole); //rmv
        switch (loggedInUserCaseRole) {
            case APPLICANT, RESPONDENT -> {
                generalApplications = wrapper.getAppRespGeneralApplications();
                generalApplicationsBefore = wrapperBefore.getAppRespGeneralApplications();
                log.info("Here are GeneralApplications: {}, here are generalApplicationsBefore {}",
                    generalApplications, generalApplicationsBefore);
            }
            case INTERVENER1 -> {
                generalApplications = wrapper.getIntervener1GeneralApplications();
                generalApplicationsBefore = wrapperBefore.getIntervener1GeneralApplications();
            }
            case INTERVENER2 -> {
                generalApplications = wrapper.getIntervener2GeneralApplications();
                generalApplicationsBefore = wrapperBefore.getIntervener2GeneralApplications();
            }
            case INTERVENER3 -> {
                generalApplications = wrapper.getIntervener3GeneralApplications();
                generalApplicationsBefore = wrapperBefore.getIntervener3GeneralApplications();
            }
            case INTERVENER4 -> {
                generalApplications = wrapper.getIntervener4GeneralApplications();
                generalApplicationsBefore = wrapperBefore.getIntervener4GeneralApplications();
            }
            default -> {
                generalApplications = wrapper.getGeneralApplications();
                generalApplicationsBefore = wrapperBefore.getGeneralApplications();
                log.info("default hit");
            }
        }
        List<String> errors = new ArrayList<>();
        service.checkIfApplicationCompleted(caseDetails, errors, generalApplications, generalApplicationsBefore);

        log.info("CAse details {} errors {} gas {} gasbefore {}",
            caseDetails, errors, generalApplications, generalApplicationsBefore);

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(caseData).errors(errors).build();
    }
}
