package uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.nocworkflows;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangeOfRepresentationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangeOrganisationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangedRepresentative;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Organisation;
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

import static java.lang.String.format;
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
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTVR_SOLICITOR_4_POLICY;
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
        final ChangedRepresentative removedSolicitor = removedSolicitorService.getRemovedSolicitorAsSolicitor(caseDetails,
            changeRequest);

        log.info("About to start updating solicitor details in the case data for Case ID: {}", caseDetails.getId());
        caseDetails.getData().putAll(updateCaseDataWithNewSolDetails(caseDetails, addedSolicitor, changeRequest));

        return updateRepresentationUpdateHistory(caseDetails, addedSolicitor,
            removedSolicitor, changeRequest);
    }

    private UserDetails getInvokerDetails(String authToken, CaseDetails caseDetails) {
        AuditEvent auditEvent = auditEventService.getLatestAuditEventByName(caseDetails.getId().toString(), NOC_EVENT)
            .orElseThrow(() -> new IllegalStateException(format("%s - Could not find %s event in audit", caseDetails.getId(), NOC_EVENT)));

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

    private boolean isApplicant(CaseDetails caseDetails, ChangeOrganisationRequest changeRequest) {
        if (StringUtils.isEmpty(changeRequest.getCaseRoleId().getValueCode())) {
            throw new UnsupportedOperationException(format("%s - unexpected empty caseRoleId", caseDetails.getId()));
        }
        return APP_SOLICITOR_POLICY.equals(changeRequest.getCaseRoleId().getValueCode());
    }

    private Map<String, Object> updateCaseDataWithNewSolDetails(CaseDetails caseDetails,
                                                                ChangedRepresentative addedSolicitor,
                                                                ChangeOrganisationRequest changeRequest) {

        Map<String, Object> caseData = caseDetails.getData();
        boolean isApplicant = isApplicant(caseDetails, changeRequest);
        boolean isConsented = caseDataService.isConsentedApplication(caseDetails);
        addSolicitorAddressToCaseData(addedSolicitor, caseDetails, changeRequest, isConsented);

        caseData.put(isApplicant ? APPLICANT_REPRESENTED : getRespondentRepresentedKey(caseDetails), YES_VALUE);

        Map<String, Object> updatedCaseData = updateSolicitorDetailsService.updateSolicitorContactDetails(
            addedSolicitor, caseData, isConsented, isApplicant);

        updatedCaseData = updateSolicitorDetailsService.removeSolicitorFields(updatedCaseData, isConsented, isApplicant);

        return updatedCaseData;
    }

    private ChangeOrganisationRequest getChangeOrganisationRequest(CaseDetails caseDetails) {

        return objectMapper.convertValue(caseDetails.getData().get(CHANGE_ORGANISATION_REQUEST),
            ChangeOrganisationRequest.class);
    }

    public void addRemovedSolicitorOrganisationFieldToCaseData(CaseDetails caseDetails) {
        ChangeOrganisationRequest changeRequest = getChangeOrganisationRequest(caseDetails);
        if (changeRequest != null && changeRequest.getOrganisationToRemove() == null) {
            log.info("Adding the organisation field for the removed solicitor needed to process NOC on case {}",
                caseDetails.getId());
            changeRequest.setOrganisationToRemove(Organisation.builder().organisationID(null).build());
            caseDetails.getData().put(CHANGE_ORGANISATION_REQUEST, changeRequest);
        }
    }

    private void addSolicitorAddressToCaseData(ChangedRepresentative addedSolicitor,
                                               CaseDetails caseDetails,
                                               ChangeOrganisationRequest changeRequest,
                                               boolean isConsented) {
        final boolean isApplicant = isApplicant(caseDetails, changeRequest);
        String appSolicitorAddressField = isConsented ? CONSENTED_SOLICITOR_ADDRESS : CONTESTED_SOLICITOR_ADDRESS;
        String solicitorAddressField = isApplicant ? appSolicitorAddressField : RESP_SOLICITOR_ADDRESS;

        OrganisationsResponse organisationsResponse = organisationService
            .findOrganisationByOrgId(addedSolicitor.getOrganisation().getOrganisationID());
        String firmName = organisationsResponse.getName();

        caseDetails.getData().put(getSolicitorFirmNameKey(caseDetails, isApplicant), firmName);
        caseDetails.getData().put(solicitorAddressField,
            updateSolicitorDetailsService.convertOrganisationAddressToSolicitorAddress(organisationsResponse));
    }

    private String getRespondentRepresentedKey(CaseDetails caseDetails) {
        return caseDataService.isConsentedApplication(caseDetails)
            ? CONSENTED_RESPONDENT_REPRESENTED : CONTESTED_RESPONDENT_REPRESENTED;
    }

    private String getSolicitorFirmNameKey(CaseDetails caseDetails, boolean isApplicant) {
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
        return ChangeOfRepresentationRequest.builder()
            .by(addedSolicitor.getName())
            .party(isApplicant(caseDetails, changeRequest) ? APPLICANT : RESPONDENT)
            .clientName(buildFullName(changeRequest, caseDetails))
            .current(current)
            .addedRepresentative(addedSolicitor)
            .removedRepresentative(removedSolicitor)
            .build();
    }

    private String buildFullName(ChangeOrganisationRequest changeRequest, CaseDetails caseDetails) {
        if (isApplicant(caseDetails, changeRequest)) {
            return caseDataService.buildFullApplicantName(caseDetails);
        } else if (RESP_SOLICITOR_POLICY.equals(changeRequest.getCaseRoleId().getValueCode())) {
            return caseDataService.buildFullRespondentName(caseDetails);
        } else if (INTVR_SOLICITOR_1_POLICY.equals(changeRequest.getCaseRoleId().getValueCode())) {
            return caseDataService.buildFullIntervener1Name(caseDetails);
        } else if (INTVR_SOLICITOR_2_POLICY.equals(changeRequest.getCaseRoleId().getValueCode())) {
            return caseDataService.buildFullIntervener2Name(caseDetails);
        } else if (INTVR_SOLICITOR_3_POLICY.equals(changeRequest.getCaseRoleId().getValueCode())) {
            return caseDataService.buildFullIntervener3Name(caseDetails);
        } else if (INTVR_SOLICITOR_4_POLICY.equals(changeRequest.getCaseRoleId().getValueCode())) {
            return caseDataService.buildFullIntervener4Name(caseDetails);
        } else {
            throw new UnsupportedOperationException(format("%s - Unrecognised caseRoleId: %s",
                caseDetails.getId(), changeRequest.getCaseRoleId().getValueCode()));
        }
    }
}
