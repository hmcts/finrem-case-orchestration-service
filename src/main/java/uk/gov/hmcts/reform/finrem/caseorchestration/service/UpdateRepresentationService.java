package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangeOfRepresentationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangeOfRepresentatives;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangeOrganisationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangedRepresentative;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.events.AuditEvent;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.organisation.OrganisationsResponse;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_SOLICITOR_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_RESPONDENT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_RESPONDENT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_EMAIL;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UpdateRepresentationService {

    private final AuditEventService auditEventService;
    private final IdamAuthService idamClient;
    private final ObjectMapper objectMapper;
    private final CaseDataService caseDataService;
    private final PrdOrganisationService organisationService;
    private final UpdateSolicitorDetailsService updateSolicitorDetailsService;
    private final ChangeOfRepresentationService changeOfRepresentationService;

    private static final String CHANGE_REQUEST_FIELD = "changeOrganisationRequestField";
    private static final String NOC_EVENT = "nocRequest";
    private static final String CHANGE_OF_REPRESENTATIVES = "ChangeOfRepresentatives";

    public Map<String, Object> updateRepresentationAsSolicitor(CaseDetails caseDetails,
                                                               String authToken)  {

        log.info("Updating representation for case ID {}", caseDetails.getId());

        final UserDetails solicitorToAdd = getInvokerDetails(authToken, caseDetails);
        final ChangeOrganisationRequest changeRequest = getChangeOrganisationRequest(caseDetails);

        final ChangedRepresentative addedSolicitor = getAddedSolicitor(solicitorToAdd, changeRequest);
        final ChangedRepresentative removedSolicitor = getRemovedSolicitor(caseDetails, changeRequest);

        log.info("About to start updating solicitor details in the case data for caseId: {}", caseDetails.getId());
        caseDetails.getData().putAll(updateCaseDataWithNewSolDetails(caseDetails, addedSolicitor, changeRequest));

        Map<String, Object> updatedCaseData = updateChangeOfRepresentatives(caseDetails,
            addedSolicitor,
            removedSolicitor,
            changeRequest);
        return updatedCaseData;
    }

    private UserDetails getInvokerDetails(String authToken, CaseDetails caseDetails) {
        AuditEvent auditEvent = auditEventService.getLatestAuditEventByName(caseDetails.getId().toString(), NOC_EVENT)
            .orElseThrow(() -> new IllegalStateException(String.format("Could not find %s event in audit", NOC_EVENT)));

        return idamClient.getUserByUserId(authToken, auditEvent.getUserId());
    }

    private Map<String, Object> updateChangeOfRepresentatives(CaseDetails caseDetails,
                                                              ChangedRepresentative addedSolicitor,
                                                              ChangedRepresentative removedSolicitor,
                                                              ChangeOrganisationRequest changeRequest) {

        Map<String, Object> caseData = caseDetails.getData();
        ChangeOfRepresentatives current = getCurrentChangeOfRepresentatives(caseData);

        ChangeOfRepresentatives change = changeOfRepresentationService
            .generateChangeOfRepresentatives(buildChangeOfRepresentationRequest(caseDetails,
                addedSolicitor,
                removedSolicitor,
                current,
                changeRequest));

        caseData.put(CHANGE_OF_REPRESENTATIVES, change.getChangeOfRepresentation());

        return caseData;
    }

    private Map<String, Object> updateCaseDataWithNewSolDetails(CaseDetails caseDetails,
                                                                ChangedRepresentative addedSolicitor,
                                                                ChangeOrganisationRequest changeRequest) {

        Map<String, Object> caseData = caseDetails.getData();
        boolean isConsented = caseDataService.isConsentedApplication(caseDetails);
        String appSolicitorAddressField = isConsented ? CONSENTED_SOLICITOR_ADDRESS : CONTESTED_SOLICITOR_ADDRESS;
        String solicitorAddressField = changeRequest.getCaseRoleId().getValueCode().equals(APP_SOLICITOR_POLICY)
            ? appSolicitorAddressField : RESP_SOLICITOR_ADDRESS;

        caseData.put(changeRequest.getCaseRoleId().getValueCode().equals(APP_SOLICITOR_POLICY)
            ? APPLICANT_REPRESENTED
            : getRespondentRepresentedKey(caseDetails),
            YES_VALUE);

        OrganisationsResponse organisationsResponse = organisationService
            .findOrganisationByOrgId(addedSolicitor.getOrganisation().getOrganisationID());

        caseData.put(solicitorAddressField,
            updateSolicitorDetailsService.convertOrganisationAddressToSolicitorAddress(organisationsResponse));

        Map<String, Object> updatedCaseData = updateSolicitorDetailsService.updateSolicitorContactDetails(addedSolicitor,
             caseData, isConsented, changeRequest.getCaseRoleId().getValueCode().equals(APP_SOLICITOR_POLICY));

        updatedCaseData = updateSolicitorDetailsService.removeSolicitorFields(updatedCaseData,
            isConsented,
            changeRequest.getCaseRoleId().getValueCode().equals(APP_SOLICITOR_POLICY));

        return updatedCaseData;
    }

    private ChangeOrganisationRequest getChangeOrganisationRequest(CaseDetails caseDetails) {

        return objectMapper.convertValue(caseDetails.getData().get(CHANGE_REQUEST_FIELD),
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

    private String getRespondentRepresentedKey(CaseDetails caseDetails) {
        return caseDataService.isConsentedApplication(caseDetails)
            ? CONSENTED_RESPONDENT_REPRESENTED : CONTESTED_RESPONDENT_REPRESENTED;
    }

    private ChangedRepresentative getAddedSolicitor(UserDetails solicitorToAdd,
                                                    ChangeOrganisationRequest changeRequest) {
        return ChangedRepresentative.builder()
            .name(solicitorToAdd.getFullName())
            .email(solicitorToAdd.getEmail())
            .organisation(changeRequest.getOrganisationToAdd())
            .build();
    }

    private ChangedRepresentative getRemovedSolicitor(CaseDetails caseDetails,
                                                      ChangeOrganisationRequest changeRequest) {
        return Optional.ofNullable(changeRequest.getOrganisationToRemove())
            .map(org -> ChangedRepresentative.builder()
                .name(changeRequest.getCaseRoleId().getValueCode().equals(APP_SOLICITOR_POLICY)
                    ? getApplicantSolicitorName(caseDetails)
                    : (String) caseDetails.getData().get(RESP_SOLICITOR_NAME))
                .email(changeRequest.getCaseRoleId().getValueCode().equals(APP_SOLICITOR_POLICY)
                    ? getApplicantSolicitorEmail(caseDetails)
                    : (String) caseDetails.getData().get(RESP_SOLICITOR_EMAIL))
                .organisation(org).build())
            .orElse(null);
    }

    private ChangeOfRepresentatives getCurrentChangeOfRepresentatives(Map<String, Object> caseData) {
        return ChangeOfRepresentatives.builder()
            .changeOfRepresentation(objectMapper.convertValue(caseData.get(CHANGE_OF_REPRESENTATIVES),
                new TypeReference<>() {})).build();
    }

    private ChangeOfRepresentationRequest buildChangeOfRepresentationRequest(CaseDetails caseDetails,
                                                                             ChangedRepresentative addedSolicitor,
                                                                             ChangedRepresentative removedSolicitor,
                                                                             ChangeOfRepresentatives current,
                                                                             ChangeOrganisationRequest changeRequest) {
        return ChangeOfRepresentationRequest.builder()
            .by(addedSolicitor.getName())
            .party(changeRequest.getCaseRoleId().getValueCode().equals(APP_SOLICITOR_POLICY) ? APPLICANT : RESPONDENT)
            .clientName(changeRequest.getCaseRoleId().getValueCode().equals(APP_SOLICITOR_POLICY)
                ? caseDataService.buildFullApplicantName(caseDetails)
                : caseDataService.buildFullRespondentName(caseDetails))
            .current(current)
            .addedRepresentative(addedSolicitor)
            .removedRepresentative(removedSolicitor)
            .build();
    }
}
