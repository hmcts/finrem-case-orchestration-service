package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangeOfRepresentationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangeOrganisationApprovalStatus;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangeOrganisationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Organisation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrganisationPolicy;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RepresentationUpdateHistory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.solicitors.GetAddedSolicitorService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.solicitors.GetRemovedSolicitorService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_ORGANISATION_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_SOLICITOR_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CHANGE_ORGANISATION_REQUEST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NOC_PARTY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_ORGANISATION_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_POLICY;

@Service
@RequiredArgsConstructor
@Slf4j
public class NoticeOfChangeService {

    private static final String REPRESENTATION_UPDATE_HISTORY = "RepresentationUpdateHistory";

    private final CaseDataService caseDataService;
    private final IdamService idamService;
    private final ChangeOfRepresentationService changeOfRepresentationService;
    private final ObjectMapper objectMapper;
    private final GetAddedSolicitorService getAddedSolicitorService;
    private final GetRemovedSolicitorService getRemovedSolicitorService;

    @Autowired
    public NoticeOfChangeService(CaseDataService caseDataService,
                                 IdamService idamService,
                                 ChangeOfRepresentationService changeOfRepresentationService,
                                 GetAddedSolicitorService getAddedSolicitorService,
                                 GetRemovedSolicitorService getRemovedSolicitorService) {
        this.caseDataService = caseDataService;
        this.idamService = idamService;
        this.objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.changeOfRepresentationService = changeOfRepresentationService;
        this.getAddedSolicitorService = getAddedSolicitorService;
        this.getRemovedSolicitorService = getRemovedSolicitorService;
    }

    public Map<String, Object> updateRepresentation(CaseDetails caseDetails,
                                                    String authorizationToken,
                                                    CaseDetails originalCaseDetails) {
        log.info("About to start updating representation as caseworker for caseID {}", caseDetails.getId());

        Map<String,Object> caseData = updateRepresentationUpdateHistory(caseDetails, authorizationToken, originalCaseDetails);
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

    // aac handles org policy modification based on the Change Organisation Request,
    // so we want to revert the org policies to their value before the event started
    public CaseDetails persistOriginalOrgPoliciesWhenRevokingAccess(CaseDetails caseDetails,
                                                                    CaseDetails originalCaseDetails) {
        final boolean isApplicant = ((String)caseDetails.getData().get(NOC_PARTY)).equalsIgnoreCase(APPLICANT);
        final String litigantOrgPolicy = isApplicant ? APPLICANT_ORGANISATION_POLICY : RESPONDENT_ORGANISATION_POLICY;

        if (hasInvalidOrgPolicy(caseDetails, isApplicant)) {
            caseDetails.getData().put(litigantOrgPolicy, getOrgPolicy(originalCaseDetails, litigantOrgPolicy));
        }
        return caseDetails;
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
            .clientName(isApplicant ? caseDataService.buildFullApplicantName(caseDetails)
                : caseDataService.buildFullRespondentName(caseDetails))
            .current(current)
            .addedRepresentative(getAddedSolicitorService.getAddedSolicitorAsCaseworker(caseDetails))
            .removedRepresentative(getRemovedSolicitorService.getRemovedSolicitorAsCaseworker(originalDetails))
            .build();
    }

    private RepresentationUpdateHistory buildCurrentUpdateHistory(Map<String, Object> caseData) {
        return RepresentationUpdateHistory.builder()
            .representationUpdateHistory(objectMapper.convertValue(caseData.get(REPRESENTATION_UPDATE_HISTORY),
                new TypeReference<>() {}))
            .build();
    }

    private boolean hasInvalidOrgPolicy(CaseDetails caseDetails, boolean isApplicant) {
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
}
