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
        log.info("Start callback event type {} for Case ID: {}", EventType.GENERAL_APPLICATION, caseId);
        FinremCaseData caseData = caseDetails.getData();

        List<GeneralApplicationCollectionData> existingGeneralApplication =
            helper.getGeneralApplicationList(caseData, GENERAL_APPLICATION_COLLECTION);

        log.info("GA AboutToStartHandler beginning existing General Application List size: {} for case ID: {}",
            existingGeneralApplication.size(), caseId);

        GeneralApplicationCollectionData data =
            helper.mapExistingGeneralApplicationToData(caseData, userAuthorisation, caseId);

        String loggedInUserCaseRole = assignCaseAccessService.getActiveUser(caseId, userAuthorisation);
        caseData.setCurrentUserCaseRoleType(loggedInUserCaseRole);

        if (data != null) {
            log.info("Data found on working model for General Application model, "
                + "adding to existing General Applications list");
            existingGeneralApplication.add(data);
        }

        List<DynamicRadioListElement> dynamicListElements = new ArrayList<>();
        helper.buildDynamicIntervenerList(dynamicListElements, caseData);
        DynamicRadioList dynamicList = helper.getDynamicRadioList(dynamicListElements);

        helper.populateGeneralApplicationDataSender(caseData, existingGeneralApplication);

        generalApplicationService.updateGeneralApplicationCollectionData(existingGeneralApplication, caseDetails);

        if (loggedInUserCaseRole.equalsIgnoreCase("Case")) {
            List<GeneralApplicationsCollection> generalApplications = caseData.getGeneralApplicationWrapper()
                .getGeneralApplications();
            if (generalApplications.isEmpty()) {
                GeneralApplicationItems items = GeneralApplicationItems.builder()
                    .generalApplicationSender(dynamicList).build();
                GeneralApplicationsCollection collection = GeneralApplicationsCollection.builder().value(items).build();
                caseData.getGeneralApplicationWrapper().setGeneralApplications(List.of(collection));
            } else {
                generalApplications.forEach(ga -> {
                    String existingCode = ga.getValue().getGeneralApplicationSender().getValue().getCode();
                    String existingLabel = ga.getValue().getGeneralApplicationSender().getValue().getLabel();
                    DynamicRadioListElement newListElement = DynamicRadioListElement.builder()
                        .code(existingCode).label(existingLabel).build();
                    DynamicRadioList existingRadioList = DynamicRadioList.builder().value(newListElement)
                        .listItems(dynamicListElements).build();
                    ga.getValue().setGeneralApplicationSender(existingRadioList);
                });
            }
        }

        log.info("GA AboutToStartHandler end existing General Application List size: {} for case ID: {}",
            existingGeneralApplication.size(), caseId);

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().data(caseData).build();
    }
}
