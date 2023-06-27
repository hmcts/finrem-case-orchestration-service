package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicRadioList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicRadioListElement;
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

        DynamicRadioListElement listElement = DynamicRadioListElement.builder().build();
        switch (loggedInUserCaseRole) {
            case APPLICANT, RESPONDENT -> {
                generalApplications = wrapper.getAppRespGeneralApplications();
                generalApplicationsBefore = wrapperBefore.getAppRespGeneralApplications();
                listElement.setCode(loggedInUserCaseRole);
                listElement.setLabel(loggedInUserCaseRole);
                DynamicRadioList radioList = DynamicRadioList.builder()
                    .value(listElement)
                    .listItems(List.of(listElement))
                    .build();
                if (generalApplications != null && !generalApplications.isEmpty()) {
                    generalApplications.forEach(x -> x.getValue().setGeneralApplicationReceivedFrom(radioList));
                }
            }
            case INTERVENER1 -> {
                generalApplications = wrapper.getIntervener1GeneralApplications();
                generalApplicationsBefore = wrapperBefore.getIntervener1GeneralApplications();
                listElement.setCode(INTERVENER1);
                listElement.setLabel(INTERVENER1);
                DynamicRadioList radioList = DynamicRadioList.builder()
                    .value(listElement)
                    .listItems(List.of(listElement))
                    .build();
                if (generalApplications != null && !generalApplications.isEmpty()) {
                    generalApplications.forEach(x -> x.getValue().setGeneralApplicationReceivedFrom(radioList));
                }
            }
            case INTERVENER2 -> {
                generalApplications = wrapper.getIntervener2GeneralApplications();
                generalApplicationsBefore = wrapperBefore.getIntervener2GeneralApplications();
                listElement.setCode(INTERVENER2);
                listElement.setLabel(INTERVENER2);
                DynamicRadioList radioList = DynamicRadioList.builder()
                    .value(listElement)
                    .listItems(List.of(listElement))
                    .build();
                if (generalApplications != null && !generalApplications.isEmpty()) {
                    generalApplications.forEach(x -> x.getValue().setGeneralApplicationReceivedFrom(radioList));
                }
            }
            case INTERVENER3 -> {
                generalApplications = wrapper.getIntervener3GeneralApplications();
                generalApplicationsBefore = wrapperBefore.getIntervener3GeneralApplications();
                listElement.setCode(INTERVENER3);
                listElement.setLabel(INTERVENER3);
                DynamicRadioList radioList = DynamicRadioList.builder()
                    .value(listElement)
                    .listItems(List.of(listElement))
                    .build();
                if (generalApplications != null && !generalApplications.isEmpty()) {
                    generalApplications.forEach(x -> x.getValue().setGeneralApplicationReceivedFrom(radioList));
                }
            }
            case INTERVENER4 -> {
                generalApplications = wrapper.getIntervener4GeneralApplications();
                generalApplicationsBefore = wrapperBefore.getIntervener4GeneralApplications();
                listElement.setCode(INTERVENER4);
                listElement.setLabel(INTERVENER4);
                DynamicRadioList radioList = DynamicRadioList.builder()
                    .value(listElement)
                    .listItems(List.of(listElement))
                    .build();
                if (generalApplications != null && !generalApplications.isEmpty()) {
                    generalApplications.forEach(x -> x.getValue().setGeneralApplicationReceivedFrom(radioList));
                }
            }
            default -> {
                generalApplications = wrapper.getGeneralApplications();
                generalApplicationsBefore = wrapperBefore.getGeneralApplications();
            }
        }
        List<String> errors = new ArrayList<>();
        service.checkIfApplicationCompleted(caseDetails, errors, generalApplications, generalApplicationsBefore);

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(caseData).errors(errors).build();
    }
}
