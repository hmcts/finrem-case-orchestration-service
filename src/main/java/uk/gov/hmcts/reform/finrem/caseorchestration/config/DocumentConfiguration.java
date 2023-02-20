package uk.gov.hmcts.reform.finrem.caseorchestration.config;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HIGHCOURT_COURTLIST;

@Data
@Component
@ConfigurationProperties(prefix = "document")
public class DocumentConfiguration {
    private String bulkPrintTemplate;
    private String bulkPrintFileName;
    @Getter(AccessLevel.NONE)
    private String miniFormTemplate;
    private String miniFormHighCourtTemplate;
    private String miniFormFileName;
    private String rejectedOrderTemplate;
    private String rejectedOrderFileName;
    private String rejectedVariationOrderFileName;
    private String coversheetFileName;
    private String rejectedOrderDocType;
    private String formCFastTrackTemplate;
    private String formCNonFastTrackTemplate;
    private String formCFileName;
    @Getter(AccessLevel.NONE)
    private String formGTemplate;
    private String formGHighCourtTemplate;
    private String formGFileName;
    private String outOfFamilyCourtResolutionTemplate;
    private String outOfFamilyCourtResolutionName;
    @Getter(AccessLevel.NONE)
    private String contestedMiniFormTemplate;
    private String contestedMiniFormHighCourtTemplate;
    private String contestedMiniFormFileName;
    private String contestedDraftMiniFormTemplateSchedule;
    private String contestedDraftMiniFormTemplate;
    private String contestedDraftMiniFormFileName;
    private String generalLetterTemplate;
    private String generalLetterFileName;
    @Getter(AccessLevel.NONE)
    private String approvedConsentOrderTemplate;
    private String approvedConsentOrderHighCourtTemplate;
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
    @Getter(AccessLevel.NONE)
    private String generalOrderTemplate;
    private String generalOrderHighCourtTemplate;
    private String generalOrderFileName;
    @Getter(AccessLevel.NONE)
    private String contestedDraftOrderNotApprovedTemplate;
    private String contestedDraftOrderNotApprovedHighCourtTemplate;
    private String contestedDraftOrderNotApprovedFileName;
    @Getter(AccessLevel.NONE)
    private String contestedOrderApprovedCoverLetterTemplate;
    private String contestedOrderApprovedCoverLetterHighCourtTemplate;
    private String contestedOrderApprovedCoverLetterFileName;
    private String manualPaymentTemplate;
    private String manualPaymentFileName;
    @Getter(AccessLevel.NONE)
    private String generalApplicationHearingNoticeTemplate;
    private String generalApplicationHearingNoticeHighCourtTemplate;
    private String generalApplicationHearingNoticeFileName;
    @Getter(AccessLevel.NONE)
    private String generalApplicationOrderTemplate;
    private String generalApplicationOrderHighCourtTemplate;
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
    private String hearingNoticeConsentedTemplate;
    private String hearingNoticeConsentedFileName;
    private String barristerAddedTemplate;
    private String barristerAddedFilename;
    private String barristerRemovedTemplate;
    private String barristerRemovedFilename;

    public String getGeneralOrderTemplate(CaseDetails caseDetails) {
        return isHighCourtSelected(caseDetails) ? generalOrderHighCourtTemplate : generalOrderTemplate;
    }

    public String getMiniFormTemplate(CaseDetails caseDetails) {
        return isHighCourtSelected(caseDetails) ? miniFormHighCourtTemplate : miniFormTemplate;
    }

    public String getFormGTemplate(CaseDetails caseDetails) {
        return isHighCourtSelected(caseDetails) ? formGHighCourtTemplate : formGTemplate;
    }

    public String getContestedMiniFormTemplate(CaseDetails caseDetails) {
        return isHighCourtSelected(caseDetails) ? contestedMiniFormHighCourtTemplate : contestedMiniFormTemplate;
    }

    public String getApprovedConsentOrderTemplate(CaseDetails caseDetails) {
        return isHighCourtSelected(caseDetails) ? approvedConsentOrderHighCourtTemplate : approvedConsentOrderTemplate;
    }

    @Deprecated
    public String getContestedDraftOrderNotApprovedTemplate() {
        return contestedDraftOrderNotApprovedTemplate;
    }

    public String getContestedDraftOrderNotApprovedTemplate(CaseDetails caseDetails) {
        return isHighCourtSelected(caseDetails) ? contestedDraftOrderNotApprovedHighCourtTemplate
            : contestedDraftOrderNotApprovedTemplate;
    }

    public String getContestedOrderApprovedCoverLetterTemplate(CaseDetails caseDetails) {
        return isHighCourtSelected(caseDetails) ? contestedOrderApprovedCoverLetterHighCourtTemplate
            : contestedOrderApprovedCoverLetterTemplate;
    }

    public String getGeneralApplicationHearingNoticeTemplate(CaseDetails caseDetails) {
        return isHighCourtSelected(caseDetails) ? generalApplicationHearingNoticeHighCourtTemplate
            : generalApplicationHearingNoticeTemplate;
    }

    public String getGeneralApplicationOrderTemplate(CaseDetails caseDetails) {
        return isHighCourtSelected(caseDetails) ? generalApplicationOrderHighCourtTemplate
            : generalApplicationOrderTemplate;
    }

    private boolean isHighCourtSelected(CaseDetails caseDetails) {
        if (caseDetails != null && caseDetails.getData() != null
            && caseDetails.getData().get(HIGHCOURT_COURTLIST) != null) {
            return true;
        }
        return false;
    }
}
