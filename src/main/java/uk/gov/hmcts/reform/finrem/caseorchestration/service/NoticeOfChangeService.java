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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangeOfRepresentatives;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangeOrganisationApprovalStatus;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangeOrganisationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangedRepresentative;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Organisation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrganisationPolicy;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_ORGANISATION_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_SOLICITOR_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CHANGE_ORGANISATION_REQUEST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_RESPONDENT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_RESPONDENT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NOC_PARTY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_ORGANISATION_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_EMAIL;

@Service
@RequiredArgsConstructor
@Slf4j
public class NoticeOfChangeService {

    private static final String APPLICANT = "applicant";
    private static final String CHANGE_OF_REPS = "ChangeOfRepresentatives";

    private final CaseDataService caseDataService;
    private final IdamService idamService;
    private final ChangeOfRepresentationService changeOfRepresentationService;
    private final ObjectMapper objectMapper;
    private boolean isApplicant;

    @Autowired
    public NoticeOfChangeService(CaseDataService caseDataService,
                                 IdamService idamService,
                                 ChangeOfRepresentationService changeOfRepresentationService) {
        this.caseDataService = caseDataService;
        this.idamService = idamService;
        this.objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.changeOfRepresentationService = changeOfRepresentationService;
    }

    public Map<String, Object> updateRepresentation(CaseDetails caseDetails,
                                                    String authorizationToken,
                                                    CaseDetails originalCaseDetails) {
        log.info("CaseDetails for caseID{}:{}", caseDetails.getId(), caseDetails);
        log.info("Original CaseDetails for caseID{}:{}", caseDetails.getId(), caseDetails);
        log.info("Applicant Org policy before: {}", originalCaseDetails.getData().get(APPLICANT_ORGANISATION_POLICY));
        log.info("Applicant Org policy after: {}", caseDetails.getData().get(APPLICANT_ORGANISATION_POLICY));

        Map<String,Object> caseData = caseDetails.getData();
        isApplicant = ((String) caseData.get(NOC_PARTY)).equalsIgnoreCase(APPLICANT);

        caseData = updateChangeOfRepresentatives(caseDetails, authorizationToken, originalCaseDetails);
        log.info("added changeOfRepresentatives to case with caseID {}", caseDetails.getId());
        caseData.put(CHANGE_ORGANISATION_REQUEST, generateChangeOrganisationRequest(caseDetails, originalCaseDetails));
        caseData.put(APPLICANT_ORGANISATION_POLICY, originalCaseDetails.getData().get(APPLICANT_ORGANISATION_POLICY));

        return caseData;
    }

    private Map<String, Object> updateChangeOfRepresentatives(CaseDetails caseDetails,
                                                              String authToken,
                                                              CaseDetails originalDetails) {
        Map<String, Object> caseData = caseDetails.getData();

        ChangeOfRepresentatives current = ChangeOfRepresentatives.builder()
            .changeOfRepresentation(objectMapper.convertValue(caseData.get(CHANGE_OF_REPS),
                new TypeReference<>() {}))
            .build();

        ChangeOfRepresentatives change = changeOfRepresentationService.generateChangeOfRepresentatives(
            ChangeOfRepresentationRequest.builder()
                .by(idamService.getIdamFullName(authToken))
                .party(isApplicant ? APPLICANT : "respondent")
                .clientName(isApplicant ? caseDataService.buildFullApplicantName(caseDetails)
                    : caseDataService.buildFullRespondentName(caseDetails))
                .current(current)
                .addedRepresentative(getAddedRep(caseDetails))
                .removedRepresentative(getRemovedRepresentative(originalDetails))
                .build()
        );

        caseData.put(CHANGE_OF_REPS, change.getChangeOfRepresentation());
        return caseData;
    }

    private ChangeOrganisationRequest generateChangeOrganisationRequest(CaseDetails caseDetails,
                                                                        CaseDetails originalDetails) {

        DynamicList role = generateCaseRoleIdDynamicListElementAsList(isApplicant ? APP_SOLICITOR_POLICY
            : RESP_SOLICITOR_POLICY);
        Organisation organisationToAdd = Optional.ofNullable(getOrganisationPolicy(caseDetails.getData()))
            .map(OrganisationPolicy::getOrganisation).orElse(null);
        Organisation organisationToRemove = Optional.ofNullable(getOrganisationPolicy(originalDetails.getData()))
            .map(OrganisationPolicy::getOrganisation).orElse(null);
        log.info("Generating Change Organisation Request for case with CaseID {}", caseDetails.getId());
        return ChangeOrganisationRequest.builder()
            .caseRoleId(role)
            .requestTimestamp(LocalDateTime.now())
            .approvalRejectionTimestamp(LocalDateTime.now())
            .approvalStatus(ChangeOrganisationApprovalStatus.APPROVED)
            .organisationToAdd(organisationToAdd)
            .organisationToRemove(organisationToRemove)
            .build();
    }

    private ChangedRepresentative getRemovedRepresentative(CaseDetails caseDetails) {
        Map<String, Object> caseData = caseDetails.getData();
        String representedKey = isApplicant ? APPLICANT_REPRESENTED : getRespondentRepresentedKey(caseDetails);
        if (caseData.get(representedKey).equals(YES_VALUE)) {
            return ChangedRepresentative.builder()
                .name(isApplicant
                    ? getApplicantSolicitorName(caseDetails)
                    : (String) caseData.get(RESP_SOLICITOR_NAME))
                .email(isApplicant
                    ? getApplicantSolicitorEmail(caseDetails)
                    : (String) caseData.get(RESP_SOLICITOR_EMAIL))
                .organisation(getOrganisationPolicy(caseData).getOrganisation())
                .build();
        }
        return null;
    }

    private ChangedRepresentative getAddedRep(CaseDetails caseDetails) {
        Map<String, Object> caseData = caseDetails.getData();
        return Optional.ofNullable(getOrganisationPolicy(caseData).getOrganisation())
            .map(org -> ChangedRepresentative.builder()
                .name(isApplicant
                    ? getApplicantSolicitorName(caseDetails)
                    : (String) caseData.get(RESP_SOLICITOR_NAME))
                .email(isApplicant
                    ? getApplicantSolicitorEmail(caseDetails)
                    : (String) caseData.get(RESP_SOLICITOR_EMAIL))
                .organisation(org)
                .build()).orElse(null);
    }

    private OrganisationPolicy getOrganisationPolicy(Map<String, Object> caseData) {

        return isApplicant
            ? objectMapper.convertValue(caseData.get(APPLICANT_ORGANISATION_POLICY), OrganisationPolicy.class)
            : objectMapper.convertValue(caseData.get(RESPONDENT_ORGANISATION_POLICY), OrganisationPolicy.class);
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

}
