package uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.solicitors;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangeOrganisationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangedRepresentative;
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

    public ChangedRepresentative getAddedSolicitorAsCaseworker(CaseDetails caseDetails) {
        final boolean isApplicant = ((String) caseDetails.getData().get(NOC_PARTY)).equalsIgnoreCase(APPLICANT);
        final String litigantOrgPolicy = isApplicant ? APPLICANT_ORGANISATION_POLICY : RESPONDENT_ORGANISATION_POLICY;

        if (!isSolicitorDigital(caseDetails, isApplicant)) {
            return getAddedSolicitor(caseDetails, isApplicant, null);
        }

        return Optional.ofNullable(getOrgPolicy(caseDetails, litigantOrgPolicy).getOrganisation())
            .map(organisation -> getAddedSolicitor(caseDetails, isApplicant, organisation))
            .orElse(null);
    }

    private OrganisationPolicy getOrgPolicy(CaseDetails caseDetails, String orgPolicy) {
        return new ObjectMapper().convertValue(caseDetails.getData().get(orgPolicy),
            OrganisationPolicy.class);
    }

    private String getSolicitorName(CaseDetails caseDetails, boolean isApplicant) {
        return isApplicant
            ? getApplicantSolicitorName(caseDetails)
            : nullToEmpty(caseDetails.getData().get(RESP_SOLICITOR_NAME));
    }

    private String getApplicantSolicitorName(CaseDetails caseDetails) {
        Map<String, Object> caseData = caseDetails.getData();
        return caseDataService.isConsentedApplication(caseDetails)
            ? nullToEmpty(caseData.get(CONSENTED_SOLICITOR_NAME)) : nullToEmpty(caseData.get(CONTESTED_SOLICITOR_NAME));
    }

    private String getSolicitorEmail(CaseDetails caseDetails, boolean isApplicant) {
        return isApplicant
            ? getApplicantSolicitorEmail(caseDetails)
            : nullToEmpty(caseDetails.getData().get(RESP_SOLICITOR_EMAIL));
    }

    private String getApplicantSolicitorEmail(CaseDetails caseDetails) {
        Map<String, Object> caseData = caseDetails.getData();
        return caseDataService.isConsentedApplication(caseDetails)
            ? nullToEmpty(caseData.get(SOLICITOR_EMAIL)) : nullToEmpty(caseData.get(CONTESTED_SOLICITOR_EMAIL));
    }

    private boolean isSolicitorDigital(CaseDetails caseDetails, boolean isApplicant) {
        return isApplicant
            ? checkApplicantSolicitorIsDigitalService.isSolicitorDigital(caseDetails)
            : checkRespondentSolicitorIsDigitalService.isSolicitorDigital(caseDetails);
    }

    private ChangedRepresentative getAddedSolicitor(CaseDetails caseDetails,
                                                    boolean isApplicant,
                                                    Organisation organisation) {
        return ChangedRepresentative.builder()
            .name(getSolicitorName(caseDetails, isApplicant))
            .email(getSolicitorEmail(caseDetails, isApplicant))
            .organisation(organisation)
            .build();

    }
}
