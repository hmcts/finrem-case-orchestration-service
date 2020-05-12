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
    private String prepareForHearing;
    private String contestedHwfSuccessful;
    private String contestedApplicationIssued;
    private String contestOrderApproved;
    private String contestedDraftOrder;
}
