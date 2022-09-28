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
    private String rejectedVariationOrderFileName;
    private String coversheetFileName;
    private String rejectedOrderDocType;
    private String formCFastTrackTemplate;
    private String formCNonFastTrackTemplate;
    private String formCFileName;
    private String formGTemplate;
    private String formGFileName;
    private String outOfFamilyCourtResolutionTemplate;
    private String outOfFamilyCourtResolutionName;
    private String contestedMiniFormTemplate;
    private String contestedMiniFormFileName;
    private String contestedDraftMiniFormTemplate;
    private String contestedDraftMiniFormFileName;
    private String generalLetterTemplate;
    private String generalLetterFileName;
    private String approvedConsentOrderTemplate;
    private String approvedConsentOrderFileName;
    private String approvedVariationOrderFileName;
    private String approvedConsentOrderNotificationTemplate;
    private String approvedConsentOrderNotificationFileName;
    private String approvedVariationOrderNotificationFileName;
    private String assignedToJudgeNotificationTemplate;
    private String assignedToJudgeNotificationFileName;
    private String consentInContestedAssignedToJudgeNotificationTemplate;
    private String consentInContestedAssignedToJudgeNotificationFileName;
    private String helpWithFeesSuccessfulNotificationTemplate;
    private String helpWithFeesSuccessfulNotificationFileName;
    private String consentOrderNotApprovedCoverLetterTemplate;
    private String consentOrderNotApprovedCoverLetterFileName;
    private String variationOrderNotApprovedCoverLetterFileName;
    private String consentOrderNotApprovedReplyCoversheetTemplate;
    private String consentOrderNotApprovedReplyCoversheetFileName;
    private String generalOrderTemplate;
    private String generalOrderFileName;
    private String contestedDraftOrderNotApprovedTemplate;
    private String contestedDraftOrderNotApprovedFileName;
    private String contestedOrderApprovedCoverLetterTemplate;
    private String contestedOrderApprovedCoverLetterFileName;
    private String manualPaymentTemplate;
    private String manualPaymentFileName;
    private String generalApplicationHearingNoticeTemplate;
    private String generalApplicationHearingNoticeFileName;
    private String generalApplicationOrderTemplate;
    private String generalApplicationOrderFileName;
    private String generalApplicationRejectionTemplate;
    private String generalApplicationRejectionFileName;
    private String additionalHearingTemplate;
    private String additionalHearingFileName;
    private String generalApplicationInterimHearingNoticeTemplate;
    private String generalApplicationInterimHearingNoticeFileName;
    private String nocLetterNotificationSolicitorTemplate;
    private String nocLetterNotificationSolicitorFileName;
    private String nocLetterNotificationLitigantSolicitorAddedTemplate;
    private String nocLetterNotificationLitigantSolicitorAddedFileName;
    private String nocLetterNotificationLitigantSolicitorRevokedTemplate;
    private String nocLetterNotificationLitigantSolicitorRevokedFileName;
    private String updateFRCInformationSolicitorTemplate;
    private String updateFRCInformationSolicitorFilename;
    private String updateFRCInformationLitigantTemplate;
    private String updateFRCInformationLitigantFilename;
}
