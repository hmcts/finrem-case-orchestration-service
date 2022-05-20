package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangeOfRepresentationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangeOrganisationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangedRepresentative;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RepresentationUpdateHistory;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.events.AuditEvent;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.organisation.OrganisationsResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.solicitors.GetAddedSolicitorService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.solicitors.GetRemovedSolicitorService;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.Map;

import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_SOLICITOR_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_RESPONDENT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_RESPONDENT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_ADDRESS;

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
    private final GetAddedSolicitorService getAddedSolicitorService;
    private final GetRemovedSolicitorService getRemovedSolicitorService;

    private static final String CHANGE_ORGANISATION_REQUEST = "changeOrganisationRequestField";
    private static final String NOC_EVENT = "nocRequest";
    private static final String REPRESENTATION_UPDATE_HISTORY = "RepresentationUpdateHistory";

    public Map<String, Object> updateRepresentationAsSolicitor(CaseDetails caseDetails,
                                                               String authToken)  {

        log.info("Updating representation for case ID {}", caseDetails.getId());

        final UserDetails solicitorToAdd = getInvokerDetails(authToken, caseDetails);
        final ChangeOrganisationRequest changeRequest = getChangeOrganisationRequest(caseDetails);

        final ChangedRepresentative addedSolicitor = getAddedSolicitorService.getAddedSolicitorAsSolicitor(solicitorToAdd,
            changeRequest);
        final ChangedRepresentative removedSolicitor = getRemovedSolicitorService.getRemovedSolicitorAsSolicitor(caseDetails,
            changeRequest);

        log.info("About to start updating solicitor details in the case data for caseId: {}", caseDetails.getId());
        caseDetails.getData().putAll(updateCaseDataWithNewSolDetails(caseDetails, addedSolicitor, changeRequest));

        Map<String, Object> updatedCaseData = updateRepresentationUpdateHistory(caseDetails,
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

    private Map<String, Object> updateRepresentationUpdateHistory(CaseDetails caseDetails,
                                                              ChangedRepresentative addedSolicitor,
                                                              ChangedRepresentative removedSolicitor,
                                                              ChangeOrganisationRequest changeRequest) {

        Map<String, Object> caseData = caseDetails.getData();
        RepresentationUpdateHistory current = getCurrentRepresentationUpdateHistory(caseData);

        RepresentationUpdateHistory change = changeOfRepresentationService
            .generateRepresentationUpdateHistory(buildChangeOfRepresentationRequest(caseDetails,
                addedSolicitor,
                removedSolicitor,
                current,
                changeRequest));

        caseData.put(REPRESENTATION_UPDATE_HISTORY, change.getRepresentationUpdateHistory());

        return caseData;
    }

    private Map<String, Object> updateCaseDataWithNewSolDetails(CaseDetails caseDetails,
                                                                ChangedRepresentative addedSolicitor,
                                                                ChangeOrganisationRequest changeRequest) {

        Map<String, Object> caseData = caseDetails.getData();
        boolean isConsented = caseDataService.isConsentedApplication(caseDetails);
        addSolicitorAddressToCaseData(addedSolicitor, caseDetails, changeRequest, isConsented);

        caseData.put(changeRequest.getCaseRoleId().getValueCode().equals(APP_SOLICITOR_POLICY)
            ? APPLICANT_REPRESENTED
            : getRespondentRepresentedKey(caseDetails),
            YES_VALUE);

        Map<String, Object> updatedCaseData = updateSolicitorDetailsService.updateSolicitorContactDetails(addedSolicitor,
             caseData, isConsented, changeRequest.getCaseRoleId().getValueCode().equals(APP_SOLICITOR_POLICY));

        updatedCaseData = updateSolicitorDetailsService.removeSolicitorFields(updatedCaseData,
            isConsented,
            changeRequest.getCaseRoleId().getValueCode().equals(APP_SOLICITOR_POLICY));

        return updatedCaseData;
    }

    private ChangeOrganisationRequest getChangeOrganisationRequest(CaseDetails caseDetails) {

        return objectMapper.convertValue(caseDetails.getData().get(CHANGE_ORGANISATION_REQUEST),
            ChangeOrganisationRequest.class);
    }

    private void addSolicitorAddressToCaseData(ChangedRepresentative addedSolicitor,
                                               CaseDetails caseDetails,
                                               ChangeOrganisationRequest changeRequest,
                                               boolean isConsented) {
        String appSolicitorAddressField = isConsented ? CONSENTED_SOLICITOR_ADDRESS : CONTESTED_SOLICITOR_ADDRESS;
        String solicitorAddressField = changeRequest.getCaseRoleId().getValueCode().equals(APP_SOLICITOR_POLICY)
            ? appSolicitorAddressField : RESP_SOLICITOR_ADDRESS;

        OrganisationsResponse organisationsResponse = organisationService
            .findOrganisationByOrgId(addedSolicitor.getOrganisation().getOrganisationID());

        caseDetails.getData().put(solicitorAddressField,
            updateSolicitorDetailsService.convertOrganisationAddressToSolicitorAddress(organisationsResponse));
    }

    private String getRespondentRepresentedKey(CaseDetails caseDetails) {
        return caseDataService.isConsentedApplication(caseDetails)
            ? CONSENTED_RESPONDENT_REPRESENTED : CONTESTED_RESPONDENT_REPRESENTED;
    }

    private RepresentationUpdateHistory getCurrentRepresentationUpdateHistory(Map<String, Object> caseData) {
        return RepresentationUpdateHistory.builder()
            .representationUpdateHistory(objectMapper.convertValue(caseData.get(REPRESENTATION_UPDATE_HISTORY),
                new TypeReference<>() {})).build();
    }

    private ChangeOfRepresentationRequest buildChangeOfRepresentationRequest(CaseDetails caseDetails,
                                                                             ChangedRepresentative addedSolicitor,
                                                                             ChangedRepresentative removedSolicitor,
                                                                             RepresentationUpdateHistory current,
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
