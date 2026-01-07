package uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.solicitors;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangeOrganisationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangedRepresentative;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Organisation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrganisationPolicy;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;

import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_ORGANISATION_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_SOLICITOR_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_ORGANISATION_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NoticeOfChangeParty.isApplicantForRepresentationChange;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NoticeOfChangeParty.isRespondentForRepresentationChange;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService.nullToEmpty;

@Service
@Slf4j
public class RemovedSolicitorService {

    private final CaseDataService caseDataService;
    private final CheckApplicantSolicitorIsDigitalService checkApplicantSolicitorIsDigitalService;
    private final CheckRespondentSolicitorIsDigitalService checkRespondentSolicitorIsDigitalService;

    @Autowired
    public RemovedSolicitorService(CaseDataService caseDataService,
                                   CheckApplicantSolicitorIsDigitalService checkApplicantSolicitorIsDigitalService,
                                   CheckRespondentSolicitorIsDigitalService checkRespondentSolicitorIsDigitalService) {
        this.caseDataService = caseDataService;
        this.checkApplicantSolicitorIsDigitalService = checkApplicantSolicitorIsDigitalService;
        this.checkRespondentSolicitorIsDigitalService = checkRespondentSolicitorIsDigitalService;
    }

    public ChangedRepresentative getRemovedSolicitorAsSolicitor(CaseDetails caseDetails,
                                                                ChangeOrganisationRequest changeRequest) {
        log.info("Remove solicitor for Case ID: {}", caseDetails.getId());
        final boolean isApplicant = changeRequest.getCaseRoleId() != null
            && changeRequest.getCaseRoleId().getValueCode().equals(APP_SOLICITOR_POLICY);

        if (!caseDataService.isLitigantRepresented(caseDetails, isApplicant)) {
            return null;
        }

        if (!isSolicitorDigital(caseDetails, isApplicant)) {
            return getNonDigitalRemovedRepresentative(caseDetails, isApplicant);
        }

        return getDigitalRemovedRepresentative(caseDetails, isApplicant, changeRequest);
    }

    @Deprecated
    public ChangedRepresentative getRemovedSolicitorAsCaseworker(CaseDetails caseDetails, final boolean isApplicant) {

        final String litigantOrgPolicy = isApplicant ? APPLICANT_ORGANISATION_POLICY : RESPONDENT_ORGANISATION_POLICY;

        if (caseDataService.isLitigantRepresented(caseDetails, isApplicant)) {
            return ChangedRepresentative.builder()
                .name(getSolicitorName(caseDetails, isApplicant))
                .email(getSolicitorEmail(caseDetails, isApplicant))
                .organisation(getRemovedOrganisation(caseDetails, litigantOrgPolicy, isApplicant))
                .build();
        }

        return null;
    }

    /**
     * Determines the representative who has been removed during a change of representation.
     *
     * <p>
     * This method compares the updated {@code finremCaseData} with the original
     * {@code originalFinremCaseData} to identify whether the Applicant or Respondent
     * has changed representative. It checks which party the change applies to and then
     * retrieves the corresponding original organisation policy.
     * </p>
     *
     * <p>
     * If the relevant party was previously represented, the method builds and returns a
     * {@link ChangedRepresentative} object containing the removed solicitor's name,
     * email, and organisation. If no change applies or the party was not represented,
     * the method returns {@code null}.
     * </p>
     *
     * @param finremCaseData          the updated case data after the change
     * @param originalFinremCaseData  the case data before the change
     * @return a {@code ChangedRepresentative} describing the removed representative,
     *         or {@code null} if no representative was removed
     */
    public ChangedRepresentative getChangedRepresentative(FinremCaseData finremCaseData,
                                                          FinremCaseData originalFinremCaseData) {
        OrganisationPolicy organisationPolicy = null;

        Boolean isApplicant = null;
        if (isApplicantForRepresentationChange(finremCaseData)) {
            organisationPolicy = originalFinremCaseData.getApplicantOrganisationPolicy();
            isApplicant = true;
        } else if (isRespondentForRepresentationChange(finremCaseData)) {
            organisationPolicy = originalFinremCaseData.getRespondentOrganisationPolicy();
            isApplicant = false;
        }

        if (isApplicant == null) {
            return null;
        }
        if (organisationPolicy == null) {
            log.info("{} - No applicant or respondent organisation policy provided", finremCaseData.getCcdCaseId());
            return null;
        }

        boolean wasLitigantRepresented = caseDataService.isLitigantRepresented(originalFinremCaseData, isApplicant);
        if (wasLitigantRepresented) {
            return ChangedRepresentative.builder()
                .name(getSolicitorName(originalFinremCaseData, isApplicant))
                .email(getSolicitorEmail(originalFinremCaseData, isApplicant))
                .organisation(organisationPolicy.getOrganisation())
                .build();
        }
        return null;
    }

