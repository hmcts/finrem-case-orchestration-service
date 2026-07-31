package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;

import java.time.LocalDate;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EstimatedAssetsChecklistVersion.V2;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EstimatedAssetsChecklistVersion.V3;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_ORDER_APPROVED_DATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_ORDER_APPROVED_JUDGE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrganisationPolicy.getDefaultOrganisationPolicy;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Schedule1OrMatrimonialAndCpList.MATRIMONIAL_AND_CIVIL_PARTNERSHIP_PROCEEDINGS;

@Service
@RequiredArgsConstructor
public class OnStartDefaultValueService {

    private final FeatureToggleService featureToggleService;

    private final IdamService idamService;

    /**
     * Sets the applicant's organisation policy to the default policy
     * for the applicant solicitor case role.
     *
     * @param callbackRequest the callback request containing the case data to update
     */
    public void defaultApplicantOrganisationPolicy(FinremCallbackRequest callbackRequest) {
        callbackRequest.getFinremCaseData().setApplicantOrganisationPolicy(
            getDefaultOrganisationPolicy(CaseRole.APP_SOLICITOR)
        );
    }

    /**
     * Sets the respondent's organisation policy to the default policy
     * for the respondent solicitor case role.
     *
     * @param callbackRequest the callback request containing the case data to update
     */
    public void defaultRespondentOrganisationPolicy(FinremCallbackRequest callbackRequest) {
        callbackRequest.getFinremCaseData().setRespondentOrganisationPolicy(
            getDefaultOrganisationPolicy(CaseRole.RESP_SOLICITOR)
        );
    }

    public void defaultCivilPartnershipField(FinremCallbackRequest callbackRequest) {
        FinremCaseData caseData = callbackRequest.getCaseDetails().getData();
        if (caseData.getCivilPartnership() == null) {
            caseData.setCivilPartnership(YesOrNo.NO);
        }
    }

    public void defaultUrgencyQuestion(FinremCallbackRequest callbackRequest) {
        FinremCaseData caseData = callbackRequest.getCaseDetails().getData();
        if (caseData.getPromptForUrgentCaseQuestion() == null) {
            caseData.setPromptForUrgentCaseQuestion(YesOrNo.NO);
        }
    }

    public void defaultTypeOfApplication(FinremCallbackRequest callbackRequest) {
        FinremCaseData caseData = callbackRequest.getCaseDetails().getData();
        if (caseData.getScheduleOneWrapper().getTypeOfApplication() == null) {
            caseData.getScheduleOneWrapper().setTypeOfApplication(MATRIMONIAL_AND_CIVIL_PARTNERSHIP_PROCEEDINGS);
        }
    }

    public void defaultIssueDate(FinremCallbackRequest callbackRequest) {
        FinremCaseData data = callbackRequest.getCaseDetails().getData();
        if (data.getIssueDate() == null) {
            data.setIssueDate(LocalDate.now());
        }
    }

    /**
     * Sets the consented order direction judge name using the surname
     * retrieved from the authenticated IDAM user.
     *
     * @param finremCaseData the case data to update with the judge name
     * @param userAuthorisation the user authorisation token used to retrieve the IDAM user details
     */
    public void defaultConsentedOrderJudgeName(FinremCaseData finremCaseData, String userAuthorisation) {
        finremCaseData.setOrderDirectionJudgeName(idamService.getIdamSurname(userAuthorisation));
    }

    public void defaultContestedOrderJudgeName(CallbackRequest callbackRequest, String userAuthorisation) {
        callbackRequest.getCaseDetails().getData().put(CONTESTED_ORDER_APPROVED_JUDGE_NAME,
            idamService.getIdamSurname(userAuthorisation));
    }

    /**
     * Sets the consented order direction date to the current system date.
     *
     * @param finremCaseData the case data to update with the current order direction date
     */
    public void defaultConsentedOrderDate(FinremCaseData finremCaseData) {
        finremCaseData.setOrderDirectionDate(LocalDate.now());
    }

    public void defaultContestedOrderDate(CallbackRequest callbackRequest) {
        callbackRequest.getCaseDetails().getData().putIfAbsent(CONTESTED_ORDER_APPROVED_DATE, LocalDate.now());
    }

    /**
     * This method sets the version of the Estimated Assets Checklist to be used in the case data based on a feature toggle.
     * Since these are new cases, we want to use the new version of the checklist once the feature toggle is enabled.
     * So, the feature toggle is enabled, the new case uses the V3 list; otherwise the new case will use the V2 list.
     *
     *
     * @param callbackRequest The callback request containing the case data.
     */
    public void setEstimatedAssetsChecklistVersion(FinremCallbackRequest callbackRequest) {
        boolean useV3EstimatedAssetsChecklist = featureToggleService.isEstimatedAssetsChecklistV3Enabled();
        FinremCaseData caseData = callbackRequest.getFinremCaseData();
        if (useV3EstimatedAssetsChecklist) {
            caseData.getEstimatedAssetsChecklistWrapper().setEstimatedAssetsChecklistVersion(V3);
        } else {
            caseData.getEstimatedAssetsChecklistWrapper().setEstimatedAssetsChecklistVersion(V2);
        }
    }
}
