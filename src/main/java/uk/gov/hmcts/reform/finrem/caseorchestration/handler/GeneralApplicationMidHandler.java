package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.GeneralApplicationHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralApplicationsCollection;
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

    private final GeneralApplicationHelper helper;
    private final GeneralApplicationService service;

    public GeneralApplicationMidHandler(FinremCaseDetailsMapper finremCaseDetailsMapper, GeneralApplicationHelper helper,
                                        GeneralApplicationService service) {
        super(finremCaseDetailsMapper);
        this.helper = helper;
        this.service = service;
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
        List<String> errors = new ArrayList<>();

        String loggedInUserCaseRole = service.getActiveUser(caseDetails.getId().toString(), userAuthorisation);
        caseData.setCurrentUserCaseRoleLabel(loggedInUserCaseRole);
        log.info("Logged in user case role {}", loggedInUserCaseRole);

        List<GeneralApplicationsCollection> generalApplications =
            caseData.getGeneralApplicationWrapper().getGeneralApplications();
        FinremCaseDetails caseDetailsBefore = callbackRequest.getCaseDetailsBefore();
        FinremCaseData caseDataBefore = caseDetailsBefore.getData();
        List<GeneralApplicationsCollection> generalApplicationsBefore =
            caseDataBefore.getGeneralApplicationWrapper().getGeneralApplications();
        List<GeneralApplicationsCollection> intervener1GeneralApplications =
            caseData.getGeneralApplicationWrapper().getIntervener1GeneralApplications();
        List<GeneralApplicationsCollection> intervener1GeneralApplicationsBefore =
            caseDataBefore.getGeneralApplicationWrapper().getIntervener1GeneralApplications();
        List<GeneralApplicationsCollection> intervener2GeneralApplications =
            caseData.getGeneralApplicationWrapper().getIntervener2GeneralApplications();
        List<GeneralApplicationsCollection> intervener2GeneralApplicationsBefore =
            caseDataBefore.getGeneralApplicationWrapper().getIntervener2GeneralApplications();
        List<GeneralApplicationsCollection> intervener3GeneralApplications =
            caseData.getGeneralApplicationWrapper().getIntervener3GeneralApplications();
        List<GeneralApplicationsCollection> intervener3GeneralApplicationsBefore =
            caseDataBefore.getGeneralApplicationWrapper().getIntervener3GeneralApplications();
        List<GeneralApplicationsCollection> intervener4GeneralApplications =
            caseData.getGeneralApplicationWrapper().getIntervener4GeneralApplications();
        List<GeneralApplicationsCollection> intervener4GeneralApplicationsBefore =
            caseDataBefore.getGeneralApplicationWrapper().getIntervener4GeneralApplications();
        List<GeneralApplicationsCollection> appRespGeneralApplications =
            caseData.getGeneralApplicationWrapper().getAppRespGeneralApplications();
        List<GeneralApplicationsCollection> appRespGeneralApplicationsBefore =
            caseDataBefore.getGeneralApplicationWrapper().getAppRespGeneralApplications();

        switch (loggedInUserCaseRole) {
            case INTERVENER1 -> {
                if (intervener1GeneralApplicationsBefore != null && intervener1GeneralApplications != null
                    && (intervener1GeneralApplicationsBefore.size() == intervener1GeneralApplications.size())) {
                    log.info("Please complete the general application for case Id {}", caseDetails.getId());
                    errors.add("Any changes to an existing General Applications will not be saved. "
                        + "Please add a new General Application in order to progress.");
                }
                if ((intervener1GeneralApplications == null || intervener1GeneralApplications.isEmpty())) {
                    log.info("Please complete the general application for case Id {}", caseDetails.getId());
                    errors.add("Please complete the General Application. No information has been entered for this application.");
                }
            }
            case INTERVENER2 -> {
                if (intervener2GeneralApplicationsBefore != null && intervener2GeneralApplications != null
                    && (intervener2GeneralApplicationsBefore.size() == intervener2GeneralApplications.size())) {
                    log.info("Please complete the general application for case Id {}", caseDetails.getId());
                    errors.add("Any changes to an existing General Applications will not be saved. "
                        + "Please add a new General Application in order to progress.");
                }
                if ((intervener2GeneralApplications == null || intervener2GeneralApplications.isEmpty())) {
                    log.info("Please complete the general application for case Id {}", caseDetails.getId());
                    errors.add("Please complete the General Application. No information has been entered for this application.");
                }
            }
            case INTERVENER3 -> {
                if (intervener3GeneralApplicationsBefore != null && intervener3GeneralApplications != null
                    && (intervener3GeneralApplicationsBefore.size() == intervener3GeneralApplications.size())) {
                    log.info("Please complete the general application for case Id {}", caseDetails.getId());
                    errors.add("Any changes to an existing General Applications will not be saved. "
                        + "Please add a new General Application in order to progress.");
                }
                if ((intervener3GeneralApplications == null || intervener3GeneralApplications.isEmpty())) {
                    log.info("Please complete the general application for case Id {}", caseDetails.getId());
                    errors.add("Please complete the General Application. No information has been entered for this application.");
                }
            }
            case INTERVENER4 -> {
                if (intervener4GeneralApplicationsBefore != null && intervener4GeneralApplications
                    != null && (intervener4GeneralApplicationsBefore.size() == intervener4GeneralApplications.size())) {
                    log.info("Please complete the general application for case Id {}", caseDetails.getId());
                    errors.add("Any changes to an existing General Applications will not be saved. "
                        + "Please add a new General Application in order to progress.");
                }
                if ((intervener4GeneralApplications == null || intervener4GeneralApplications.isEmpty())) {
                    log.info("Please complete the general application for case Id {}", caseDetails.getId());
                    errors.add("Please complete the General Application. No information has been entered for this application.");
                }
            }
            case APPLICANT, RESPONDENT -> {
                if (appRespGeneralApplicationsBefore != null && appRespGeneralApplications
                    != null && (appRespGeneralApplicationsBefore.size() == appRespGeneralApplications.size())) {
                    log.info("Please complete the general application for case Id {}", caseDetails.getId());
                    errors.add("Any changes to an existing General Applications will not be saved. "
                        + "Please add a new General Application in order to progress.");
                }
                if ((appRespGeneralApplications == null || appRespGeneralApplications.isEmpty())) {
                    log.info("Please complete the general application for case Id {}", caseDetails.getId());
                    errors.add("Please complete the General Application. No information has been entered for this application.");
                }
            }
            default -> {
                if (generalApplicationsBefore != null && generalApplications
                    != null && (generalApplicationsBefore.size() == generalApplications.size())) {
                    log.info("Please complete the general application for case Id {}", caseDetails.getId());
                    errors.add("Any changes to an existing General Applications will not be saved. "
                        + "Please add a new General Application in order to progress.");
                }
                if ((generalApplications == null || generalApplications.isEmpty())) {
                    log.info("Please complete the general application for case Id {}", caseDetails.getId());
                    errors.add("Please complete the General Application. No information has been entered for this application.");
                }
            }
        }

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(caseData).errors(errors).build();
    }
}
