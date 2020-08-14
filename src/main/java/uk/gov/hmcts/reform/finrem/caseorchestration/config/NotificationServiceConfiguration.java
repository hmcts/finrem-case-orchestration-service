package uk.gov.hmcts.reform.finrem.caseorchestration.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "finrem.notification")
public class NotificationServiceConfiguration {
    private String url;
    private String api;
    private String hwfSuccessful;
    private String assignToJudge;
    private String consentOrderMade;
    private String consentOrderNotApproved;
    private String consentOrderAvailable;
    private String consentOrderAvailableCtsc;
    private String ctscEmail;
    private String consentGeneralEmail;
    private String prepareForHearing;
    private String prepareForHearingOrderSent;
    private String contestedHwfSuccessful;
    private String contestedApplicationIssued;
    private String contestOrderApproved;
    private String contestedDraftOrder;
    private String contestedGeneralEmail;
    private String contestedOrderNotApproved;
    private String contestedConsentOrderApproved;
    private String contestedConsentGeneralOrder;
    private String contestedConsentOrderNotApproved;
}
