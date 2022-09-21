package uk.gov.hmcts.reform.finrem.caseorchestration.service.barristers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.BarristerUpdateDifferenceCalculator;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.BarristerChange;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Barrister;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.BarristerData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangeOfRepresentationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangedRepresentative;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Element;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RepresentationUpdate;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RepresentationUpdateHistory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignCaseAccessService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ChangeOfRepresentationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PrdOrganisationService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_BARRISTER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_BARRISTER_ROLE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_SOLICITOR_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CASE_ROLE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.REPRESENTATION_UPDATE_HISTORY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_BARRISTER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_BARRISTER_ROLE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_POLICY;

@Slf4j
@RequiredArgsConstructor
@Service
public class ManageBarristerService {

    public static final String MANAGE_BARRISTER_PARTY = "barristerParty";
    public static final String MANAGE_BARRISTERS = "Manage Barristers";

    private final BarristerUpdateDifferenceCalculator barristerUpdateDifferenceCalculator;
    private final ChangeOfRepresentationService changeOfRepresentationService;
    private final AssignCaseAccessService assignCaseAccessService;
    private final PrdOrganisationService organisationService;
    private final IdamService idamService;
    private final CaseDataService caseDataService;
    private final ObjectMapper objectMapper;

    public List<BarristerData> getBarristersForParty(CaseDetails caseDetails) {
        String caseRole = getCaseRole(caseDetails);

        return Optional.ofNullable(getBarristerCollection(caseDetails, caseRole)).orElse(new ArrayList<>());
    }

    public Map<String, Object> updateBarristerAccess(CaseDetails caseDetails,
                                                     List<Barrister> barristers,
                                                     List<Barrister> barristersBeforeEvent,
                                                     String authToken) {
        final BarristerChange barristerChange = barristerUpdateDifferenceCalculator.calculate(barristersBeforeEvent, barristers);
        final String caseRole = getBarristerCaseRole(caseDetails);

        barristerChange.getAdded().forEach(userToBeAdded -> addUser(caseDetails, authToken, caseRole, userToBeAdded));

        return updateRepresentationUpdateHistoryForCase(caseDetails, barristerChange, authToken);
    }

    private void addUser(CaseDetails caseDetails, String authToken, String caseRole, Barrister userToBeAdded) {
        organisationService.findUserByEmail(userToBeAdded.getEmail(), authToken)
            .ifPresentOrElse(
                userId -> assignCaseAccessService.grantCaseRoleToUser(caseDetails.getId(), userId, caseRole, authToken),
                throwMissingUserException(userToBeAdded)
            );
    }

    private Map<String, Object> updateRepresentationUpdateHistoryForCase(CaseDetails caseDetails,
                                                                         BarristerChange barristerChange,
                                                                         String authToken) {
        barristerChange.getAdded().forEach(addedUser -> {
            RepresentationUpdateHistory representationUpdateHistory =
                changeOfRepresentationService.generateRepresentationUpdateHistory(
                    ChangeOfRepresentationRequest.builder()
                        .addedRepresentative(convertToChangedRepresentative(addedUser))
                        .by(idamService.getIdamFullName(authToken))
                        .via(MANAGE_BARRISTERS)
                        .clientName(getClientName(caseDetails))
                        .party(getManageBarristerParty(caseDetails))
                        .current(getRepresentationUpdateHistory(caseDetails))
                        .build());
            caseDetails.getData().put(REPRESENTATION_UPDATE_HISTORY,
                representationUpdateHistory.getRepresentationUpdateHistory());
        });

        return caseDetails.getData();
    }

    private String getCaseRole(CaseDetails caseDetails) {
        String caseRole = Objects.toString(caseDetails.getData().get(CASE_ROLE), StringUtils.EMPTY);

        if (!List.of(APP_SOLICITOR_POLICY, RESP_SOLICITOR_POLICY).contains(caseRole)) {
            String caseworkerParty = Objects.toString(caseDetails.getData().get(MANAGE_BARRISTER_PARTY), StringUtils.EMPTY);
            caseRole = APPLICANT.equalsIgnoreCase(caseworkerParty) ? APP_SOLICITOR_POLICY : RESP_SOLICITOR_POLICY;
        }

        return caseRole;
    }

    private String getBarristerCaseRole(CaseDetails caseDetails) {
        return APP_SOLICITOR_POLICY.equals(getCaseRole(caseDetails)) ? APPLICANT_BARRISTER_ROLE : RESPONDENT_BARRISTER_ROLE;
    }

    private String getBarristerCollectionKey(String caseRole) {
        return APP_SOLICITOR_POLICY.equals(caseRole) ? APPLICANT_BARRISTER_COLLECTION : RESPONDENT_BARRISTER_COLLECTION;
    }

    private ChangedRepresentative convertToChangedRepresentative(Barrister barrister) {
        return ChangedRepresentative.builder()
            .name(barrister.getName())
            .email(barrister.getEmail())
            .organisation(barrister.getOrganisation())
            .build();
    }

    private RepresentationUpdateHistory getRepresentationUpdateHistory(CaseDetails caseDetails) {
        return RepresentationUpdateHistory.builder()
            .representationUpdateHistory(Optional.ofNullable(getUpdateHistory(caseDetails)).orElse(new ArrayList<>()))
            .build();
    }

    private String getClientName(CaseDetails caseDetails) {
        return APP_SOLICITOR_POLICY.equals(getCaseRole(caseDetails))
            ? caseDataService.buildFullApplicantName(caseDetails)
            : caseDataService.buildFullRespondentName(caseDetails);
    }

    private String getManageBarristerParty(CaseDetails caseDetails) {
        return APP_SOLICITOR_POLICY.equals(getCaseRole(caseDetails)) ? APPLICANT : RESPONDENT;
    }

    private List<BarristerData> getBarristerCollection(CaseDetails caseDetails, String caseRole) {
        return objectMapper.convertValue(caseDetails.getData().get(getBarristerCollectionKey(caseRole)), new TypeReference<>() {});
    }

    private List<Element<RepresentationUpdate>> getUpdateHistory(CaseDetails caseDetails) {
        return objectMapper.convertValue(caseDetails.getData().get(REPRESENTATION_UPDATE_HISTORY), new TypeReference<>() {});
    }

    private Runnable throwMissingUserException(Barrister userToBeAdded) {
        return () -> {
            throw new IllegalArgumentException(String.format("Could not find the user with email %s",
                userToBeAdded.getEmail()));
        };
    }
}
