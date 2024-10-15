package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.GeneralApplicationHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicRadioList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicRadioListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationCollectionData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationItems;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralApplicationWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PaperNotificationService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class RejectGeneralApplicationSubmittedHandler extends FinremCallbackHandler {

    public static final String APPLICANT = "applicant";
    public static final String RESPONDENT = "respondent";
    private final NotificationService notificationService;
    private final PaperNotificationService paperNotificationService;
    private final ObjectMapper objectMapper;
    private final GeneralApplicationHelper generalApplicationHelper;
    private final FinremCaseDetailsMapper finremCaseDetailsMapper;

    public RejectGeneralApplicationSubmittedHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                    NotificationService notificationService,
                                                    PaperNotificationService paperNotificationService,
                                                    ObjectMapper objectMapper,
                                                    GeneralApplicationHelper generalApplicationHelper) {
        super(finremCaseDetailsMapper);
        this.notificationService = notificationService;
        this.paperNotificationService = paperNotificationService;
        this.objectMapper = objectMapper;
        this.generalApplicationHelper = generalApplicationHelper;
        this.finremCaseDetailsMapper = finremCaseDetailsMapper;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.SUBMITTED.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.REJECT_GENERAL_APPLICATION.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                                   String userAuthorisation) {

        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        String receivedFrom = getApplicationReceivedFrom(caseDetails, callbackRequest.getCaseDetailsBefore()) == null
            ? null : getApplicationReceivedFrom(
                caseDetails, callbackRequest.getCaseDetailsBefore()).getValue().getCode();

        log.info("General application receivedFrom: {} for Case ID: {} ", receivedFrom, caseDetails.getId());
        if (APPLICANT.equalsIgnoreCase(receivedFrom)) {
            sendApplicantNotifications(userAuthorisation, caseDetails);
        }

        if (RESPONDENT.equalsIgnoreCase(receivedFrom)) {
            sendRespondentNotifications(userAuthorisation, caseDetails);
        }

        final List<IntervenerWrapper> interveners = caseDetails.getData().getInterveners();
        interveners.forEach(intervenerWrapper -> {
            if (intervenerWrapper.getIntervenerType().getTypeValue().equalsIgnoreCase(receivedFrom)) {
                log.info("Reject general application receivedFrom matched");
                sendIntervenerNotifications(userAuthorisation, caseDetails, intervenerWrapper);
            }
        });

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().data(caseDetails.getData()).build();
    }

    @SuppressWarnings("squid:CallToDeprecatedMethod")
    private void sendApplicantNotifications(String userAuthorisation, FinremCaseDetails caseDetails) {
        if (notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)) {
            notificationService.sendGeneralApplicationRejectionEmailToAppSolicitor(caseDetails);
        } else {
            CaseDetails caseDetailsForNotification = finremCaseDetailsMapper.mapToCaseDetails(caseDetails);
            paperNotificationService.printApplicantRejectionGeneralApplication(
                caseDetailsForNotification, userAuthorisation);
        }
    }

    @SuppressWarnings("squid:CallToDeprecatedMethod")
    private void sendRespondentNotifications(String userAuthorisation, FinremCaseDetails caseDetails) {
        if (notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails)) {
            notificationService.sendGeneralApplicationRejectionEmailToResSolicitor(caseDetails);
        } else {
            CaseDetails caseDetailsForNotification = finremCaseDetailsMapper.mapToCaseDetails(caseDetails);
            paperNotificationService.printRespondentRejectionGeneralApplication(
                caseDetailsForNotification, userAuthorisation);
        }
    }

    @SuppressWarnings("squid:CallToDeprecatedMethod")
    private void sendIntervenerNotifications(String userAuthorisation, FinremCaseDetails caseDetails,
                                             IntervenerWrapper intervenerWrapper) {
        if (notificationService.isIntervenerSolicitorDigitalAndEmailPopulated(intervenerWrapper, caseDetails)) {
            log.info("Send general application rejection email for Case ID: {}", caseDetails.getId());
            notificationService.sendGeneralApplicationRejectionEmailToIntervenerSolicitor(caseDetails, intervenerWrapper);
        } else {
            log.info("Send general application rejection letter for Case ID: {}", caseDetails.getId());
            CaseDetails caseDetailsForNotification = finremCaseDetailsMapper.mapToCaseDetails(caseDetails);
            paperNotificationService.printIntervenerRejectionGeneralApplication(
                caseDetailsForNotification, intervenerWrapper, userAuthorisation);
        }
    }

    private DynamicRadioList getApplicationReceivedFrom(FinremCaseDetails caseDetails, FinremCaseDetails caseDetailsBefore) {

        List<GeneralApplicationCollectionData> applicationCollectionDataList = new ArrayList<>();

        GeneralApplicationWrapper wrapperBefore = caseDetailsBefore.getData().getGeneralApplicationWrapper();
        if (wrapperBefore.getGeneralApplications() != null && !wrapperBefore.getGeneralApplications().isEmpty()) {
            applicationCollectionDataList.addAll(objectMapper.convertValue(
                wrapperBefore.getGeneralApplications(), new TypeReference<>() {
                }));
        }
        if (wrapperBefore.getIntervener1GeneralApplications() != null
            && !wrapperBefore.getIntervener1GeneralApplications().isEmpty()) {
            applicationCollectionDataList.addAll(objectMapper.convertValue(
                wrapperBefore.getIntervener1GeneralApplications(), new TypeReference<>() {
                }));
        }
        if (wrapperBefore.getIntervener2GeneralApplications() != null
            && !wrapperBefore.getIntervener2GeneralApplications().isEmpty()) {
            applicationCollectionDataList.addAll(objectMapper.convertValue(
                wrapperBefore.getIntervener2GeneralApplications(), new TypeReference<>() {
                }));
        }
        if (wrapperBefore.getIntervener3GeneralApplications() != null
            && !wrapperBefore.getIntervener3GeneralApplications().isEmpty()) {
            applicationCollectionDataList.addAll(objectMapper.convertValue(
                wrapperBefore.getIntervener3GeneralApplications(), new TypeReference<>() {
                }));
        }
        if (wrapperBefore.getIntervener4GeneralApplications() != null
            && !wrapperBefore.getIntervener4GeneralApplications().isEmpty()) {
            applicationCollectionDataList.addAll(
                objectMapper.convertValue(wrapperBefore.getIntervener4GeneralApplications(), new TypeReference<>() {
                }));
        }
        if (wrapperBefore.getAppRespGeneralApplications() != null
            && !wrapperBefore.getAppRespGeneralApplications().isEmpty()) {
            applicationCollectionDataList.addAll(objectMapper.convertValue(
                wrapperBefore.getAppRespGeneralApplications(), new TypeReference<>() {
                }));
        }
        DynamicList dynamicList = generalApplicationHelper.objectToDynamicList(caseDetails.getData()
            .getGeneralApplicationWrapper().getGeneralApplicationList());
        String valueCode = dynamicList.getValueCode();
        Optional<GeneralApplicationCollectionData> rejectedApplication = applicationCollectionDataList.stream()
            .filter(document -> document.getId().equals(valueCode))
            .findFirst();

        List<DynamicRadioListElement> dynamicListElements = new ArrayList<>();
        generalApplicationHelper.buildDynamicIntervenerList(dynamicListElements, caseDetails.getData());
        DynamicRadioList dynamicRadioList = generalApplicationHelper.getDynamicRadioList(dynamicListElements);

        return rejectedApplication
            .map(GeneralApplicationCollectionData::getGeneralApplicationItems)
            .map(GeneralApplicationItems::getGeneralApplicationSender)
            .orElse(dynamicRadioList);
    }
}
