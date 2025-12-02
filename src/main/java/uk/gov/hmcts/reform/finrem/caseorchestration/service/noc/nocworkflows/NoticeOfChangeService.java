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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangeOfRepresentationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangeOrganisationApprovalStatus;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangeOrganisationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Element;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NoticeOfChangeParty;
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

import static org.apache.commons.collections4.ListUtils.emptyIfNull;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_ORGANISATION_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_SOLICITOR_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CHANGE_ORGANISATION_REQUEST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NOC_PARTY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.REPRESENTATION_UPDATE_HISTORY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_ORGANISATION_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NoticeOfChangeParty.isApplicantForRepresentationChange;
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
        finremCaseData.setChangeOrganisationRequestField(changeRequest);
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
        RepresentationUpdateHistory current = buildCurrentUpdateHistory(finremCaseData);

        RepresentationUpdateHistory history = changeOfRepresentationService.generateRepresentationUpdateHistory(
            buildChangeOfRepresentationRequest(authToken, finremCaseData, current, originalFinremCaseData), viaEventType);

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

    private ChangeOrganisationRequest generateChangeOrganisationRequest(CaseDetails caseDetails,
                                                                        CaseDetails originalDetails) {

        final boolean isApplicant = ((String) caseDetails.getData().get(NOC_PARTY)).equalsIgnoreCase(APPLICANT);
        final String litigantOrgPolicy = isApplicant ? APPLICANT_ORGANISATION_POLICY : RESPONDENT_ORGANISATION_POLICY;
        final DynamicList role = generateCaseRoleIdAsDynamicList(isApplicant ? APP_SOLICITOR_POLICY : RESP_SOLICITOR_POLICY);

        final Organisation organisationToAdd = Optional.ofNullable(getOrgPolicy(caseDetails, litigantOrgPolicy))
            .map(OrganisationPolicy::getOrganisation).orElse(null);

        final Organisation organisationToRemove = Optional.ofNullable(getOrgPolicy(originalDetails, litigantOrgPolicy))
            .map(OrganisationPolicy::getOrganisation).orElse(null);

        return buildChangeOrganisationRequest(role, organisationToAdd, organisationToRemove);
    }

    private ChangeOrganisationRequest generateChangeOrganisationRequest(FinremCaseData finremCaseData,
                                                                        FinremCaseData originalFinremCaseData) {
        final boolean isApplicant = NoticeOfChangeParty.isApplicantForRepresentationChange(finremCaseData);
        OrganisationPolicy organisationPolicy = isApplicant ? finremCaseData.getApplicantOrganisationPolicy()
            : finremCaseData.getRespondentOrganisationPolicy();
        OrganisationPolicy originalOrganisationPolicy = isApplicant ? originalFinremCaseData.getApplicantOrganisationPolicy()
            : originalFinremCaseData.getRespondentOrganisationPolicy();

        final DynamicList role = generateCaseRoleIdAsDynamicList(isApplicant ? APP_SOLICITOR_POLICY : RESP_SOLICITOR_POLICY);

        final Organisation organisationToAdd = Optional.ofNullable(organisationPolicy)
            .map(OrganisationPolicy::getOrganisation).orElse(null);

        final Organisation organisationToRemove = Optional.ofNullable(originalOrganisationPolicy)
            .map(OrganisationPolicy::getOrganisation).orElse(null);

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
        final boolean isApplicant = ((String) caseDetails.getData().get(NOC_PARTY)).equalsIgnoreCase(APPLICANT);
        return ChangeOfRepresentationRequest.builder()
            .by(idamService.getIdamFullName(authToken))
            .party(isApplicant ? APPLICANT : RESPONDENT)
            .clientName(getClientName(caseDetails, isApplicant))
            .current(current)
            .addedRepresentative(addedSolicitorService.getAddedSolicitorAsCaseworker(caseDetails))
            .removedRepresentative(removedSolicitorService.getRemovedSolicitorAsCaseworker(originalDetails, isApplicant))
            .build();
    }

    private ChangeOfRepresentationRequest buildChangeOfRepresentationRequest(String authToken,
                                                                             FinremCaseData finremCaseData,
                                                                             RepresentationUpdateHistory current,
                                                                             FinremCaseData originalFinremCaseData) {
        final boolean isApplicant = isApplicantForRepresentationChange(finremCaseData);

        return ChangeOfRepresentationRequest.builder()
            .by(idamService.getIdamFullName(authToken))
            .party(isApplicant ? APPLICANT : RESPONDENT)
            .clientName(getClientName(finremCaseData, isApplicant))
            .current(current)
            .addedRepresentative(addedSolicitorService.getAddedSolicitorAsCaseworker(finremCaseData))
            .removedRepresentative(removedSolicitorService.getRemovedSolicitorAsCaseworker(originalFinremCaseData, isApplicant))
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
        final boolean isApplicant = ((String) caseDetails.getData().get(NOC_PARTY)).equalsIgnoreCase(APPLICANT);
        final String litigantOrgPolicy = isApplicant ? APPLICANT_ORGANISATION_POLICY : RESPONDENT_ORGANISATION_POLICY;

        if (hasInvalidOrgPolicy(caseDetails, isApplicant)) {
            caseDetails.getData().put(litigantOrgPolicy, getOrgPolicy(originalCaseDetails, litigantOrgPolicy));
        }
        return caseDetails;
    }

    public boolean hasInvalidOrgPolicy(CaseDetails caseDetails, boolean isApplicant) {
        Optional<OrganisationPolicy> orgPolicy = Optional.ofNullable(getOrgPolicy(caseDetails, isApplicant
            ? APPLICANT_ORGANISATION_POLICY
            : RESPONDENT_ORGANISATION_POLICY));

        return orgPolicy.isEmpty()
            || orgPolicy.get().getOrgPolicyCaseAssignedRole() == null
            || !orgPolicy.get().getOrgPolicyCaseAssignedRole().equalsIgnoreCase(
            isApplicant ? APP_SOLICITOR_POLICY : RESP_SOLICITOR_POLICY);
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

    private String getClientName(FinremCaseData finremCaseData, boolean isApplicant) {
        return isApplicant ? finremCaseData.getFullApplicantName()
            : finremCaseData.getRespondentFullName();
    }
}
