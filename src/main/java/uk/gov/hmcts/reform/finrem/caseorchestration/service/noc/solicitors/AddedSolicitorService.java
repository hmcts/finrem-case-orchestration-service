package uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.solicitors;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangeOrganisationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangedRepresentative;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Organisation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrganisationPolicy;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_ORGANISATION_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NOC_PARTY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_ORGANISATION_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NoticeOfChangeParty.isApplicantForRepresentationChange;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService.nullToEmpty;

@Service
@Slf4j
@RequiredArgsConstructor
public class AddedSolicitorService {

    private final CaseDataService caseDataService;
    private final CheckApplicantSolicitorIsDigitalService checkApplicantSolicitorIsDigitalService;
    private final CheckRespondentSolicitorIsDigitalService checkRespondentSolicitorIsDigitalService;

    public ChangedRepresentative getAddedSolicitorAsSolicitor(UserDetails solicitorToAdd,
                                                              ChangeOrganisationRequest changeRequest) {
        return ChangedRepresentative.builder()
            .name(solicitorToAdd.getFullName())
            .email(solicitorToAdd.getEmail())
            .organisation(changeRequest.getOrganisationToAdd())
            .build();
    }

    @Deprecated
    public ChangedRepresentative getAddedSolicitorAsCaseworker(CaseDetails caseDetails) {
        final boolean isApplicant = ((String) caseDetails.getData().get(NOC_PARTY)).equalsIgnoreCase(APPLICANT);

        final String litigantOrgPolicy = isApplicant ? APPLICANT_ORGANISATION_POLICY : RESPONDENT_ORGANISATION_POLICY;

        if (caseDataService.isLitigantRepresented(caseDetails, isApplicant)
            && !isSolicitorDigital(caseDetails, isApplicant)) {
            return getAddedSolicitor(caseDetails, isApplicant, null);
        }

        return getChangedRepresentative(caseDetails, isApplicant, litigantOrgPolicy);
    }

    /**
     * Returns the solicitor added by a caseworker during a change of representation.
     *
     * <p>This method determines whether the change applies to the applicant or the respondent.
     * If the party is represented by a non-digital solicitor, it returns the newly added solicitor.
     * Otherwise, it returns the updated representative details based on the relevant
     * organisation policy (applicant or respondent).</p>
     *
     * @param finremCaseData the case data containing representation and policy details
     * @return the updated {@link ChangedRepresentative} for the affected party
     */
    public ChangedRepresentative getAddedSolicitorAsCaseworker(FinremCaseData finremCaseData) {
        final boolean isApplicant = isApplicantForRepresentationChange(finremCaseData);

        if (caseDataService.isLitigantRepresented(finremCaseData, isApplicant)
            && !isSolicitorDigital(finremCaseData, isApplicant)) {
            return getAddedSolicitor(finremCaseData, isApplicant, null);
        }

        return getChangedRepresentative(finremCaseData, isApplicant,
            Optional.of(isApplicant ? finremCaseData.getApplicantOrganisationPolicy()
                : finremCaseData.getRespondentOrganisationPolicy()));
    }

    @Deprecated
    private ChangedRepresentative getChangedRepresentative(CaseDetails caseDetails, boolean isApplicant, String litigantOrgPolicy) {

        return getOrgPolicy(caseDetails, litigantOrgPolicy)
            .map(OrganisationPolicy::getOrganisation)
            .map(organisation -> getAddedSolicitor(caseDetails, isApplicant, organisation))
            .orElse(null);
    }

    private ChangedRepresentative getChangedRepresentative(FinremCaseData finremCaseData, boolean isApplicant,
                                                           Optional<OrganisationPolicy> organisationPolicy) {

        return organisationPolicy
            .map(OrganisationPolicy::getOrganisation)
            .map(organisation -> getAddedSolicitor(finremCaseData, isApplicant, organisation))
            .orElse(null);
    }

    @Deprecated
    private Optional<OrganisationPolicy> getOrgPolicy(CaseDetails caseDetails, String orgPolicy) {
        return Optional.ofNullable(new ObjectMapper().convertValue(caseDetails.getData().get(orgPolicy),
            OrganisationPolicy.class));
    }

    @Deprecated
    private String getSolicitorName(CaseDetails caseDetails, boolean isApplicant) {
        return isApplicant
            ? getApplicantSolicitorName(caseDetails)
            : nullToEmpty(caseDetails.getData().get(RESP_SOLICITOR_NAME));
    }

    private String getSolicitorName(FinremCaseData finremCaseData, boolean isApplicant) {
        return isApplicant
            ? nullToEmpty(finremCaseData.getAppSolicitorName())
            : nullToEmpty(finremCaseData.getRespondentSolicitorName());
    }

    @Deprecated
    private String getApplicantSolicitorName(CaseDetails caseDetails) {
        Map<String, Object> caseData = caseDetails.getData();
        return caseDataService.isConsentedApplication(caseDetails)
            ? nullToEmpty(caseData.get(CONSENTED_SOLICITOR_NAME)) : nullToEmpty(caseData.get(CONTESTED_SOLICITOR_NAME));
    }

    @Deprecated
    private String getSolicitorEmail(CaseDetails caseDetails, boolean isApplicant) {
        return isApplicant
            ? getApplicantSolicitorEmail(caseDetails)
            : nullToEmpty(caseDetails.getData().get(RESP_SOLICITOR_EMAIL));
    }

    private String getSolicitorEmail(FinremCaseData finremCaseData, boolean isApplicant) {
        return isApplicant
            ? nullToEmpty(finremCaseData.getAppSolicitorEmail())
            : nullToEmpty(finremCaseData.getContactDetailsWrapper().getRespondentSolicitorEmail());
    }

    @Deprecated
    private String getApplicantSolicitorEmail(CaseDetails caseDetails) {
        Map<String, Object> caseData = caseDetails.getData();
        return caseDataService.isConsentedApplication(caseDetails)
            ? nullToEmpty(caseData.get(SOLICITOR_EMAIL)) : nullToEmpty(caseData.get(CONTESTED_SOLICITOR_EMAIL));
    }

    @Deprecated
    private boolean isSolicitorDigital(CaseDetails caseDetails, boolean isApplicant) {
        return isApplicant
            ? checkApplicantSolicitorIsDigitalService.isSolicitorDigital(caseDetails)
            : checkRespondentSolicitorIsDigitalService.isSolicitorDigital(caseDetails);
    }

    private boolean isSolicitorDigital(FinremCaseData finremCaseData, boolean isApplicant) {
        return isApplicant
            ? checkApplicantSolicitorIsDigitalService.isSolicitorDigital(finremCaseData)
            : checkRespondentSolicitorIsDigitalService.isSolicitorDigital(finremCaseData);
    }

    @Deprecated
    private ChangedRepresentative getAddedSolicitor(CaseDetails caseDetails,
                                                    boolean isApplicant,
                                                    Organisation organisation) {
        return ChangedRepresentative.builder()
            .name(getSolicitorName(caseDetails, isApplicant))
            .email(getSolicitorEmail(caseDetails, isApplicant))
            .organisation(organisation)
            .build();
    }

    private ChangedRepresentative getAddedSolicitor(FinremCaseData finremCaseData,
                                                    boolean isApplicant,
                                                    Organisation organisation) {
        return ChangedRepresentative.builder()
            .name(getSolicitorName(finremCaseData, isApplicant))
            .email(getSolicitorEmail(finremCaseData, isApplicant))
            .organisation(organisation)
            .build();
    }
}
