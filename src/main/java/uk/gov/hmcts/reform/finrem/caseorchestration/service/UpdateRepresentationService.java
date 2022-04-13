package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.Solicitor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangeOrganisationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Organisation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrganisationPolicy;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.events.AuditEvent;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.organisation.OrganisationsResponse;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_ORGANISATION_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_SOLICITOR_AGREE_TO_RECEIVE_EMAILS_CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_SOLICITOR_AGREE_TO_RECEIVE_EMAILS_CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_SOLICITOR_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_RESPONDENT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_SOLICITOR_DX_NUMBER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_SOLICITOR_FIRM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_RESPONDENT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_DX_NUMBER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_FIRM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_ORGANISATION_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_DX_NUMBER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_FIRM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_NOTIFICATIONS_EMAIL_CONSENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_PHONE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_PHONE;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UpdateRepresentationService {

    private final AuditEventService auditEventService;
    private final IdamClient idamClient;
    private final ObjectMapper objectMapper;
    private final CaseDataService caseDataService;
    private final PrdOrganisationService organisationService;
    private final UpdateSolicitorDetailsService updateSolicitorDetailsService;

    private static final String CHANGE_REQUEST_FIELD = "changeOrganisationRequestField";
    private static final String NOC_EVENT = "nocRequest";

    private boolean isApplicant;

    public Map<String, Object> updateRepresentationAsSolicitor(CaseDetails caseDetails,
                                                               String authToken) {
        Map<String, Object> caseData = caseDetails.getData();

        AuditEvent auditEvent = auditEventService.getLatestAuditEventByName(caseDetails.getId().toString(), NOC_EVENT)
            .orElseThrow(() -> new IllegalStateException(String.format("Could not find %s event in audit", NOC_EVENT)));

        UserDetails solicitorToAdd = idamClient.getUserByUserId(authToken, auditEvent.getUserId());

        ChangeOrganisationRequest change = getChangeOrganisationRequest(caseDetails);
        isApplicant = change.getCaseRoleId().getValueCode().equals(APP_SOLICITOR_POLICY);

        Solicitor addedSolicitor = Solicitor.builder()
            .name(solicitorToAdd.getFullName())
            .email(solicitorToAdd.getEmail())
            .organisation(change.getOrganisationToAdd())
            .build();

        Solicitor removedSolicitor = null;

        if (Optional.ofNullable(change.getOrganisationToRemove()).isPresent()) {
            removedSolicitor = Solicitor.builder()
                .name(isApplicant ? getApplicantSolicitorName(caseDetails) : (String) caseData.get(RESP_SOLICITOR_NAME))
                .email(isApplicant ? getApplicantSolicitorEmail(caseDetails) : (String) caseData.get(RESP_SOLICITOR_EMAIL))
                .organisation(getSolicitorOrganisation(caseDetails))
                .build();
        }

        //Generate ChangeOfRepresentatives Here...
        //some code
        caseDetails.setData(updateCaseDataWithNewSolDetails(caseDetails,
            addedSolicitor,
            authToken));

        //Call ApplyNoCDecision here
        return caseData;
    }

    private ChangeOrganisationRequest getChangeOrganisationRequest(CaseDetails caseDetails) {
        Map<String, Object> caseData = caseDetails.getData();

        return objectMapper.convertValue(caseData.get(CHANGE_REQUEST_FIELD),
            ChangeOrganisationRequest.class);
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


    private Organisation getSolicitorOrganisation(CaseDetails caseDetails) {
        Map<String, Object> caseData = caseDetails.getData();
        return isApplicant
            ? objectMapper.convertValue(caseData.get(APPLICANT_ORGANISATION_POLICY),
            OrganisationPolicy.class).getOrganisation()
            : objectMapper.convertValue(caseData.get(RESPONDENT_ORGANISATION_POLICY),
            OrganisationPolicy.class).getOrganisation();
    }

    private String getRespondentRepresentedKey(CaseDetails caseDetails) {
        return caseDataService.isConsentedApplication(caseDetails)
            ? CONSENTED_RESPONDENT_REPRESENTED : CONTESTED_RESPONDENT_REPRESENTED;
    }

    private Map<String, Object> updateCaseDataWithNewSolDetails(CaseDetails caseDetails,
                                               Solicitor addedSolicitor,
                                               String authToken) {

        //Add solicitor contact details, set isRepresented to Yes, update solicitor address
        Map<String, Object> caseData = caseDetails.getData();
        boolean isConsented = caseDataService.isConsentedApplication(caseDetails);

        String appSolicitorAddressField = isConsented ? CONSENTED_SOLICITOR_ADDRESS : CONTESTED_SOLICITOR_ADDRESS;
        String solicitorAddressField = isApplicant ? appSolicitorAddressField : RESP_SOLICITOR_ADDRESS;

        //update party is represented
        caseData.put(isApplicant ? APPLICANT_REPRESENTED : getRespondentRepresentedKey(caseDetails), YES_VALUE);

        OrganisationsResponse organisationsResponse = organisationService.retrieveOrganisationsData(authToken);

        //update address
        caseData.put(solicitorAddressField,
            updateSolicitorDetailsService.convertOrganisationAddressToSolicitorAddress(organisationsResponse));

        //Update other contact fields and return
        return updateSolicitorContactDetails(addedSolicitor, caseData, isConsented);
    }

    private Map<String, Object> updateSolicitorContactDetails(Solicitor addedSolicitor,
                                                              Map<String, Object> caseData,
                                                                       boolean isConsented) {
        if (isApplicant) {
            caseData.put(isConsented ? CONSENTED_SOLICITOR_NAME : CONTESTED_SOLICITOR_NAME,
                addedSolicitor.getName());
            caseData.put(SOLICITOR_EMAIL, addedSolicitor.getEmail());
            caseData.put(isConsented ? CONSENTED_SOLICITOR_FIRM : CONTESTED_SOLICITOR_FIRM,
                addedSolicitor.getOrganisation().getOrganisationName());
        } else {
            updateRespSolFields(caseData, addedSolicitor);
        }


        return removeSolicitorFields(caseData, isConsented);

    }

    private Map<String, Object> removeSolicitorFields(Map<String, Object> caseData, boolean isConsented) {
        if (isApplicant) {
            caseData.remove(SOLICITOR_PHONE);
            caseData.remove(isConsented ? CONSENTED_SOLICITOR_DX_NUMBER : CONTESTED_SOLICITOR_DX_NUMBER);
            caseData.remove(isConsented ? APP_SOLICITOR_AGREE_TO_RECEIVE_EMAILS_CONSENTED
                : APP_SOLICITOR_AGREE_TO_RECEIVE_EMAILS_CONTESTED);
        } else {
            removeRespSolFields(caseData);
        }

        return caseData;
    }

    private void updateRespSolFields(Map<String, Object> caseData, Solicitor addedSolicitor) {
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
