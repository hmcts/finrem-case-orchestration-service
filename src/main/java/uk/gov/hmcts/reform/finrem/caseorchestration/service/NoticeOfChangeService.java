package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangeOfRepresentation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangeOfRepresentatives;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangedRepresentative;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Organisation;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CA_UPDATE_ADD_REPRESENTATION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CHANGE_OF_REPRESENTATIVES;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NOC_IS_SOL_DIGITAL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NOC_NEW_SOL_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NOC_NEW_SOL_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NOC_NEW_SOL_ORGANISATION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NOC_PARTY;

@Service
@RequiredArgsConstructor
@Slf4j
public class NoticeOfChangeService {

    private static final String APPLICANT = "applicant";
    private static final String RESPONDENT = "respondent";
    private static final String NOTICE_OF_CHANGE = "Notice of Change";

    CaseDataService caseDataService;
    IdamService idamService;
    ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    public Map<String, Object> updateRepresentation(CaseDetails caseDetails,
                                                    String authorizationToken,
                                                    String caseEventId) {

        Map<String,Object> caseData = caseDetails.getData();

        ChangedRepresentative changedRepresentative = generateChangedRepresentative(caseDetails);

        ChangeOfRepresentation changeOfRepresentation = generateChangeOfRepresentation(caseDetails, authorizationToken,
            changedRepresentative, CA_UPDATE_ADD_REPRESENTATION.equalsIgnoreCase(caseEventId));

        ChangeOfRepresentatives changeOfRepresentatives = updateChangeOfRepresentatives(caseDetails, changeOfRepresentation);

        if (caseData.get(NOC_IS_SOL_DIGITAL).equals("Yes")) {
            //need to create a mirror org policy that doesn't require id, else
            return null;
        }

        if (caseDetails.getData().get(NOC_PARTY).equals(APPLICANT)) {
            return null;
        } else if (caseDetails.getData().get(NOC_PARTY).equals(RESPONDENT)) {
            return null;
        }

        return caseData;
    }

    private ChangedRepresentative generateChangedRepresentative(CaseDetails caseDetails) {

        Map<String, Object> caseData = caseDetails.getData();
        Organisation representativeOrg = objectMapper.convertValue(caseData.get(NOC_NEW_SOL_ORGANISATION), Organisation.class);

        return ChangedRepresentative
            .builder()
            .name((String) caseData.get(NOC_NEW_SOL_NAME))
            .email((String) caseData.get(NOC_NEW_SOL_EMAIL))
            .organisation(representativeOrg)
            .build();
    }

    private ChangeOfRepresentation generateChangeOfRepresentation(CaseDetails caseDetails,
                                                                  String authorizationToken,
                                                                  ChangedRepresentative changedRepresentative,
                                                                  boolean isAdded) {
        Map<String, Object> caseData = caseDetails.getData();
        boolean isApplicant = caseData.get(NOC_PARTY).equals(APPLICANT);
        String party = isApplicant ? APPLICANT : RESPONDENT;
        String clientName = isApplicant ? caseDataService.buildFullApplicantName(caseDetails) : caseDataService.buildFullRespondentName(caseDetails);

        return isAdded ? ChangeOfRepresentation.builder().party(party).clientName(clientName)
            .date(LocalDate.now()).by(idamService.getIdamFullName(authorizationToken)).via(NOTICE_OF_CHANGE)
            .added(changedRepresentative).build()

            : ChangeOfRepresentation.builder().party(party).clientName(clientName)
            .date(LocalDate.now()).by(idamService.getIdamFullName(authorizationToken)).via(NOTICE_OF_CHANGE)
            .removed(changedRepresentative).build();

    }

    private ChangedRepresentative generateChangedRepresentativeSolicitorIsNotDigital(CaseDetails caseDetails) {
        return null;
    }

    private Map<String, Object> updateCurrentApplicantSolicitorFields(CaseDetails caseDetails) {
        return null;
    }

    private Map<String, Object> updateCurrentRespondentSolicitorFields(CaseDetails caseDetails) {
        return null;
    }

    public ChangeOfRepresentatives updateChangeOfRepresentatives(CaseDetails caseDetails, ChangeOfRepresentation latestRepresentationChange) {
        Map<String, Object> caseData = caseDetails.getData();

        if (Optional.ofNullable(caseData.get(CHANGE_OF_REPRESENTATIVES)).isEmpty()) {
            return ChangeOfRepresentatives.builder()
                .changeOfRepresentation(List.of(latestRepresentationChange))
                .build();
        } else {
            ChangeOfRepresentatives changeOfRepresentatives = objectMapper.convertValue(caseData.get(CHANGE_OF_REPRESENTATIVES), ChangeOfRepresentatives.class);
            changeOfRepresentatives.addChangeOfRepresentation(latestRepresentationChange);
            return changeOfRepresentatives;
        }
    }

}
