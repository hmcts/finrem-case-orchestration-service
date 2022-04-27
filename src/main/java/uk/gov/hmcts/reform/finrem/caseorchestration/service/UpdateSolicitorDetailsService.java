package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Address;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangedRepresentative;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.organisation.OrganisationsResponse;

import java.util.Map;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_SOLICITOR_AGREE_TO_RECEIVE_EMAILS_CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_SOLICITOR_AGREE_TO_RECEIVE_EMAILS_CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_SOLICITOR_DX_NUMBER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_SOLICITOR_FIRM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_DX_NUMBER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_FIRM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_DX_NUMBER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_FIRM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_NOTIFICATIONS_EMAIL_CONSENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_PHONE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_PHONE;
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

    public Map<String, Object> updateSolicitorContactDetails(ChangedRepresentative addedSolicitor,
                                                             Map<String, Object> caseData,
                                                             boolean isConsented,
                                                             boolean isApplicant) {
        if (isApplicant) {
            updateAppSolFields(caseData, isConsented, addedSolicitor);
        } else {
            updateRespSolFields(caseData, addedSolicitor);
        }

        return caseData;
    }

    public Map<String, Object> removeSolicitorFields(Map<String, Object> caseData,
                                                      boolean isConsented,
                                                      boolean isApplicant) {
        if (isApplicant) {
            removeAppSolFields(caseData, isConsented);
        } else {
            removeRespSolFields(caseData);
        }

        return caseData;
    }

    private void updateAppSolFields(Map<String, Object> caseData,
                                    boolean isConsented,
                                    ChangedRepresentative addedSolicitor) {
        caseData.put(isConsented ? CONSENTED_SOLICITOR_NAME : CONTESTED_SOLICITOR_NAME,
            addedSolicitor.getName());
        caseData.put(isConsented ? SOLICITOR_EMAIL : CONTESTED_SOLICITOR_EMAIL, addedSolicitor.getEmail());
        caseData.put(isConsented ? CONSENTED_SOLICITOR_FIRM : CONTESTED_SOLICITOR_FIRM,
            addedSolicitor.getOrganisation().getOrganisationName());
    }

    private void removeAppSolFields(Map<String, Object> caseData,
                                    boolean isConsented) {
        caseData.remove(SOLICITOR_PHONE);
        caseData.remove(isConsented ? CONSENTED_SOLICITOR_DX_NUMBER : CONTESTED_SOLICITOR_DX_NUMBER);
        caseData.remove(isConsented ? APP_SOLICITOR_AGREE_TO_RECEIVE_EMAILS_CONSENTED
            : APP_SOLICITOR_AGREE_TO_RECEIVE_EMAILS_CONTESTED);
    }

    private void updateRespSolFields(Map<String, Object> caseData,
                                     ChangedRepresentative addedSolicitor) {
        caseData.put(RESP_SOLICITOR_NAME, addedSolicitor.getName());
        caseData.put(RESP_SOLICITOR_EMAIL, addedSolicitor.getEmail());
        caseData.put(RESP_SOLICITOR_FIRM, addedSolicitor.getOrganisation().getOrganisationName());
    }

    private void removeRespSolFields(Map<String, Object> caseData) {
        caseData.remove(RESP_SOLICITOR_PHONE);
        caseData.remove(RESP_SOLICITOR_DX_NUMBER);
        caseData.remove(RESP_SOLICITOR_NOTIFICATIONS_EMAIL_CONSENT);
    }
}
