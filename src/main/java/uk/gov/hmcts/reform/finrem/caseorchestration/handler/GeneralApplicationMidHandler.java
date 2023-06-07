package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationCollectionData;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralApplicationService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_COLLECTION;

@Slf4j
@Service
public class GeneralApplicationMidHandler extends FinremCallbackHandler {

    private final GeneralApplicationHelper helper;
    private final GeneralApplicationService service;
    private final ObjectMapper objectMapper;

    public GeneralApplicationMidHandler(FinremCaseDetailsMapper finremCaseDetailsMapper, GeneralApplicationHelper helper,
                                        GeneralApplicationService service, ObjectMapper objectMapper) {
        super(finremCaseDetailsMapper);
        this.helper = helper;
        this.service = service;
        this.objectMapper = objectMapper;
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

        List<GeneralApplicationsCollection> generalApplications = caseData.getGeneralApplicationWrapper().getGeneralApplications();
        if (generalApplications == null || generalApplications.isEmpty()) {
            log.info("Please complete the general application for case Id {}", caseDetails.getId());
            errors.add("Please complete the General Application. No information has been entered for this application.");
        }

        FinremCaseDetails caseDetailsBefore = callbackRequest.getCaseDetailsBefore();
        FinremCaseData caseDataBefore = caseDetailsBefore.getData();

        String loggedInUserCaseRole = service.getActiveUser(caseDetails.getId().toString(), userAuthorisation);
        log.info("Logged in user case role {}", loggedInUserCaseRole);

        List<GeneralApplicationsCollection> generalApplicationsBefore = caseDataBefore.getGeneralApplicationWrapper().getGeneralApplications();

        List<GeneralApplicationCollectionData> applicationCollectionDataListBefore =
            objectMapper.convertValue(generalApplicationsBefore, new TypeReference<>() {
            });

        List<GeneralApplicationCollectionData> applicationCollectionDataList =
            objectMapper.convertValue(generalApplications, new TypeReference<>() {
            });

        List<GeneralApplicationCollectionData> generalApplicationDataBefore = service.getGeneralApplicationsForUserRole(loggedInUserCaseRole,
            applicationCollectionDataList);
        List<GeneralApplicationCollectionData> generalApplicationData = service.getGeneralApplicationsForUserRole(loggedInUserCaseRole,
            applicationCollectionDataListBefore);

        if (generalApplicationDataBefore != null && generalApplicationData != null && (generalApplicationDataBefore.size() == generalApplicationData.size())) {
            log.info("Please complete the general application for case Id {}", caseDetails.getId());
            errors.add("Any changes to an existing General Applications will not be saved. "
                + "Please add a new General Application in order to progress.");
        }

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(caseData).errors(errors).build();
    }
}
