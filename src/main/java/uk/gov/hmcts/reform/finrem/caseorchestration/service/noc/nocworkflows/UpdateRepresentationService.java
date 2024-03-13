package uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.nocworkflows;

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
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AuditEventService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ChangeOfRepresentationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamAuthService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PrdOrganisationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.UpdateSolicitorDetailsService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.barristers.BarristerRepresentationChecker;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.solicitors.AddedSolicitorService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.solicitors.RemovedSolicitorService;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.Map;

import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_SOLICITOR_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_RESPONDENT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_SOLICITOR_FIRM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_RESPONDENT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_FIRM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTVR_SOLICITOR_1_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTVR_SOLICITOR_2_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTVR_SOLICITOR_3_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.IS_NOC_REJECTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_FIRM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangeOrganisationApprovalStatus.REJECTED;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UpdateRepresentationService {

    private static final String CHANGE_ORGANISATION_REQUEST = "changeOrganisationRequestField";
    private static final String NOC_EVENT = "nocRequest";
    private static final String REPRESENTATION_UPDATE_HISTORY = "RepresentationUpdateHistory";

    private final AuditEventService auditEventService;
    private final IdamAuthService idamClient;
    private final ObjectMapper objectMapper;
    private final CaseDataService caseDataService;
    private final PrdOrganisationService organisationService;
    private final UpdateSolicitorDetailsService updateSolicitorDetailsService;
    private final ChangeOfRepresentationService changeOfRepresentationService;
    private final AddedSolicitorService addedSolicitorService;
    private final RemovedSolicitorService removedSolicitorService;
    private final BarristerRepresentationChecker barristerRepresentationChecker;

    public Map<String, Object> updateRepresentationAsSolicitor(CaseDetails caseDetails,
                                                               String authToken) {

        log.info("Updating representation for Case ID: {}", caseDetails.getId());

        final UserDetails solicitorToAdd = getInvokerDetails(authToken, caseDetails);
        log.info("{} Found the solicitor to add {}", caseDetails.getId(), solicitorToAdd.getId());

        final ChangeOrganisationRequest changeRequest = getChangeOrganisationRequest(caseDetails);

        if (barristerRepresentationChecker.hasUserBeenBarristerOnCase(caseDetails.getData(), solicitorToAdd)) {
            log.error("User has represented litigant as Barrister for Case ID: {}, REJECTING COR", caseDetails.getId());
            changeRequest.setApprovalStatus(REJECTED);
            Map<String, Object> caseData = caseDetails.getData();
            caseData.put(CHANGE_ORGANISATION_REQUEST, changeRequest);
            caseData.put(IS_NOC_REJECTED, YES_VALUE);
            return caseData;
        }

        final ChangedRepresentative addedSolicitor = addedSolicitorService.getAddedSolicitorAsSolicitor(solicitorToAdd,
            changeRequest);
        if (addedSolicitor != null) {
            log.info("{} Added solicitor email: {}, organisation {}, name {} ", caseDetails.getId(),
                addedSolicitor.getEmail(), addedSolicitor.getOrganisation().getOrganisationID(),
                addedSolicitor.getName());
        }

        final ChangedRepresentative removedSolicitor = removedSolicitorService.getRemovedSolicitorAsSolicitor(caseDetails,
            changeRequest);
        if (removedSolicitor != null && removedSolicitor.getOrganisation() != null) {
            log.info("{} Removed solicitor organisation {}, name {} ", caseDetails.getId(),
                removedSolicitor.getOrganisation().getOrganisationID(),
                removedSolicitor.getName());
        } else {
            log.info("{} Removed solicitor is null", caseDetails.getId());
        }
        log.info("About to start updating solicitor details in the case data for Case ID: {}", caseDetails.getId());
        caseDetails.getData().putAll(updateCaseDataWithNewSolDetails(caseDetails, addedSolicitor, changeRequest));
        log.info("About to update representation update history for Case ID: {}", caseDetails.getId());

        return updateRepresentationUpdateHistory(caseDetails, addedSolicitor,
            removedSolicitor, changeRequest);
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
        if (current != null) {
            if (current.getRepresentationUpdateHistory() != null
                && !current.getRepresentationUpdateHistory().isEmpty()) {
                log.info("{} current party is {}", caseDetails.getId(),
                    current.getRepresentationUpdateHistory().get(0).getValue().getParty());
            }
            if (current.getRepresentationUpdateHistory() != null
                && !current.getRepresentationUpdateHistory().isEmpty()) {
                log.info("{} current client name is {}", caseDetails.getId(),
                    current.getRepresentationUpdateHistory().get(0).getValue().getClientName());
            }
        } else {
            log.info("{} current is null", caseDetails.getId());
        }

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


        boolean isApplicant = changeRequest.getCaseRoleId().getValueCode().equals(APP_SOLICITOR_POLICY);
        log.info("{} Is applicant: {}", caseDetails.getId(), isApplicant);
        boolean isConsented = caseDataService.isConsentedApplication(caseDetails);
        log.info("{} Is consented: {}", caseDetails.getId(), isConsented);
        addSolicitorAddressToCaseData(addedSolicitor, caseDetails, changeRequest, isConsented);

        log.info("{} changeRequest.getCaseRoleId().getValueCode() is {}", caseDetails.getId(),
            changeRequest.getCaseRoleId().getValueCode());
        Map<String, Object> caseData = caseDetails.getData();
        caseData.put(changeRequest.getCaseRoleId().getValueCode().equals(APP_SOLICITOR_POLICY)
            ? APPLICANT_REPRESENTED : getRespondentRepresentedKey(caseDetails), YES_VALUE);


        Map<String, Object> updatedCaseData = updateSolicitorDetailsService.updateSolicitorContactDetails(
            addedSolicitor, caseData, isConsented, isApplicant);
        log.info("{} about to remove solicitor fields", caseDetails.getId());

        updatedCaseData = updateSolicitorDetailsService.removeSolicitorFields(updatedCaseData, isConsented, isApplicant);

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
        final boolean isApplicant = changeRequest.getCaseRoleId().getValueCode().equals(APP_SOLICITOR_POLICY);
        log.info("{} Is applicant: {}", caseDetails.getId(), isApplicant);
        String appSolicitorAddressField = isConsented ? CONSENTED_SOLICITOR_ADDRESS : CONTESTED_SOLICITOR_ADDRESS;
        log.info("{} App solicitor address field: {}", caseDetails.getId(), appSolicitorAddressField);
        String solicitorAddressField = isApplicant ? appSolicitorAddressField : RESP_SOLICITOR_ADDRESS;
        log.info("{} Solicitor address field: {}", caseDetails.getId(), solicitorAddressField);
        OrganisationsResponse organisationsResponse = organisationService
            .findOrganisationByOrgId(addedSolicitor.getOrganisation().getOrganisationID());
        log.info("{} Found the organisation with ID: {} and name: {}", caseDetails.getId(),
            organisationsResponse.getOrganisationIdentifier(),
            organisationsResponse.getName());
        String firmName = organisationsResponse.getName();
        log.info("{} Firm name: {}", caseDetails.getId(), firmName);
        caseDetails.getData().put(getSolicitorFirmNameKey(caseDetails, isApplicant), firmName);
        log.info("{} contestedFirm: {}", caseDetails.getId(), caseDetails.getData().get(CONTESTED_SOLICITOR_FIRM));
        caseDetails.getData().put(solicitorAddressField,
            updateSolicitorDetailsService.convertOrganisationAddressToSolicitorAddress(organisationsResponse));
        log.info("{} updated contested solicitor address to {}", caseDetails.getId(), caseDetails.getData().get(CONTESTED_SOLICITOR_ADDRESS));
    }

    private String getRespondentRepresentedKey(CaseDetails caseDetails) {
        return caseDataService.isConsentedApplication(caseDetails)
            ? CONSENTED_RESPONDENT_REPRESENTED : CONTESTED_RESPONDENT_REPRESENTED;
    }

    private String getSolicitorFirmNameKey(CaseDetails caseDetails, boolean isApplicant) {
        log.info(getAppSolicitorFirmNameKey(caseDetails));
        return isApplicant
            ? getAppSolicitorFirmNameKey(caseDetails)
            : RESP_SOLICITOR_FIRM;
    }

    private String getAppSolicitorFirmNameKey(CaseDetails caseDetails) {
        return caseDataService.isConsentedApplication(caseDetails)
            ? CONSENTED_SOLICITOR_FIRM
            : CONTESTED_SOLICITOR_FIRM;
    }

    private RepresentationUpdateHistory getCurrentRepresentationUpdateHistory(Map<String, Object> caseData) {
        return RepresentationUpdateHistory.builder()
            .representationUpdateHistory(objectMapper.convertValue(caseData.get(REPRESENTATION_UPDATE_HISTORY),
                new TypeReference<>() {
                })).build();
    }

    private ChangeOfRepresentationRequest buildChangeOfRepresentationRequest(CaseDetails caseDetails,
                                                                             ChangedRepresentative addedSolicitor,
                                                                             ChangedRepresentative removedSolicitor,
                                                                             RepresentationUpdateHistory current,
                                                                             ChangeOrganisationRequest changeRequest) {
        if (changeRequest != null) {
            log.info("{} change request {}", caseDetails.getId(), changeRequest);
        }
        return ChangeOfRepresentationRequest.builder()
            .by(addedSolicitor.getName())
            .party(changeRequest.getCaseRoleId().getValueCode().equals(APP_SOLICITOR_POLICY) ? APPLICANT : RESPONDENT)
            .clientName(buildFullName(changeRequest, caseDetails))
            .current(current)
            .addedRepresentative(addedSolicitor)
            .removedRepresentative(removedSolicitor)
            .build();
    }

    private String buildFullName(ChangeOrganisationRequest changeRequest, CaseDetails caseDetails) {
        if (changeRequest.getCaseRoleId().getValueCode().equals(APP_SOLICITOR_POLICY)) {
            return caseDataService.buildFullApplicantName(caseDetails);
        } else if (changeRequest.getCaseRoleId().getValueCode().equals(RESP_SOLICITOR_POLICY)) {
            return caseDataService.buildFullRespondentName(caseDetails);
        } else if (changeRequest.getCaseRoleId().getValueCode().equals(INTVR_SOLICITOR_1_POLICY)) {
            return caseDataService.buildFullIntervener1Name(caseDetails);
        } else if (changeRequest.getCaseRoleId().getValueCode().equals(INTVR_SOLICITOR_2_POLICY)) {
            return caseDataService.buildFullIntervener2Name(caseDetails);
        } else if (changeRequest.getCaseRoleId().getValueCode().equals(INTVR_SOLICITOR_3_POLICY)) {
            return caseDataService.buildFullIntervener3Name(caseDetails);
        }
        return caseDataService.buildFullIntervener4Name(caseDetails);
    }
}
