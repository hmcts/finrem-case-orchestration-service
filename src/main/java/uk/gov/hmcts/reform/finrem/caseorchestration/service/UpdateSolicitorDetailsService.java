package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Address;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.organisation.OrganisationsResponse;

import java.util.Map;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_FIRM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ORGANISATION_POLICY_APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ORGANISATION_POLICY_ORGANISATION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ORGANISATION_POLICY_ORGANISATION_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ORGANISATION_POLICY_RESPONDENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_FIRM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_REFERENCE;

@Service
@Slf4j
@RequiredArgsConstructor()
public class UpdateSolicitorDetailsService {

    private final PrdOrganisationService organisationService;
    private final ObjectMapper objectMapper;

    public void setApplicantSolicitorOrganisationDetails(CaseDetails caseDetails) {
        OrganisationsResponse organisationsResponse = retrieveOrganisationsData(caseDetails, ORGANISATION_POLICY_APPLICANT);

        if (organisationsResponse != null) {
            caseDetails.getData().put(CONTESTED_SOLICITOR_ADDRESS, convertOrganisationAddressToSolicitorAddress(organisationsResponse));
            caseDetails.getData().put(CONTESTED_SOLICITOR_FIRM, organisationsResponse.getName());
            caseDetails.getData().put(SOLICITOR_REFERENCE, organisationsResponse.getOrganisationIdentifier());
        }
    }

    public void setRespondentSolicitorOrganisationDetails(CaseDetails caseDetails) {
        OrganisationsResponse organisationsResponse = retrieveOrganisationsData(caseDetails, ORGANISATION_POLICY_RESPONDENT);

        if (organisationsResponse != null) {
            caseDetails.getData().put(RESP_SOLICITOR_ADDRESS, convertOrganisationAddressToSolicitorAddress(organisationsResponse));
            caseDetails.getData().put(RESP_SOLICITOR_FIRM, organisationsResponse.getName());
            caseDetails.getData().put(RESP_SOLICITOR_REFERENCE, organisationsResponse.getOrganisationIdentifier());
        }
    }

    private OrganisationsResponse retrieveOrganisationsData(CaseDetails caseDetails, String orgPolicyFieldName) {
        Map<String, Object> orgPolicy = (Map<String, Object>) caseDetails.getData().get(orgPolicyFieldName);
        Map<String, Object> org = (Map<String, Object>) orgPolicy.get(ORGANISATION_POLICY_ORGANISATION);
        return organisationService.retrieveOrganisationsData((String) org.get(ORGANISATION_POLICY_ORGANISATION_ID));
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
