package uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.nocworkflows;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangeOfRepresentationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangeOrganisationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangedRepresentative;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_SOLICITOR_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CHANGE_ORGANISATION_REQUEST;
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

    private static final String NOC_EVENT = "nocRequest";
    private static final String REPRESENTATION_UPDATE_HISTORY = "RepresentationUpdateHistory";
    private static final String NOT_ACTIVE_USER_ERROR = "Email is not linked to an active User within a HMCTS organisation";
    private static final String VALIDATE_EMAIL_ACTIVE_FOR_ORG_ERROR = "Email could not be linked to your organisation. Please check and try again";

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
    private final FinremCaseDetailsMapper finremCaseDetailsMapper;

    /**
     * Updates the legal representation of a case when a solicitor submits a Notice of Change (NoC).
     *
     * <p>This method retrieves the invoking user's details from the authentication token and extracts
     * the {@code ChangeOrganisationRequest} from the case.</p>
     *
     * <p>It checks whether the user has previously acted as a barrister on the case. If so, the request
     * is rejected, the NoC is marked as rejected, and no further changes are applied.</p>
     *
     * <p>If the request is valid, the method determines the solicitor being added and the solicitor
     * being removed, updates the case data with the new solicitor details, and records the change in
     * the representation update history.</p>
     *
     * @param caseDetails the case details containing the current case data and metadata; must not be {@code null}
     * @param authToken the authentication token of the invoking user, used to identify the solicitor; must not be {@code null}
     * @return a {@link Map} containing the updated case data. If the request is rejected due to prior
     *         barrister involvement, the original case data is returned unchanged
     *
     * @throws RuntimeException if an error occurs while updating representation details
     */
    public Map<String, Object> updateRepresentationAsSolicitor(CaseDetails caseDetails,
                                                               String authToken) {

        log.info("Updating representation for Case ID: {}", caseDetails.getId());

        final UserDetails solicitorToAdd = getInvokerDetails(authToken, caseDetails);
        final ChangeOrganisationRequest changeRequest = getChangeOrganisationRequest(caseDetails);

        if (shouldRejectNoc(caseDetails, solicitorToAdd)) {
            markNocRejected(caseDetails);
            return caseDetails.getData();
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

    private boolean shouldRejectNoc(CaseDetails caseDetails, UserDetails solicitorToAdd) {
        Map<String, Object> data = caseDetails.getData();

        if (barristerRepresentationChecker.hasUserBeenBarristerOnCase(data, solicitorToAdd)) {
            log.info("User has represented litigant as Barrister for Case ID: {}, REJECTING COR",
                caseDetails.getId());
            return true;
        }

        FinremCaseData caseData = finremCaseDetailsMapper.mapToFinremCaseData(data);

        boolean isApplicantRequest =
            isRequestForApplicantSolicitorRole(caseDetails, caseData.getChangeOrganisationRequestField());

        return isApplicantRequest
            ? isSolicitorToAddAlreadyRepresentingRespondent(solicitorToAdd, caseData)
            : isSolicitorToAddAlreadyRepresentingApplicant(solicitorToAdd, caseData);
    }

    private boolean isSolicitorAlreadyRepresenting(Function<FinremCaseData, String> emailExtractor,
                                                   UserDetails solicitorToAdd,
                                                   FinremCaseData finremCaseData) {

        String existingEmail = emailExtractor.apply(finremCaseData);
        String emailToBeAdded = solicitorToAdd.getEmail();

        return Objects.equals(normalizeAndLower(emailToBeAdded), normalizeAndLower(existingEmail));
    }

    private boolean isSolicitorToAddAlreadyRepresentingApplicant(UserDetails solicitorToAdd,
                                                                 FinremCaseData finremCaseData) {
        return isSolicitorAlreadyRepresenting(FinremCaseData::getAppSolicitorEmailIfRepresented,
            solicitorToAdd, finremCaseData);
    }

    private boolean isSolicitorToAddAlreadyRepresentingRespondent(UserDetails solicitorToAdd,
                                                                  FinremCaseData finremCaseData) {
        return isSolicitorAlreadyRepresenting(FinremCaseData::getRespSolicitorEmailIfRepresented,
            solicitorToAdd, finremCaseData);
    }

    private String normalizeAndLower(String email) {
        // TODO extract the same logic FinremCallbackRequest.normalizeAndLower
        return ofNullable(email)
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .map(String::toLowerCase)
            .orElse(null);
    }

    /*
     * Checks if the email address provided is linked to an active user within a HMCTS organisation.
     * Upon missing or invalid email address, an error message is added to the list.
     * @param emailAddress the email address to validate
     * @param caseReference the case reference to include in error messages for traceability
     * @return a list of error messages, empty if the email is valid and active
     */
    public List<String> validateEmailActiveForOrganisation(String emailAddress, String caseReference) {
        List<String> errors  = new ArrayList<>();
        try {
            Optional<String> userId = organisationService.findUserByEmail(emailAddress);
            if (userId.isEmpty()) {
                log.info("{} case reference: {}", NOT_ACTIVE_USER_ERROR, caseReference);
                errors.add(NOT_ACTIVE_USER_ERROR);
            }
            return errors;
        } catch (Exception e) {
            log.error(
                "validateEmailActiveForOrganisation failed for Case reference {}. Exception message {}",
                caseReference,
                e.getMessage()
            );
            errors.add(VALIDATE_EMAIL_ACTIVE_FOR_ORG_ERROR + " Case reference: " + caseReference);
            return errors;
        }
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

    private boolean isRequestForApplicantSolicitorRole(CaseDetails caseDetails, ChangeOrganisationRequest changeRequest) {
        String roleId = Optional.ofNullable(changeRequest.getCaseRoleId())
            .map(DynamicList::getValueCode)
            .orElseThrow(() -> new NoticeOfChangeInvalidRequestException(
                format("%s - unexpected empty caseRoleId", caseDetails.getId())
            ));

        return APP_SOLICITOR_POLICY.equals(roleId);
    }

    private Map<String, Object> updateCaseDataWithNewSolDetails(CaseDetails caseDetails,
                                                                ChangedRepresentative addedSolicitor,
                                                                ChangeOrganisationRequest changeRequest) {

        Map<String, Object> caseData = caseDetails.getData();
        boolean isApplicant = isRequestForApplicantSolicitorRole(caseDetails, changeRequest);
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
        final boolean isApplicant = isRequestForApplicantSolicitorRole(caseDetails, changeRequest);
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
            .party(isRequestForApplicantSolicitorRole(caseDetails, changeRequest) ? APPLICANT : RESPONDENT)
            .clientName(buildFullName(changeRequest, caseDetails))
            .current(current)
            .addedRepresentative(addedSolicitor)
            .removedRepresentative(removedSolicitor)
            .build();
    }

    private String buildFullName(ChangeOrganisationRequest changeRequest, CaseDetails caseDetails) {
        if (isRequestForApplicantSolicitorRole(caseDetails, changeRequest)) {
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

    private void markNocRejected(CaseDetails caseDetails) {
        ChangeOrganisationRequest changeRequest = getChangeOrganisationRequest(caseDetails);
        changeRequest.setApprovalStatus(REJECTED);
        Map<String, Object> caseData = caseDetails.getData();
        caseData.put(CHANGE_ORGANISATION_REQUEST, changeRequest);
        caseData.put(IS_NOC_REJECTED, YES_VALUE);
    }
}
