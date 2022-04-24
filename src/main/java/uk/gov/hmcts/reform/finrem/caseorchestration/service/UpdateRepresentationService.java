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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangeOrganisationApprovalStatus;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangeOrganisationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangedRepresentative;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrganisationPolicy;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.events.AuditEvent;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.organisation.OrganisationsResponse;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_ORGANISATION_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_SOLICITOR_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_RESPONDENT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_RESPONDENT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_ORGANISATION_POLICY;
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
    private static final String CHANGE_OF_REPS = "ChangeOfRepresentatives";
    private static final String NOC_PARTY = "nocParty";

    private boolean isApplicant;

    public Map<String, Object> updateRepresentationAsSolicitor(CaseDetails caseDetails,
                                                               String authToken)  {

        log.info("Updating representation for case ID {}", caseDetails.getId());

        AuditEvent auditEvent = auditEventService.getLatestAuditEventByName(caseDetails.getId().toString(), NOC_EVENT)
            .orElseThrow(() -> new IllegalStateException(String.format("Could not find %s event in audit", NOC_EVENT)));

        UserDetails solicitorToAdd = idamClient.getUserByUserId(authToken, auditEvent.getUserId());
        ChangeOrganisationRequest change = getChangeOrganisationRequest(caseDetails);
        isApplicant = change.getCaseRoleId().getValueCode().equals(APP_SOLICITOR_POLICY);

        ChangedRepresentative addedSolicitor = ChangedRepresentative.builder()
            .name(solicitorToAdd.getFullName())
            .email(solicitorToAdd.getEmail())
            .organisation(change.getOrganisationToAdd())
            .build();

        ChangedRepresentative removedSolicitor = Optional.ofNullable(change.getOrganisationToRemove())
            .map(org -> ChangedRepresentative.builder()
            .name(isApplicant ? getApplicantSolicitorName(caseDetails)
                : (String) caseDetails.getData().get(RESP_SOLICITOR_NAME))
            .email(isApplicant ? getApplicantSolicitorEmail(caseDetails)
                : (String) caseDetails.getData().get(RESP_SOLICITOR_EMAIL))
            .organisation(org).build())
            .orElse(null);

        log.info("About to start updating solicitor details in the case data for caseId: {}", caseDetails.getId());
        caseDetails.getData().putAll(updateCaseDataWithNewSolDetails(caseDetails, addedSolicitor));

        return updateChangeOfRepresentatives(caseDetails, addedSolicitor, removedSolicitor, addedSolicitor.getName());
    }

    public Map<String, Object> removeRepresentationAsCaseworker(CaseDetails caseDetails,
                                                                String authToken) {
        isApplicant = ((String)caseDetails.getData().get(NOC_PARTY)).equalsIgnoreCase("applicant");

        OrganisationPolicy policy = objectMapper.convertValue(caseDetails.getData().get(isApplicant
            ? APPLICANT_ORGANISATION_POLICY : RESPONDENT_ORGANISATION_POLICY), OrganisationPolicy.class);

        ChangedRepresentative removedSolicitor = Optional.ofNullable(policy.getOrganisation())
            .map(org -> ChangedRepresentative.builder()
                .name(isApplicant ? getApplicantSolicitorName(caseDetails)
                    : (String) caseDetails.getData().get(RESP_SOLICITOR_NAME))
                .email(isApplicant ? getApplicantSolicitorEmail(caseDetails)
                    : (String) caseDetails.getData().get(RESP_SOLICITOR_EMAIL))
                .organisation(org).build())
            .orElseThrow(IllegalStateException::new);

        caseDetails.getData().put(isApplicant ? APPLICANT_REPRESENTED
            : getRespondentRepresentedKey(caseDetails), NO_VALUE);
        caseDetails.getData().put(CHANGE_REQUEST_FIELD, generateChangeOrganisationRequest(policy));
        String invokerName = idamClient.getUserFullName(authToken);

        return updateChangeOfRepresentatives(caseDetails, null, removedSolicitor, invokerName);
    }

    private ChangeOrganisationRequest generateChangeOrganisationRequest(OrganisationPolicy organisationPolicy) {

        return ChangeOrganisationRequest.builder()
            .organisationToAdd(null)
            .organisationToRemove(organisationPolicy.getOrganisation())
            .approvalStatus(ChangeOrganisationApprovalStatus.APPROVED)
            .caseRoleId(generateCaseRoleIdDynamicListElementAsList(organisationPolicy.getOrgPolicyCaseAssignedRole()))
            .requestTimestamp(LocalDateTime.now())
            .approvalRejectionTimestamp(LocalDateTime.now())
            .reason(null)
            .build();
    }

    private Map<String, Object> updateChangeOfRepresentatives(CaseDetails caseDetails,
                                                              ChangedRepresentative addedSolicitor,
                                                              ChangedRepresentative removedSolicitor,
                                                              String invokerName) {

        Map<String, Object> caseData = caseDetails.getData();
        ChangeOfRepresentatives current = ChangeOfRepresentatives.builder()
            .changeOfRepresentation(objectMapper.convertValue(caseData.get(CHANGE_OF_REPS),
                new TypeReference<>() {}))
            .build();

        ChangeOfRepresentatives change = changeOfRepresentationService.generateChangeOfRepresentatives(
            ChangeOfRepresentationRequest.builder()
                .by(invokerName)
                .party(isApplicant ? "Applicant" : "Respondent")
                .clientName(isApplicant ? caseDataService.buildFullApplicantName(caseDetails)
                    : caseDataService.buildFullRespondentName(caseDetails))
                .current(current)
                .addedRepresentative(addedSolicitor)
                .removedRepresentative(removedSolicitor)
                .build()
        );

        caseData.put(CHANGE_OF_REPS, change.getChangeOfRepresentation());

        return caseData;
    }

    private Map<String, Object> updateCaseDataWithNewSolDetails(CaseDetails caseDetails,
                                                                ChangedRepresentative addedSolicitor) {

        Map<String, Object> caseData = caseDetails.getData();
        boolean isConsented = caseDataService.isConsentedApplication(caseDetails);
        String appSolicitorAddressField = isConsented ? CONSENTED_SOLICITOR_ADDRESS : CONTESTED_SOLICITOR_ADDRESS;
        String solicitorAddressField = isApplicant ? appSolicitorAddressField : RESP_SOLICITOR_ADDRESS;

        //isRepresented Boolean
        caseData.put(isApplicant ? APPLICANT_REPRESENTED : getRespondentRepresentedKey(caseDetails), YES_VALUE);
        OrganisationsResponse organisationsResponse = organisationService
            .findOrganisationByOrgId(addedSolicitor.getOrganisation().getOrganisationID());

        //address
        caseData.put(solicitorAddressField,
            updateSolicitorDetailsService.convertOrganisationAddressToSolicitorAddress(organisationsResponse));

        //Other contact fields
        return updateSolicitorDetailsService.updateSolicitorContactDetails(addedSolicitor, caseData,
            isConsented, isApplicant);
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

    // Manage case assignment's API only accepts CaseRoleId as the selected element of a dynamic list
    // and not just as a simple string, so we have to do this ugly cast to get the API to process our COR
    private DynamicList generateCaseRoleIdDynamicListElementAsList(String role) {
        final DynamicListElement roleItem = DynamicListElement.builder()
            .code(role)
            .label(role)
            .build();

        return DynamicList.builder()
            .value(roleItem)
            .listItems(List.of(roleItem))
            .build();
    }
}
