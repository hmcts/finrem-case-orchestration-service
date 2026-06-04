package uk.gov.hmcts.reform.finrem.caseorchestration.handler.managehearings;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.CallbackHandlerLogger;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.managehearings.HearingCorrespondenceHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.ManageHearingsAction;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.VacateOrAdjournAction;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.hearings.Hearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.hearings.VacateOrAdjournedHearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ManageHearingsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationAuditService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.managehearings.ManageHearingActionService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ContestedStatus.PREPARE_FOR_HEARING;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTESTED_ADJOURN_NOTIFICATION_SOLICITOR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTESTED_HEARING_NOTIFICATION_SOLICITOR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTESTED_VACATE_NOTIFICATION_SOLICITOR;

@Slf4j
@Service
public class ManageHearingsAboutToSubmitHandler  extends FinremCallbackHandler {

    private final ManageHearingActionService manageHearingActionService;
    private final NotificationAuditService notificationAuditService;
    private final HearingCorrespondenceHelper hearingCorrespondenceHelper;

    public ManageHearingsAboutToSubmitHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                              ManageHearingActionService manageHearingActionService,
                                              NotificationAuditService notificationAuditService,
                                              HearingCorrespondenceHelper hearingCorrespondenceHelper) {
        super(finremCaseDetailsMapper);
        this.manageHearingActionService = manageHearingActionService;
        this.notificationAuditService = notificationAuditService;
        this.hearingCorrespondenceHelper = hearingCorrespondenceHelper;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.MANAGE_HEARINGS.equals(eventType);
    }

    /**
     * Handles the 'About to Submit' callback for managing hearings.
     * When a hearing is added (ManageHearingsAction.ADD_HEARING), the case state is explicitly set to PREPARE_FOR_HEARING.
     * Other hearing actions, when built, will keep the case in the same state.
     *
     * @param callbackRequest the request containing case details
     * @param userAuthorisation the user authorisation token
     * @return a response containing updated case data
     */
    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        log.info(CallbackHandlerLogger.aboutToSubmit(callbackRequest));
        FinremCaseDetails finremCaseDetails = callbackRequest.getCaseDetails();

        FinremCaseData finremCaseData = finremCaseDetails.getData();

        ManageHearingsWrapper hearingsWrapper = finremCaseData.getManageHearingsWrapper();
        ManageHearingsAction actionSelection = hearingsWrapper.getManageHearingsActionSelection();

        if (ManageHearingsAction.ADD_HEARING.equals(actionSelection)
            || (YesOrNo.YES.equals(hearingsWrapper.getIsRelistSelected()))) {
            manageHearingActionService.performAddHearing(finremCaseDetails, userAuthorisation);
            finremCaseData.setState(PREPARE_FOR_HEARING.getId());
        }

        if (ManageHearingsAction.ADJOURN_OR_VACATE_HEARING.equals(actionSelection)) {
            manageHearingActionService.performAdjournOrVacateHearing(finremCaseDetails, userAuthorisation);
        }

        manageHearingActionService.updateTabData(finremCaseData);
        createNotificationAuditRows(callbackRequest, actionSelection, hearingsWrapper);

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(finremCaseData).build();
    }

    private void createNotificationAuditRows(FinremCallbackRequest callbackRequest,
                                             ManageHearingsAction actionSelection,
                                             ManageHearingsWrapper hearingsWrapper) {
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        EventType eventType = callbackRequest.getEventType();

        boolean addHearingFlow = ManageHearingsAction.ADD_HEARING.equals(actionSelection)
            || YesOrNo.YES.equals(hearingsWrapper.getIsRelistSelected());

        if (ManageHearingsAction.ADD_HEARING.equals(actionSelection)) {
            auditNewHearingNotice(caseDetails, hearingsWrapper, eventType);
            return;
        }

        if (ManageHearingsAction.ADJOURN_OR_VACATE_HEARING.equals(actionSelection)) {
            if (addHearingFlow) {
                auditNewHearingNotice(caseDetails, hearingsWrapper, eventType);
            }
            auditVacateNotice(caseDetails, hearingsWrapper, eventType);
        }
    }

    private void auditNewHearingNotice(FinremCaseDetails caseDetails,
                                       ManageHearingsWrapper wrapper,
                                       EventType eventType) {
        Hearing hearing = hearingCorrespondenceHelper.getActiveHearingInContext(wrapper, wrapper.getWorkingHearingId());
        if (!hearing.shouldSendNotifications()) {
            return;
        }

        List<CaseDocument> documentsToPost = collectNewHearingDocuments(caseDetails.getData(), hearing, wrapper);

        notificationAuditService.createAuditsForHearingCorrespondence(
            caseDetails,
            hearing,
            eventType,
            FR_CONTESTED_HEARING_NOTIFICATION_SOLICITOR,
            documentsToPost
        );
    }

    private void auditVacateNotice(FinremCaseDetails caseDetails,
                                   ManageHearingsWrapper wrapper,
                                   EventType eventType) {
        VacateOrAdjournedHearing vacateOrAdjournedHearing =
            hearingCorrespondenceHelper.getVacateOrAdjournedHearingInContext(wrapper, wrapper.getWorkingVacatedHearingId());

        boolean isVacatedAndRelisted = hearingCorrespondenceHelper.isVacatedAndRelistedHearing(caseDetails.getData());

        if (!isVacatedAndRelisted && !vacateOrAdjournedHearing.shouldSendNotifications()) {
            return;
        }

        CaseDocument vacateNotice = hearingCorrespondenceHelper.getVacateHearingNotice(caseDetails.getData());
        List<CaseDocument> documentsToPost = vacateNotice == null ? List.of() : List.of(vacateNotice);

        VacateOrAdjournAction action = vacateOrAdjournedHearing.getHearingStatus();
        var templateName = VacateOrAdjournAction.ADJOURN_HEARING.equals(action)
            ? FR_CONTESTED_ADJOURN_NOTIFICATION_SOLICITOR
            : FR_CONTESTED_VACATE_NOTIFICATION_SOLICITOR;

        notificationAuditService.createAuditsForVacateCorrespondence(
            caseDetails,
            vacateOrAdjournedHearing,
            eventType,
            templateName,
            documentsToPost
        );
    }

    private List<CaseDocument> collectNewHearingDocuments(FinremCaseData caseData,
                                                          Hearing hearing,
                                                          ManageHearingsWrapper wrapper) {
        List<CaseDocument> documentsToPost = new ArrayList<>(Optional.ofNullable(hearing.getAdditionalHearingDocs())
            .orElseGet(List::of)
            .stream()
            .map(DocumentCollectionItem::getValue)
            .toList());

        hearingCorrespondenceHelper.getMiniFormAIfRequired(caseData, hearing).ifPresent(documentsToPost::add);
        documentsToPost.addAll(wrapper.getAssociatedWorkingHearingDocuments());

        return documentsToPost;
    }
}
