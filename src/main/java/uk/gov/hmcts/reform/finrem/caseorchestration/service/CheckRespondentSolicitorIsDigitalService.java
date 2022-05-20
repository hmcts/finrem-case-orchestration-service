package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrganisationPolicy;

import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_ORGANISATION_POLICY;

@Service
@Slf4j
public class CheckRespondentSolicitorIsDigitalService extends CheckSolicitorIsDigitalServiceBase {

    public CheckRespondentSolicitorIsDigitalService(CaseDataService caseDataService) {
        super(caseDataService);
    }

    @Override
    public boolean isSolicitorDigital(CaseDetails caseDetails) {
        Map<String, Object> caseData = caseDetails.getData();
        OrganisationPolicy respondentPolicy = getRespondentOrganisationPolicy(caseData);
        boolean isRespondentRepresented = getRespondentIsRepresented(caseData);

        if (respondentPolicy == null) {
            throw new IllegalStateException(String.format("Respondent Organisation Policy is null for caseId %s",
                caseDetails.getId()));
        }

        return Optional.ofNullable(respondentPolicy.getOrganisation()).isPresent() && isRespondentRepresented;
    }

    private OrganisationPolicy getRespondentOrganisationPolicy(Map<String, Object> caseData) {
        return new ObjectMapper().convertValue(caseData.get(RESPONDENT_ORGANISATION_POLICY),
            OrganisationPolicy.class);
    }

    private boolean getRespondentIsRepresented(Map<String, Object> caseData) {
        return caseDataService.isRespondentRepresentedByASolicitor(caseData);
    }
}

