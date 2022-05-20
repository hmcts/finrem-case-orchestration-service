package uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.solicitors;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangeOrganisationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangedRepresentative;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Organisation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrganisationPolicy;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;

import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_ORGANISATION_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_SOLICITOR_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_RESPONDENT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_RESPONDENT_REPRESENTED;
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
        final boolean isApplicant = changeRequest.getCaseRoleId().getValueCode().equals(APP_SOLICITOR_POLICY);

        if (!isLitigantRepresented(caseDetails, isApplicant)) {
            return null;
        }

        if (!isSolicitorDigital(caseDetails, isApplicant)) {
            return getNonDigitalRemovedRepresentative(caseDetails, isApplicant);
        }

        return getDigitalRemovedRepresentative(caseDetails, isApplicant, changeRequest);
    }

    public ChangedRepresentative getRemovedSolicitorAsCaseworker(CaseDetails caseDetails, final boolean isApplicant) {
        Map<String, Object> caseData = caseDetails.getData();

        final String litigantOrgPolicy = isApplicant ? APPLICANT_ORGANISATION_POLICY : RESPONDENT_ORGANISATION_POLICY;

        if (caseData.get(getLitigantRepresentedKey(caseDetails, isApplicant)).equals(YES_VALUE)) {
            return ChangedRepresentative.builder()
                .name(getSolicitorName(caseDetails, isApplicant))
                .email(getSolicitorEmail(caseDetails, isApplicant))
                .organisation(getRemovedOrganisation(caseDetails, litigantOrgPolicy, isApplicant))
                .build();
        }

        return null;
    }

    private boolean isLitigantRepresented(CaseDetails caseDetails, boolean isApplicant) {
        String isRepresented = nullToEmpty(caseDetails.getData().get(getLitigantRepresentedKey(caseDetails, isApplicant)));

        return YES_VALUE.equalsIgnoreCase(isRepresented);
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

    private String getSolicitorEmail(CaseDetails caseDetails, boolean isApplicant) {
        return isApplicant
            ? getApplicantSolicitorEmail(caseDetails)
            : nullToEmpty(caseDetails.getData().get(RESP_SOLICITOR_EMAIL));
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

    private String getLitigantRepresentedKey(CaseDetails caseDetails, boolean isApplicant) {
        return isApplicant ? APPLICANT_REPRESENTED : getRespondentRepresentedKey(caseDetails);
    }

    private String getRespondentRepresentedKey(CaseDetails caseDetails) {
        return caseDataService.isConsentedApplication(caseDetails)
            ? CONSENTED_RESPONDENT_REPRESENTED : CONTESTED_RESPONDENT_REPRESENTED;
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
