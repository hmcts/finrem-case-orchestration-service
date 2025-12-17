package uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.nocworkflows;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangeOfRepresentationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangeOrganisationApprovalStatus;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangeOrganisationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangedRepresentative;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Element;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Organisation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrganisationPolicy;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RepresentationUpdateHistory;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RepresentationUpdateHistoryCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ChangeOfRepresentationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.solicitors.AddedSolicitorService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.solicitors.RemovedSolicitorService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static org.apache.commons.collections4.ListUtils.emptyIfNull;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_ORGANISATION_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CHANGE_ORGANISATION_REQUEST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NOC_PARTY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.REPRESENTATION_UPDATE_HISTORY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_ORGANISATION_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangeOfRepresentationRequest.APPLICANT_PARTY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangeOfRepresentationRequest.RESPONDENT_PARTY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NoticeOfChangeParty.isApplicantForRepresentationChange;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NoticeOfChangeParty.isRespondentForRepresentationChange;
import static uk.gov.hmcts.reform.finrem.caseorchestration.utils.ListUtils.nullIfEmpty;

@Service
@RequiredArgsConstructor
@Slf4j
public class NoticeOfChangeService {

    private final CaseDataService caseDataService;
    private final IdamService idamService;
    private final ChangeOfRepresentationService changeOfRepresentationService;
    private final ObjectMapper objectMapper;
    private final AddedSolicitorService addedSolicitorService;
    private final RemovedSolicitorService removedSolicitorService;

    @Autowired
    public NoticeOfChangeService(CaseDataService caseDataService,
                                 IdamService idamService,
                                 ChangeOfRepresentationService changeOfRepresentationService,
                                 AddedSolicitorService addedSolicitorService,
                                 RemovedSolicitorService removedSolicitorService) {
        this.caseDataService = caseDataService;
        this.idamService = idamService;
        this.objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.changeOfRepresentationService = changeOfRepresentationService;
        this.addedSolicitorService = addedSolicitorService;
        this.removedSolicitorService = removedSolicitorService;
    }

    @Deprecated
    public Map<String, Object> updateRepresentation(CaseDetails caseDetails,
                                                    String authorizationToken,
                                                    CaseDetails originalCaseDetails) {
        log.info("About to start updating representation as caseworker for Case ID: {}", caseDetails.getId());

        Map<String, Object> caseData = updateRepresentationUpdateHistory(caseDetails, authorizationToken, originalCaseDetails);
        final ChangeOrganisationRequest changeRequest = generateChangeOrganisationRequest(caseDetails, originalCaseDetails);
        caseData.put(CHANGE_ORGANISATION_REQUEST, changeRequest);

        return caseData;
    }

    private Map<String, Object> updateRepresentationUpdateHistory(CaseDetails caseDetails,
                                                                  String authToken,
                                                                  CaseDetails originalDetails) {
        Map<String, Object> caseData = caseDetails.getData();
        RepresentationUpdateHistory current = buildCurrentUpdateHistory(caseData);

        final RepresentationUpdateHistory history = changeOfRepresentationService.generateRepresentationUpdateHistory(
            buildChangeOfRepresentationRequest(authToken, caseDetails, current, originalDetails));

        caseData.put(REPRESENTATION_UPDATE_HISTORY, history.getRepresentationUpdateHistory());
        return caseData;
    }

    /**
     * Updates the representation update history on the given {@link FinremCaseData} object.
     *
     * <p>This method builds the current representation change details and sends them to the
     * {@code changeOfRepresentationService} to generate an updated history. The returned
     * history is then copied onto the {@code finremCaseData} object.</p>
     *
     * <p>The {@code viaEventType} parameter indicates the event type that triggered this update.</p>
     *
     * @param finremCaseData          the case data to update
     * @param originalFinremCaseData  the original case data before any changes
     * @param viaEventType            the event type that triggered this update
     * @param authToken               the authorisation token used for the request
     */
    public void updateRepresentationUpdateHistory(FinremCaseData finremCaseData,
                                                  FinremCaseData originalFinremCaseData,
                                                  EventType viaEventType,
                                                  String authToken) {
        ChangeOfRepresentationRequest changeOfRepresentationRequest = buildChangeOfRepresentationRequest(authToken,
            finremCaseData, originalFinremCaseData);
        boolean hasChange = changeOfRepresentationRequest != null;

        if (hasChange) {
            RepresentationUpdateHistory history = changeOfRepresentationService.generateRepresentationUpdateHistory(
                changeOfRepresentationRequest, viaEventType);

            // modifying finremCaseData reference object
            finremCaseData.setRepresentationUpdateHistory(
                nullIfEmpty(history.getRepresentationUpdateHistory()).stream()
                    .map(element -> RepresentationUpdateHistoryCollection.builder()
                        .id(element.getId())
                        .value(element.getValue())
                        .build())
                    .collect(Collectors.toList())
            );
        }
    }

