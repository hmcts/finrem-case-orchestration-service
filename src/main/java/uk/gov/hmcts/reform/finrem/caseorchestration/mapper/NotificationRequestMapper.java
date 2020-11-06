package uk.gov.hmcts.reform.finrem.caseorchestration.mapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.ContestedCourtHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.notification.NotificationRequest;

import java.util.Map;
import java.util.Objects;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.DIVORCE_CASE_NUMBER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_EMAIL_BODY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.isConsentedApplication;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.isContestedApplication;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationRequestMapper {

    private static final String CONSENTED = "consented";
    private static final String CONTESTED = "contested";

    public NotificationRequest createNotificationRequestForAppSolicitor(CallbackRequest callbackRequest) {
        return isConsentedApplication(callbackRequest.getCaseDetails())
            ? buildNotificationRequest(callbackRequest, SOLICITOR_REFERENCE,
            CONSENTED_SOLICITOR_NAME, SOLICITOR_EMAIL, CONSENTED, GENERAL_EMAIL_BODY, DIVORCE_CASE_NUMBER)
            : buildNotificationRequest(callbackRequest, SOLICITOR_REFERENCE,
            CONTESTED_SOLICITOR_NAME, CONTESTED_SOLICITOR_EMAIL, CONTESTED, GENERAL_EMAIL_BODY, DIVORCE_CASE_NUMBER);
    }

    public NotificationRequest createNotificationRequestForRespSolicitor(CallbackRequest callbackRequest) {
        return isConsentedApplication(callbackRequest.getCaseDetails())
            ? buildNotificationRequest(callbackRequest, RESP_SOLICITOR_REFERENCE,
            RESP_SOLICITOR_NAME, RESP_SOLICITOR_EMAIL, CONSENTED, GENERAL_EMAIL_BODY, DIVORCE_CASE_NUMBER)
            : buildNotificationRequest(callbackRequest, RESP_SOLICITOR_REFERENCE,
            RESP_SOLICITOR_NAME, RESP_SOLICITOR_EMAIL, CONTESTED, GENERAL_EMAIL_BODY, DIVORCE_CASE_NUMBER);
    }

    private NotificationRequest buildNotificationRequest(CallbackRequest callbackRequest,
                                                         String solicitorReference,
                                                         String solicitorName,
                                                         String solicitorEmail,
                                                         String caseType,
                                                         String generalEmailBody,
                                                         String divorceCaseNumber) {
        NotificationRequest notificationRequest = new NotificationRequest();
        Map<String, Object> mapOfCaseData = callbackRequest.getCaseDetails().getData();

        notificationRequest.setCaseReferenceNumber(Objects.toString(callbackRequest.getCaseDetails().getId()));
        notificationRequest.setSolicitorReferenceNumber(Objects.toString(mapOfCaseData.get(solicitorReference)));
        notificationRequest.setDivorceCaseNumber(Objects.toString(mapOfCaseData.get(divorceCaseNumber)));
        notificationRequest.setName(Objects.toString(mapOfCaseData.get(solicitorName)));
        notificationRequest.setNotificationEmail(Objects.toString(mapOfCaseData.get(solicitorEmail)));
        notificationRequest.setGeneralEmailBody(Objects.toString(mapOfCaseData.get(generalEmailBody)));
        notificationRequest.setCaseType(caseType);

        if (isContestedApplication(callbackRequest.getCaseDetails())) {
            String selectedCourt = ContestedCourtHelper.getSelectedCourt(callbackRequest.getCaseDetails());
            notificationRequest.setSelectedCourt(selectedCourt);

            log.info("selectedCourt is {} for case ID: {}", selectedCourt,
                notificationRequest.getCaseReferenceNumber());
        }
        return notificationRequest;
    }
}