    private boolean isSolicitorDigital(CaseDetails caseDetails, boolean isApplicant) {
        final CheckSolicitorIsDigitalServiceBase checkSolicitorIsDigitalService = isApplicant
            ? checkApplicantSolicitorIsDigitalService : checkRespondentSolicitorIsDigitalService;

        return checkSolicitorIsDigitalService.isSolicitorDigital(caseDetails);
    }

    private ChangedRepresentative getNonDigitalRemovedRepresentative(CaseDetails caseDetails, boolean isApplicant) {
        return ChangedRepresentative.builder()
            .name(getSolicitorName(caseDetails, isApplicant))
            .email(getSolicitorEmail(caseDetails, isApplicant))
            .organisation(null)
            .build();
    }

    private ChangedRepresentative getDigitalRemovedRepresentative(CaseDetails caseDetails,
                                                                  boolean isApplicant,
                                                                  ChangeOrganisationRequest changeRequest) {
        return Optional.ofNullable(changeRequest.getOrganisationToRemove())
            .map(org -> ChangedRepresentative.builder()
                .name(getSolicitorName(caseDetails, isApplicant))
                .email(getSolicitorEmail(caseDetails, isApplicant))
                .organisation(org).build())
            .orElse(null);
    }

    private String getSolicitorName(CaseDetails caseDetails, boolean isApplicant) {
        return isApplicant
            ? getApplicantSolicitorName(caseDetails)
            : nullToEmpty(caseDetails.getData().get(RESP_SOLICITOR_NAME));
    }

    private String getSolicitorName(FinremCaseData finremCaseData, boolean isApplicant) {
        if (isApplicant) {
            return nullToEmpty(finremCaseData.getAppSolicitorName());
        } else {
            return nullToEmpty(finremCaseData.getContactDetailsWrapper().getRespondentSolicitorName());
        }
    }

    private String getSolicitorEmail(CaseDetails caseDetails, boolean isApplicant) {
        return isApplicant
            ? getApplicantSolicitorEmail(caseDetails)
            : nullToEmpty(caseDetails.getData().get(RESP_SOLICITOR_EMAIL));
    }

    private String getSolicitorEmail(FinremCaseData finremCaseData, boolean isApplicant) {
        if (isApplicant) {
            return finremCaseData.getAppSolicitorEmail();
        } else {
            return nullToEmpty(finremCaseData.getContactDetailsWrapper().getRespondentSolicitorEmail());
        }
    }

    private String getApplicantSolicitorName(CaseDetails caseDetails) {
        Map<String, Object> caseData = caseDetails.getData();
        return caseDataService.isConsentedApplication(caseDetails)
            ? (String) caseData.get(CONSENTED_SOLICITOR_NAME) : (String) caseData.get(CONTESTED_SOLICITOR_NAME);
    }

    private String getApplicantSolicitorEmail(CaseDetails caseDetails) {
        Map<String, Object> caseData = caseDetails.getData();
        return caseDataService.isConsentedApplication(caseDetails)
            ? (String) caseData.get(SOLICITOR_EMAIL) : (String) caseData.get(CONTESTED_SOLICITOR_EMAIL);
    }

    private Organisation getRemovedOrganisation(CaseDetails caseDetails,
                                                String orgPolicy,
                                                boolean isApplicant) {
        final OrganisationPolicy organisationPolicy = new ObjectMapper()
            .convertValue(caseDetails.getData().get(orgPolicy),
                OrganisationPolicy.class);

        if (!isSolicitorDigital(caseDetails, isApplicant)) {
            return null;
        }

        return Optional.ofNullable(organisationPolicy).map(OrganisationPolicy::getOrganisation).orElse(null);
    }
}
