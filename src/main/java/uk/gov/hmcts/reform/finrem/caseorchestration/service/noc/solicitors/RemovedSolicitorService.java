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
     * Returns the solicitor to be removed for the caseworker, based on whether the
     * applicant or respondent is selected.
     *
     * <p>
     * If the selected litigant is currently represented, this method builds and returns a
     * {@link ChangedRepresentative} containing the solicitor’s name, email, and the
     * organisation being removed. If the litigant is not represented, it returns {@code null}.
     *
     * @param finremCaseData  the current case data
     * @param isApplicant     true if the removal applies to the applicant; false if it applies to the respondent
     * @return the solicitor details to remove, or null if the litigant is not represented
     */
    public ChangedRepresentative getRemovedSolicitorAsCaseworker(FinremCaseData finremCaseData, boolean isApplicant) {
        final OrganisationPolicy organisationPolicy = isApplicant ? finremCaseData.getApplicantOrganisationPolicy()
            : finremCaseData.getRespondentOrganisationPolicy();

        if (caseDataService.isLitigantRepresented(finremCaseData, isApplicant)) {
            return ChangedRepresentative.builder()
                .name(getSolicitorName(finremCaseData, isApplicant))
                .email(getSolicitorEmail(finremCaseData, isApplicant))
                .organisation(getRemovedOrganisation(finremCaseData, organisationPolicy, isApplicant))
                .build();
        }

        return null;
    }

    private boolean isSolicitorDigital(CaseDetails caseDetails, boolean isApplicant) {
        final CheckSolicitorIsDigitalServiceBase checkSolicitorIsDigitalService = isApplicant
            ? checkApplicantSolicitorIsDigitalService : checkRespondentSolicitorIsDigitalService;

        return checkSolicitorIsDigitalService.isSolicitorDigital(caseDetails);
    }

    private boolean isSolicitorDigital(FinremCaseData finremCaseData, boolean isApplicant) {
        if (isApplicant) {
            return checkApplicantSolicitorIsDigitalService.isSolicitorDigital(finremCaseData);
        } else {
            return checkRespondentSolicitorIsDigitalService.isSolicitorDigital(finremCaseData);
        }
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
        return isApplicant
            ? nullToEmpty(finremCaseData.getAppSolicitorName())
            : nullToEmpty(finremCaseData.getContactDetailsWrapper().getRespondentSolicitorName());
    }

    private String getSolicitorEmail(CaseDetails caseDetails, boolean isApplicant) {
        return isApplicant
            ? getApplicantSolicitorEmail(caseDetails)
            : nullToEmpty(caseDetails.getData().get(RESP_SOLICITOR_EMAIL));
    }

    private String getSolicitorEmail(FinremCaseData finremCaseData, boolean isApplicant) {
        return isApplicant
            ? finremCaseData.getAppSolicitorEmail()
            : nullToEmpty(finremCaseData.getContactDetailsWrapper().getRespondentSolicitorEmail());
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

    private Organisation getRemovedOrganisation(FinremCaseData finremCaseData,
                                                OrganisationPolicy organisationPolicy,
                                                boolean isApplicant) {
        if (!isSolicitorDigital(finremCaseData, isApplicant)) {
            return null;
        }

        return Optional.ofNullable(organisationPolicy).map(OrganisationPolicy::getOrganisation).orElse(null);
    }
}
