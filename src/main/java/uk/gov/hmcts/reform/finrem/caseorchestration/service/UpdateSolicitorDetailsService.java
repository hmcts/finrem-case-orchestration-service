package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Address;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.organisation.OrganisationsResponse;

import java.util.Map;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_SOLICITOR_FIRM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_FIRM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_REFERENCE;

@Service
@Slf4j
@RequiredArgsConstructor
public class UpdateSolicitorDetailsService {

    private final PrdOrganisationService organisationService;
    private final ObjectMapper objectMapper;
    private final CaseDataService caseDataService;

    public void setApplicantSolicitorOrganisationDetails(String authToken, CaseDetails caseDetails) {
        OrganisationsResponse organisationsResponse = organisationService.retrieveOrganisationsData(authToken);

        if (organisationsResponse != null) {
            if (caseDataService.isContestedApplication(caseDetails)) {
                caseDetails.getData().put(CONTESTED_SOLICITOR_ADDRESS, convertOrganisationAddressToSolicitorAddress(organisationsResponse));
                caseDetails.getData().put(CONTESTED_SOLICITOR_FIRM, organisationsResponse.getName());
            } else {
                caseDetails.getData().put(CONSENTED_SOLICITOR_ADDRESS, convertOrganisationAddressToSolicitorAddress(organisationsResponse));
                caseDetails.getData().put(CONSENTED_SOLICITOR_FIRM, organisationsResponse.getName());
            }

            caseDetails.getData().put(SOLICITOR_REFERENCE, organisationsResponse.getOrganisationIdentifier());
        }
    }

    public Map<String, Object> convertOrganisationAddressToSolicitorAddress(OrganisationsResponse organisationData) {
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
