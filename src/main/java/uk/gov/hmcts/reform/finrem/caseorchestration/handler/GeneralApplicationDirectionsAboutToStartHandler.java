package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.GeneralApplicationHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationCollectionData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationItems;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignCaseAccessService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralApplicationDirectionsService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
public class GeneralApplicationDirectionsAboutToStartHandler extends FinremCallbackHandler implements GeneralApplicationHandler {

    private final AssignCaseAccessService assignCaseAccessService;
    private final GeneralApplicationHelper helper;
    private final GeneralApplicationDirectionsService service;
    private final FinremCaseDetailsMapper finremCaseDetailsMapper;

    public GeneralApplicationDirectionsAboutToStartHandler(AssignCaseAccessService assignCaseAccessService,
                                                           FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                           GeneralApplicationHelper helper,
                                                           GeneralApplicationDirectionsService service) {
        super(finremCaseDetailsMapper);
        this.helper = helper;
        this.service = service;
        this.assignCaseAccessService = assignCaseAccessService;
        this.finremCaseDetailsMapper = finremCaseDetailsMapper;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_START.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.GENERAL_APPLICATION_DIRECTIONS.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        FinremCaseDetails finremCaseDetails = callbackRequest.getCaseDetails();

        String caseId = String.valueOf(finremCaseDetails.getId());
        log.info("About to Start callback event type {} for case id: {}", EventType.GENERAL_APPLICATION_DIRECTIONS, caseId);

        FinremCaseData caseData = finremCaseDetails.getData();

        String loggedInUserCaseRole = assignCaseAccessService.getActiveUser(caseId, userAuthorisation);
        log.info("Logged in user case role type {} on case {}", loggedInUserCaseRole, caseId);
        caseData.setCurrentUserCaseRoleType(loggedInUserCaseRole);
        CaseDetails caseDetails = finremCaseDetailsMapper.mapToCaseDetails(callbackRequest.getCaseDetails());

        service.startGeneralApplicationDirections(caseDetails);

        helper.populateGeneralApplicationSender(caseData, caseData.getGeneralApplicationWrapper().getGeneralApplications());

        List<GeneralApplicationCollectionData> outcomeList = helper.getOutcomeList(caseData);
        AtomicInteger index = new AtomicInteger(0);
        if (outcomeList.isEmpty() && caseData.getGeneralApplicationWrapper().getGeneralApplicationCreatedBy() != null) {
            log.info("setting direction list if existing ga not moved to collection for Case ID: {}", caseId);
            setDirectionListForNonCollectionGeneralApplication(caseData, index, userAuthorisation, caseId);
        } else {
            if (outcomeList.isEmpty()) {
                return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().data(caseData)
                    .errors(List.of("There are no general application available for issue direction.")).build();
            }
            List<DynamicListElement> dynamicListElements = outcomeList.stream()
                .map(ga -> getDynamicListElements(ga.getId() + "#" + ga.getGeneralApplicationItems().getGeneralApplicationStatus(),
                    getLabel(ga.getGeneralApplicationItems(), index.incrementAndGet())))
                .toList();

            DynamicList dynamicList = generateAvailableGeneralApplicationAsDynamicList(dynamicListElements);
            caseData.getGeneralApplicationWrapper().setGeneralApplicationDirectionsList(dynamicList);
        }
        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().data(caseData).build();
    }

    private void setDirectionListForNonCollectionGeneralApplication(FinremCaseData caseData,
                                                                    AtomicInteger index,
                                                                    String userAuthorisation, String caseId) {
        GeneralApplicationItems applicationItems = helper.getApplicationItems(caseData, userAuthorisation, caseId);
        DynamicListElement dynamicListElements
            = getDynamicListElements(applicationItems.getGeneralApplicationCreatedBy(), getLabel(applicationItems, index.incrementAndGet()));

        List<DynamicListElement> dynamicListElementsList = new ArrayList<>();
        dynamicListElementsList.add(dynamicListElements);

        DynamicList dynamicList = generateAvailableGeneralApplicationAsDynamicList(dynamicListElementsList);
        caseData.getGeneralApplicationWrapper().setGeneralApplicationDirectionsList(dynamicList);
    }

}
