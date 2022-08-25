package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.GeneralApplicationHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationCollectionData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationItems;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PaperNotificationService;

import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_LIST;

@Slf4j
@Component
@RequiredArgsConstructor
public class RejectGeneralApplicationSubmittedHandler implements CallbackHandler {

    public static final String APPLICANT = "applicant";
    public static final String RESPONDENT = "respondent";
    public static final String CASE = "case";

    private final NotificationService notificationService;
    private final PaperNotificationService paperNotificationService;
    private final ObjectMapper objectMapper;
    private final GeneralApplicationHelper generalApplicationHelper;

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.SUBMITTED.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.REJECT_GENERAL_APPLICATION.equals(eventType);
    }

    @Override
    public AboutToStartOrSubmitCallbackResponse handle(CallbackRequest callbackRequest, String userAuthorisation) {

        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        String receivedFrom = getApplicationReceivedFrom(caseDetails, callbackRequest.getCaseDetailsBefore());

        if (APPLICANT.equals(receivedFrom)) {
            sendApplicantNotifications(userAuthorisation, caseDetails);
        }

        if (RESPONDENT.equals(receivedFrom)) {
            sendRespondentNotifications(userAuthorisation, caseDetails);
        }

        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDetails.getData()).build();
    }

    private void sendApplicantNotifications(String userAuthorisation, CaseDetails caseDetails) {
        if (notificationService.isApplicantSolicitorRegisteredAndEmailCommunicationEnabled(caseDetails)) {
            notificationService.sendGeneralApplicationRejectionEmailToAppSolicitor(caseDetails);
        } else {
            paperNotificationService.printApplicantRejectionGeneralApplication(caseDetails, userAuthorisation);
        }
    }

    private void sendRespondentNotifications(String userAuthorisation, CaseDetails caseDetails) {
        if (notificationService.isRespondentSolicitorRegisteredAndEmailCommunicationEnabled(caseDetails)) {
            notificationService.sendGeneralApplicationRejectionEmailToResSolicitor(caseDetails);
        } else {
            paperNotificationService.printRespondentRejectionGeneralApplication(caseDetails, userAuthorisation);
        }
    }

    private String getApplicationReceivedFrom(CaseDetails caseDetails, CaseDetails caseDetailsBefore) {
        DynamicList dynamicList = generalApplicationHelper.objectToDynamicList(caseDetails.getData().get(GENERAL_APPLICATION_LIST));
        String valueCode = dynamicList.getValueCode();

        List<GeneralApplicationCollectionData> applicationCollectionDataList =
            objectMapper.convertValue(caseDetailsBefore.getData().get(GENERAL_APPLICATION_COLLECTION), new TypeReference<>() {});

        Optional<GeneralApplicationCollectionData> rejectedApplication = applicationCollectionDataList.stream()
            .filter(document -> document.getId().equals(valueCode))
            .findFirst();

        return rejectedApplication
            .map(GeneralApplicationCollectionData::getGeneralApplicationItems)
            .map(GeneralApplicationItems::getGeneralApplicationReceivedFrom)
            .orElse(StringUtils.EMPTY);
    }
}
