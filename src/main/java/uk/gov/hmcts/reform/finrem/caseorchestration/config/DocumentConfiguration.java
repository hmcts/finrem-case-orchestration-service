package uk.gov.hmcts.reform.finrem.caseorchestration.config;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HIGHCOURT;
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
    @Getter(AccessLevel.NONE)
    private String rejectedOrderTemplate;
    private String rejectedOrderHighCourtTemplate;
    private String rejectedOrderFileName;
    private String rejectedVariationOrderFileName;
    private String coversheetFileName;
    private String rejectedOrderDocType;
    @Getter(AccessLevel.NONE)
    private String formCFastTrackTemplate;
    private String formCFastTrackHighCourtTemplate;
    @Getter(AccessLevel.NONE)
    private String formCNonFastTrackTemplate;
    private String formCNonFastTrackHighCourtTemplate;
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
    @Getter(AccessLevel.NONE)
    private String contestedMiniFormScheduleTemplate;
    private String contestedMiniFormHighCourtScheduleTemplate;
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
    @Getter(AccessLevel.NONE)
    private String generalApplicationInterimHearingNoticeTemplate;
    private String generalApplicationInterimHearingNoticeHighCourtTemplate;
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
    @Getter(AccessLevel.NONE)
    private String hearingNoticeConsentedTemplate;
    private String hearingNoticeConsentedHighCourtTemplate;
    private String hearingNoticeConsentedFileName;
    private String barristerAddedTemplate;
    private String barristerAddedFilename;
    private String barristerRemovedTemplate;
    private String barristerRemovedFilename;

    private String intervenerAddedTemplate;
    private String intervenerAddedFilename;
    private String intervenerAddedSolicitorTemplate;
    private String intervenerAddedSolicitorFilename;
    private String intervenerRemovedTemplate;
    private String intervenerRemovedFilename;
    private String intervenerRemovedSolicitorTemplate;
    private String intervenerRemovedSolicitorFilename;

    public String getGeneralOrderTemplate(CaseDetails caseDetails) {
        return isHighCourtSelected(caseDetails) ? generalOrderHighCourtTemplate : generalOrderTemplate;
    }

    public String getMiniFormTemplate(CaseDetails caseDetails) {
        return isHighCourtSelected(caseDetails) ? miniFormHighCourtTemplate : miniFormTemplate;
    }

    public String getRejectedOrderTemplate(CaseDetails caseDetails) {
        return isHighCourtSelected(caseDetails) ? rejectedOrderHighCourtTemplate : rejectedOrderTemplate;
    }

    public String getFormCFastTrackTemplate(CaseDetails caseDetails) {
        return isHighCourtSelected(caseDetails) ? formCFastTrackHighCourtTemplate : formCFastTrackTemplate;
    }

    public String getFormCNonFastTrackTemplate(CaseDetails caseDetails) {
        return isHighCourtSelected(caseDetails) ? formCNonFastTrackHighCourtTemplate : formCNonFastTrackTemplate;
    }

    public String getFormGTemplate(CaseDetails caseDetails) {
        return isHighCourtSelected(caseDetails) ? formGHighCourtTemplate : formGTemplate;
    }

    public String getContestedMiniFormTemplate(CaseDetails caseDetails) {
        return isHighCourtSelected(caseDetails) ? contestedMiniFormHighCourtTemplate : contestedMiniFormTemplate;
    }

    public String getContestedMiniFormTemplate(FinremCaseDetails caseDetails) {
        return isHighCourtSelected(caseDetails) ? contestedMiniFormHighCourtTemplate : contestedMiniFormTemplate;
    }

    public String getContestedMiniFormScheduleTemplate(FinremCaseDetails caseDetails) {
        return isHighCourtSelected(caseDetails) ? contestedMiniFormHighCourtScheduleTemplate : contestedMiniFormScheduleTemplate;
    }


    public String getApprovedConsentOrderTemplate(CaseDetails caseDetails) {
        return isHighCourtSelected(caseDetails) ? approvedConsentOrderHighCourtTemplate : approvedConsentOrderTemplate;
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

    public String getGeneralApplicationInterimHearingNoticeTemplate(CaseDetails caseDetails) {
        return isHighCourtSelected(caseDetails) ? generalApplicationInterimHearingNoticeHighCourtTemplate
            : generalApplicationInterimHearingNoticeTemplate;
    }

    public String getHearingNoticeConsentedTemplate(CaseDetails caseDetails) {
        return isHighCourtSelected(caseDetails) ? hearingNoticeConsentedHighCourtTemplate
            : hearingNoticeConsentedTemplate;
    }


    private boolean isHighCourtSelected(CaseDetails caseDetails) {
        if (caseDetails != null && caseDetails.getData() != null
            && caseDetails.getData().get(HIGHCOURT_COURTLIST) != null) {
            return true;
        }
        return false;
    }

    private boolean isHighCourtSelected(FinremCaseDetails caseDetails) {
        FinremCaseData caseData = caseDetails.getData();
        return ObjectUtils.isNotEmpty(caseData)
            && ObjectUtils.isNotEmpty(caseData.getRegionWrapper())
            && ObjectUtils.isNotEmpty(caseData.getRegionWrapper().getDefaultRegionWrapper())
            && ObjectUtils.isNotEmpty(caseData.getRegionWrapper().getDefaultRegionWrapper().getHighCourtFrcList())
            && caseData.getRegionWrapper().getDefaultRegionWrapper().getHighCourtFrcList()
            .getValue().equalsIgnoreCase(HIGHCOURT);
    }
}
