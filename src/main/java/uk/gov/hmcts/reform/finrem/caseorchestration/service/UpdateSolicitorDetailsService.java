package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Address;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.organisation.OrganisationsResponse;

import java.util.Map;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_ADDRESS;

@Service
@RequiredArgsConstructor
public class UpdateSolicitorDetailsService {

    private final OrganisationService organisationService;

    public void updateApplicantSolicitorAddressFromPRD(CaseDetails caseDetails, String authToken){
        OrganisationsResponse organisationData = organisationService.retrieveOrganisationsData(authToken);

        caseDetails.getData().put(CONTESTED_SOLICITOR_ADDRESS, convertOrganisationAddressToSolicitorAddress(organisationData));
    }

    private Map convertOrganisationAddressToSolicitorAddress (OrganisationsResponse organisationData) {
        return new ObjectMapper().convertValue(Address.builder()
            .addressLine1(organisationData.getContactInformation().getAddressLine1())
            .addressLine2(organisationData.getContactInformation().getAddressLine2())
            .addressLine3(organisationData.getContactInformation().getAddressLine3())
            .county(organisationData.getContactInformation().getCounty())
            .country(organisationData.getContactInformation().getCountry())
            .postTown(organisationData.getContactInformation().getTownCity())
            .postCode(organisationData.getContactInformation().getPostcode())
            .build(), Map.class);
    }
}
