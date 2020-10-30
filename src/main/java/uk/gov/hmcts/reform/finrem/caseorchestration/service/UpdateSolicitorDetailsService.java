package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Address;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.organisation.OrganisationsResponse;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class UpdateSolicitorDetailsService {

    private final PrdOrganisationService organisationService;
    private final ObjectMapper objectMapper;

    public Map<String, Object> updateApplicantSolicitorAddressFromPrd(String authToken) {
        OrganisationsResponse organisationData = organisationService.retrieveOrganisationsData(authToken);

        return convertOrganisationAddressToSolicitorAddress(organisationData);
    }

    private Map<String, Object> convertOrganisationAddressToSolicitorAddress(OrganisationsResponse organisationData) {
        return objectMapper.convertValue(Address.builder()
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
