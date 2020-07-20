package uk.gov.hmcts.reform.finrem.caseorchestration.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "document")
public class DocumentConfiguration {
    private String bulkPrintTemplate;
    private String bulkPrintFileName;
    private String miniFormTemplate;
    private String miniFormFileName;
    private String rejectedOrderTemplate;
    private String rejectedOrderFileName;
    private String coversheetFileName;
    private String rejectedOrderDocType;
    private String formCFastTrackTemplate;
    private String formCNonFastTrackTemplate;
    private String formCFileName;
    private String formGTemplate;
    private String formGFileName;
    private String contestedMiniFormTemplate;
    private String contestedMiniFormFileName;
    private String contestedDraftMiniFormTemplate;
    private String contestedDraftMiniFormFileName;
    private String generalLetterTemplate;
    private String generalLetterFileName;
    private String approvedConsentOrderTemplate;
    private String approvedConsentOrderFileName;
    private String approvedConsentOrderNotificationTemplate;
    private String approvedConsentOrderNotificationFileName;
    private String assignedToJudgeNotificationTemplate;
    private String assignedToJudgeNotificationFileName;
    private String helpWithFeesSuccessfulNotificationTemplate;
    private String helpWithFeesSuccessfulNotificationFileName;
    private String consentOrderNotApprovedCoverLetterTemplate;
    private String consentOrderNotApprovedCoverLetterFileName;
    private String consentOrderNotApprovedReplyCoversheetTemplate;
    private String consentOrderNotApprovedReplyCoversheetFileName;
    private String generalOrderTemplate;
    private String generalOrderFileName;
    private String contestedApplicationNotApprovedTemplate;
    private String contestedApplicationNotApprovedFileName;
}