    /**
     * Populates the {@code ChangeOrganisationRequest} field on the given {@link FinremCaseData}.
     *
     * <p>This method generates a new {@code ChangeOrganisationRequest} using the current and
     * original case data, and then sets it on the {@code finremCaseData} object.</p>
     *
     * @param finremCaseData          the case data to update
     * @param originalFinremCaseData  the original case data before any organisation changes
     */
    public void populateChangeOrganisationRequestField(FinremCaseData finremCaseData, FinremCaseData originalFinremCaseData) {
        ChangeOrganisationRequest changeRequest = generateChangeOrganisationRequest(finremCaseData, originalFinremCaseData);
        if (changeRequest != null) {
            finremCaseData.setChangeOrganisationRequestField(changeRequest);
        }
    }

    private ChangeOrganisationRequest generateChangeOrganisationRequest(CaseDetails caseDetails,
                                                                        CaseDetails originalDetails) {

        final boolean isApplicant = ((String) caseDetails.getData().get(NOC_PARTY)).equalsIgnoreCase(APPLICANT_PARTY);
        final String litigantOrgPolicy = isApplicant ? APPLICANT_ORGANISATION_POLICY : RESPONDENT_ORGANISATION_POLICY;
        final DynamicList role = generateCaseRoleIdAsDynamicList(isApplicant ? CaseRole.APP_SOLICITOR.getCcdCode()
            : CaseRole.RESP_SOLICITOR.getCcdCode());

        final Organisation organisationToAdd = ofNullable(getOrgPolicy(caseDetails, litigantOrgPolicy))
            .map(OrganisationPolicy::getOrganisation).orElse(null);

        final Organisation organisationToRemove = ofNullable(getOrgPolicy(originalDetails, litigantOrgPolicy))
            .map(OrganisationPolicy::getOrganisation).orElse(null);

        return buildChangeOrganisationRequest(role, organisationToAdd, organisationToRemove);
    }

    private ChangeOrganisationRequest generateChangeOrganisationRequest(FinremCaseData finremCaseData,
                                                                        FinremCaseData originalFinremCaseData) {

        CaseRole caseRole = null;
        OrganisationPolicy organisationPolicy = null;
        if (isApplicantForRepresentationChange(finremCaseData)) {
            organisationPolicy = finremCaseData.getApplicantOrganisationPolicy();
            caseRole = CaseRole.APP_SOLICITOR;
        } else if (isRespondentForRepresentationChange(finremCaseData)) {
            organisationPolicy = finremCaseData.getRespondentOrganisationPolicy();
            caseRole = CaseRole.RESP_SOLICITOR;
        }

        if (caseRole == null) {
            log.info("{} - No ChangeOrganisationRequest generated implies it's triggered by intervener.",
                finremCaseData.getCcdCaseId());
            return null;
        }

        OrganisationPolicy originalOrganisationPolicy = null;
        if (isApplicantForRepresentationChange(finremCaseData)) {
            originalOrganisationPolicy = originalFinremCaseData.getApplicantOrganisationPolicy();
        } else if (isRespondentForRepresentationChange(finremCaseData)) {
            originalOrganisationPolicy = originalFinremCaseData.getRespondentOrganisationPolicy();
        }

        DynamicList role = generateCaseRoleIdAsDynamicList(caseRole.getCcdCode());
        Organisation organisationToAdd = ofNullable(organisationPolicy)
            .map(OrganisationPolicy::getOrganisation).orElse(null);
        Organisation organisationToRemove = ofNullable(originalOrganisationPolicy)
            .map(OrganisationPolicy::getOrganisation).orElse(null);

        boolean noChange = organisationToAdd == null && organisationToRemove == null;
        if (noChange) {
            log.info("{} - No ChangeOrganisationRequest generated due to no changes on organisation policy",
                finremCaseData.getCcdCaseId());
            return null;
        }

        return buildChangeOrganisationRequest(role, organisationToAdd, organisationToRemove);
    }

    private OrganisationPolicy getOrgPolicy(CaseDetails caseDetails, String orgPolicy) {
        return objectMapper.convertValue(caseDetails.getData().get(orgPolicy),
            OrganisationPolicy.class);
    }

    private ChangeOrganisationRequest buildChangeOrganisationRequest(DynamicList role,
                                                                     Organisation organisationToAdd,
                                                                     Organisation organisationToRemove) {
        return ChangeOrganisationRequest.builder()
            .caseRoleId(role)
            .requestTimestamp(LocalDateTime.now())
            .approvalRejectionTimestamp(LocalDateTime.now())
            .approvalStatus(ChangeOrganisationApprovalStatus.APPROVED)
            .organisationToAdd(organisationToAdd)
            .organisationToRemove(organisationToRemove)
            .build();
    }

