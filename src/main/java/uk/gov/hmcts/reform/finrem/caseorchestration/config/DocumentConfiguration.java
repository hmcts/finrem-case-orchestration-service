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
    private String coversheetTemplate;
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
    private String helpWithFeesSuccessfulTemplate;
    private String helpWithFeesSuccessfulFileName;
}
