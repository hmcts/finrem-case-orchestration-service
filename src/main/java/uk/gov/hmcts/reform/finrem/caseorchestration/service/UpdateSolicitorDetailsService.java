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

    public Map<String, Object> getApplicantSolicitorAddressFromPrd(String authToken) {
        return convertOrganisationAddressToSolicitorAddress(organisationService.retrieveOrganisationsData(authToken));
    }

    private Map<String, Object> convertOrganisationAddressToSolicitorAddress(OrganisationsResponse organisationData) {
        return objectMapper.convertValue(Address.builder()
            .addressLine1(organisationData.getContactInformation().get(0).getAddressLine1())
            .addressLine2(organisationData.getContactInformation().get(0).getAddressLine2())
            .addressLine3(organisationData.getContactInformation().get(0).getAddressLine3())
            .county(organisationData.getContactInformation().get(0).getCounty())
            .country(organisationData.getContactInformation().get(0).getCountry())
            .postTown(organisationData.getContactInformation().get(0).getTownCity())
            .postCode(organisationData.getContactInformation().get(0).getPostcode())
            .build(), Map.class);
    }
}
