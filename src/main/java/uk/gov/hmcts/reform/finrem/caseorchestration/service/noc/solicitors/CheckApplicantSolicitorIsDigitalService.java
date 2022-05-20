package uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.solicitors;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrganisationPolicy;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;

import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_ORGANISATION_POLICY;

@Service
@Slf4j
public class CheckApplicantSolicitorIsDigitalService extends CheckSolicitorIsDigitalServiceBase {

    @Autowired
    public CheckApplicantSolicitorIsDigitalService(CaseDataService caseDataService) {
        super(caseDataService);
    }

    @Override
    public boolean isSolicitorDigital(CaseDetails caseDetails) {
        Map<String, Object> caseData = caseDetails.getData();
        OrganisationPolicy applicantPolicy = getApplicantOrganisationPolicy(caseData);
        boolean isApplicantRepresented = getApplicantIsRepresented(caseData);

        if (applicantPolicy == null) {
            throw new IllegalStateException(String.format("Applicant Organisation Policy is null for caseId %s",
                caseDetails.getId()));
        }

        return Optional.ofNullable(applicantPolicy.getOrganisation()).isPresent() && isApplicantRepresented;
    }

    private OrganisationPolicy getApplicantOrganisationPolicy(Map<String, Object> caseData) {
        return new ObjectMapper().convertValue(caseData.get(APPLICANT_ORGANISATION_POLICY),
            OrganisationPolicy.class);
    }

    private boolean getApplicantIsRepresented(Map<String, Object> caseData) {
        return caseDataService.isApplicantRepresentedByASolicitor(caseData);
    }
}
