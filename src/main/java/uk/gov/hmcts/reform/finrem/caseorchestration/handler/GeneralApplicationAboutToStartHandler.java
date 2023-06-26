package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.GeneralApplicationHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicRadioList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicRadioListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationCollectionData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationItems;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralApplicationsCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignCaseAccessService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralApplicationService;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_COLLECTION;

@Slf4j
@Service
public class GeneralApplicationAboutToStartHandler extends FinremCallbackHandler {

    private final GeneralApplicationHelper helper;
    private final GeneralApplicationService generalApplicationService;
    private final AssignCaseAccessService assignCaseAccessService;

    public GeneralApplicationAboutToStartHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                 GeneralApplicationHelper helper,
                                                 GeneralApplicationService generalApplicationService,
                                                 AssignCaseAccessService assignCaseAccessService) {
        super(finremCaseDetailsMapper);
        this.helper = helper;
        this.generalApplicationService = generalApplicationService;
        this.assignCaseAccessService = assignCaseAccessService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_START.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.GENERAL_APPLICATION.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        String caseId = caseDetails.getId().toString();
        log.info("Start callback event type {} for case id: {}", EventType.GENERAL_APPLICATION, caseId);
        FinremCaseData caseData = caseDetails.getData();

        List<GeneralApplicationCollectionData> existingGeneralApplication =
            helper.getGeneralApplicationList(caseData, GENERAL_APPLICATION_COLLECTION);
        GeneralApplicationCollectionData data =
            helper.migrateExistingGeneralApplication(caseData, userAuthorisation, caseId);

        String loggedInUserCaseRole = assignCaseAccessService.getActiveUser(caseId, userAuthorisation);
        log.info("Logged in user case role type {}", loggedInUserCaseRole);
        caseData.setCurrentUserCaseRoleType(loggedInUserCaseRole);

        if (data != null) {
            existingGeneralApplication.add(data);
        }

        List<DynamicRadioListElement> dynamicListElements = new ArrayList<>();
        helper.buildDynamicIntervenerList(dynamicListElements, caseData);
        DynamicRadioList dynamicList = helper.getDynamicRadioList(dynamicListElements);

        generalApplicationService.updateGeneralApplicationCollectionData(existingGeneralApplication, caseData);

        if (loggedInUserCaseRole.equalsIgnoreCase("Case")) {
            List<GeneralApplicationsCollection> generalApplications = caseData.getGeneralApplicationWrapper()
                .getGeneralApplications();
            if (generalApplications.isEmpty()) {
                GeneralApplicationItems items = GeneralApplicationItems.builder()
                    .generalApplicationReceivedFrom(dynamicList).build();
                GeneralApplicationsCollection collection = GeneralApplicationsCollection.builder().value(items).build();
                caseData.getGeneralApplicationWrapper().setGeneralApplications(List.of(collection));
            } else {
                generalApplications.forEach(x -> {
                    String existingCode = x.getValue().getGeneralApplicationReceivedFrom().getValue().getCode();
                    String existingLabel = x.getValue().getGeneralApplicationReceivedFrom().getValue().getLabel();
                    DynamicRadioListElement newListElement = DynamicRadioListElement.builder()
                        .code(existingCode).label(existingLabel).build();
                    DynamicRadioList existingRadioList = DynamicRadioList.builder().value(newListElement)
                        .listItems(dynamicListElements).build();
                    x.getValue().setGeneralApplicationReceivedFrom(existingRadioList);
                });
            }
        }

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().data(caseData).build();
    }
}