    private ChangeOfRepresentationRequest buildChangeOfRepresentationRequest(String authToken,
                                                                             CaseDetails caseDetails,
                                                                             RepresentationUpdateHistory current,
                                                                             CaseDetails originalDetails) {
        final boolean isApplicant = ((String) caseDetails.getData().get(NOC_PARTY)).equalsIgnoreCase(APPLICANT_PARTY);
        return ChangeOfRepresentationRequest.builder()
            .by(idamService.getIdamFullName(authToken))
            .party(isApplicant ? APPLICANT_PARTY : RESPONDENT_PARTY)
            .clientName(getClientName(caseDetails, isApplicant))
            .current(current)
            .addedRepresentative(addedSolicitorService.getAddedSolicitorAsCaseworker(caseDetails))
            .removedRepresentative(removedSolicitorService.getRemovedSolicitorAsCaseworker(originalDetails, isApplicant))
            .build();
    }

    private ChangeOfRepresentationRequest buildChangeOfRepresentationRequest(String authToken,
                                                                             FinremCaseData finremCaseData,
                                                                             FinremCaseData originalFinremCaseData) {
        ChangedRepresentative added = addedSolicitorService.getAddedSolicitorAsCaseworker(finremCaseData);
        ChangedRepresentative removed = removedSolicitorService.getChangedRepresentative(finremCaseData,
            originalFinremCaseData);
        if (added == null && removed == null) {
            // no change
            return null;
        }
        return ChangeOfRepresentationRequest.builder()
            .by(idamService.getIdamFullName(authToken))
            .party(getParty(finremCaseData))
            .clientName(getClientName(finremCaseData))
            .current(buildCurrentUpdateHistory(finremCaseData))
            .addedRepresentative(added)
            .removedRepresentative(removed)
            .build();
    }

    private RepresentationUpdateHistory buildCurrentUpdateHistory(Map<String, Object> caseData) {
        return RepresentationUpdateHistory.builder()
            .representationUpdateHistory(objectMapper.convertValue(caseData.get(REPRESENTATION_UPDATE_HISTORY),
                new TypeReference<>() {
                }))
            .build();
    }

    private RepresentationUpdateHistory buildCurrentUpdateHistory(FinremCaseData finremCaseData) {
        return RepresentationUpdateHistory.builder()
            .representationUpdateHistory(
                emptyIfNull(finremCaseData.getRepresentationUpdateHistory()).stream().map(
                    a -> Element.element(a.getId(), a.getValue())
                ).collect(Collectors.toList())
            )
            .build();
    }

    // aac handles org policy modification based on the Change Organisation Request,
    // so we need to revert the org policies to their value before the event started
    public CaseDetails persistOriginalOrgPoliciesWhenRevokingAccess(CaseDetails caseDetails,
                                                                    CaseDetails originalCaseDetails) {
        final boolean isApplicant = ((String) caseDetails.getData().get(NOC_PARTY)).equalsIgnoreCase(APPLICANT_PARTY);
        final String litigantOrgPolicy = isApplicant ? APPLICANT_ORGANISATION_POLICY : RESPONDENT_ORGANISATION_POLICY;

        if (hasInvalidOrgPolicy(caseDetails, isApplicant)) {
            caseDetails.getData().put(litigantOrgPolicy, getOrgPolicy(originalCaseDetails, litigantOrgPolicy));
        }
        return caseDetails;
    }

    public boolean hasInvalidOrgPolicy(CaseDetails caseDetails, boolean isApplicant) {
        Optional<OrganisationPolicy> orgPolicy = ofNullable(getOrgPolicy(caseDetails, isApplicant
            ? APPLICANT_ORGANISATION_POLICY
            : RESPONDENT_ORGANISATION_POLICY));

        return orgPolicy.isEmpty()
            || orgPolicy.get().getOrgPolicyCaseAssignedRole() == null
            || !orgPolicy.get().getOrgPolicyCaseAssignedRole().equalsIgnoreCase(
            isApplicant ? CaseRole.APP_SOLICITOR.getCcdCode() : CaseRole.RESP_SOLICITOR.getCcdCode());
    }

    private DynamicList generateCaseRoleIdAsDynamicList(String role) {
        final DynamicListElement roleItem = DynamicListElement.builder()
            .code(role)
            .label(role)
            .build();

        return DynamicList.builder()
            .value(roleItem)
            .listItems(List.of(roleItem))
            .build();
    }

    private String getClientName(CaseDetails caseDetails, boolean isApplicant) {
        return isApplicant
            ? caseDataService.buildFullApplicantName(caseDetails)
            : caseDataService.buildFullRespondentName(caseDetails);
    }

    private String getClientName(FinremCaseData finremCaseData) {
        if (isApplicantForRepresentationChange(finremCaseData)) {
            return finremCaseData.getFullApplicantName();
        } else if (isRespondentForRepresentationChange(finremCaseData)) {
            return finremCaseData.getRespondentFullName();
        } else {
            return null;
        }
    }

    private String getParty(FinremCaseData finremCaseData) {
        if (isApplicantForRepresentationChange(finremCaseData)) {
            return APPLICANT_PARTY;
        } else if (isRespondentForRepresentationChange(finremCaseData)) {
            return RESPONDENT_PARTY;
        } else {
            return null;
        }
    }
}
