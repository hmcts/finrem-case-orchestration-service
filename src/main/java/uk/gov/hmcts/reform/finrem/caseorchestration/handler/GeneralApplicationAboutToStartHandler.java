package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.GeneralApplicationHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationCollectionData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationItems;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignCaseAccessService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralApplicationService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CASE_ROLE_FOR_FIELD_SHOW;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER1;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER2;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER3;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER4;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeneralApplicationAboutToStartHandler implements CallbackHandler<Map<String, Object>> {

    private final GeneralApplicationHelper helper;
    private final GeneralApplicationService generalApplicationService;

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_START.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.GENERAL_APPLICATION.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<Map<String, Object>> handle(CallbackRequest callbackRequest,
                                                                                   String userAuthorisation) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        String caseId = caseDetails.getId().toString();
        log.info("Start callback event type {} for case id: {}", EventType.GENERAL_APPLICATION, caseId);
        Map<String, Object> caseData = caseDetails.getData();

        List<GeneralApplicationCollectionData> existingGeneralApplication = helper.getGeneralApplicationList(caseData);
        GeneralApplicationCollectionData data =
            helper.migrateExistingGeneralApplication(caseData, userAuthorisation, caseId);

        String loggedInUserCaseRole = generalApplicationService.getActiveUser(caseId, userAuthorisation);
        log.info("Logged in user case role {}", loggedInUserCaseRole);
        existingGeneralApplication = generalApplicationService.getGeneralApplicationsForUserRole(loggedInUserCaseRole, existingGeneralApplication);
        if (existingGeneralApplication.size() == 0) {
            GeneralApplicationCollectionData.GeneralApplicationCollectionDataBuilder builder =
                GeneralApplicationCollectionData.builder();
            builder.id(UUID.randomUUID().toString());
            GeneralApplicationItems generalApplicationItems = GeneralApplicationItems.builder().build();
            builder.generalApplicationItems(generalApplicationItems);
            existingGeneralApplication.add(builder.build());
        }
        if (data != null) {
            existingGeneralApplication.add(data);
        }
        caseData.put(GENERAL_APPLICATION_COLLECTION, existingGeneralApplication);
        //existingGeneralApplication.stream().forEach(x -> x.getGeneralApplicationItems().setGeneralApplicationUserRole(loggedInUserCaseRole));
        caseData.put(CASE_ROLE_FOR_FIELD_SHOW, loggedInUserCaseRole);
        System.out.println("This is the case role for the current user: " + loggedInUserCaseRole);

        return GenericAboutToStartOrSubmitCallbackResponse.<Map<String, Object>>builder().data(caseData).build();
    }
}
