package uk.gov.hmcts.reform.finrem.caseorchestration.handler.generalapplicationdirections;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.CallbackHandlerLogger;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.GeneralApplicationHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.GeneralApplicationHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationCollectionData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationItems;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.HearingType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.WorkingHearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ManageHearingsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignCaseAccessService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralApplicationDirectionsService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PartyService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole.APP_SOLICITOR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole.RESP_SOLICITOR;

@Slf4j
@Service
public class GeneralApplicationDirectionsAboutToStartHandler extends FinremCallbackHandler implements GeneralApplicationHandler {

    private final AssignCaseAccessService assignCaseAccessService;
    private final GeneralApplicationHelper helper;
    private final GeneralApplicationDirectionsService generalApplicationDirectionsService;
    private final PartyService partyService;

    public GeneralApplicationDirectionsAboutToStartHandler(AssignCaseAccessService assignCaseAccessService,
                                                           FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                           GeneralApplicationHelper helper,
                                                           GeneralApplicationDirectionsService generalApplicationDirectionsService,
                                                           PartyService partyService) {
        super(finremCaseDetailsMapper);
        this.helper = helper;
        this.generalApplicationDirectionsService = generalApplicationDirectionsService;
        this.assignCaseAccessService = assignCaseAccessService;
        this.partyService = partyService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_START.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.GENERAL_APPLICATION_DIRECTIONS_MH.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        log.info(CallbackHandlerLogger.aboutToStart(callbackRequest));
        FinremCaseDetails finremCaseDetails = callbackRequest.getCaseDetails();

        String caseId = finremCaseDetails.getCaseIdAsString();

        FinremCaseData caseData = finremCaseDetails.getData();

        // Initialize the working hearing for general application directions (MH)
        initialiseWorkingHearing(caseData.getManageHearingsWrapper(), finremCaseDetails);

        String loggedInUserCaseRole = assignCaseAccessService.getActiveUser(caseId, userAuthorisation);
        log.info("Logged in user case role type {} on Case ID: {}", loggedInUserCaseRole, caseId);
        caseData.setCurrentUserCaseRoleType(loggedInUserCaseRole);

        generalApplicationDirectionsService.resetGeneralApplicationDirectionsFields(caseData);

        helper.populateGeneralApplicationSender(caseData, caseData.getGeneralApplicationWrapper().getGeneralApplications());

        List<GeneralApplicationCollectionData> outcomeList = helper.getOutcomeList(caseData);
        AtomicInteger index = new AtomicInteger(0);
        if (outcomeList.isEmpty() && caseData.getGeneralApplicationWrapper().getGeneralApplicationCreatedBy() != null) {
            log.info("Setting direction list if existing general application not moved to collection for Case ID: {}", caseId);
            setDirectionListForNonCollectionGeneralApplication(caseData, index, userAuthorisation, caseId);
        } else {
            if (outcomeList.isEmpty()) {
                log.info("The user cannot carry out the directions as there are no general applications in the outcome state"
                    + " for Case ID: {}", caseId);
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

    private void initialiseWorkingHearing(ManageHearingsWrapper manageHearingsWrapper, FinremCaseDetails finremCaseDetails) {
        manageHearingsWrapper.setWorkingHearing(WorkingHearing.builder()
            .partiesOnCaseMultiSelectList(getDefaultPartiesOnCaseMultiSelectList(finremCaseDetails))
            .hearingNoticePrompt(YesOrNo.YES)
            .withHearingTypes(HearingType.APPLICATION_HEARING)
            .build());
    }

    private DynamicMultiSelectList getDefaultPartiesOnCaseMultiSelectList(FinremCaseDetails finremCaseDetails) {
        return partyService.getAllActivePartyList(finremCaseDetails)
            .setValueByCodes(Stream.of(APP_SOLICITOR, RESP_SOLICITOR)
                .map(CaseRole::getCcdCode)
                .toList());
    }
}
