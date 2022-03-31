package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangeOfRepresentation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangeOfRepresentatives;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangedRepresentative;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Organisation;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_FIRST_MIDDLE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_LAST_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_RESPONDENT_FIRST_MIDDLE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NOC_IS_SOL_DIGITAL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NOC_NEW_SOL_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NOC_NEW_SOL_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NOC_NEW_SOL_ORG;
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

    public Map<String, Object> updateRepresentation(CaseDetails caseDetails) {
        Map<String,Object> caseData = caseDetails.getData();

        ChangedRepresentative changedRepresentative = caseData.get(NOC_IS_SOL_DIGITAL).equals("Yes")
            ? generateChangedRepresentativeSolicitorIsDigital(caseDetails)
            : generateChangedRepresentativeSolicitorIsNotDigital(caseDetails);

        if (caseDetails.getData().get(NOC_PARTY).equals(APPLICANT)) {
            party = APPLICANT;

        } else if (caseDetails.getData().get(NOC_PARTY).equals(RESPONDENT)) {
            party = RESPONDENT;
        }
    }

    private void generateChangeOfRepresentatives(CaseDetails caseDetails) {
        Map<String,Object> caseData = caseDetails.getData();



    }

    private ChangedRepresentative generateChangedRepresentativeSolicitorIsDigital(CaseDetails caseDetails) {

        Map<String, Object> caseData = caseDetails.getData();
        Organisation representativeOrg = (Organisation) caseData.get(NOC_NEW_SOL_ORG);
        boolean solHasEmail = Optional.ofNullable(caseData.get(NOC_NEW_SOL_EMAIL)).isPresent();

        return ChangedRepresentative
            .builder()
            .name((String) caseData.get(NOC_NEW_SOL_NAME))
            .email(solHasEmail ? (String) caseData.get(NOC_NEW_SOL_EMAIL) : null)
            .organisation(representativeOrg)
            .build();
    }

    private ChangeOfRepresentation generateChangeOfRepresentation(CaseDetails caseDetails) {
        Map<String, Object> caseData = caseDetails.getData();
        boolean isApplicant = caseData.get(NOC_PARTY).equals(APPLICANT);
        String party = isApplicant ? APPLICANT : RESPONDENT;
        String clientName = isApplicant ? caseDataService.buildFullApplicantName(caseDetails) : caseDataService.buildFullRespondentName(caseDetails);

        return ChangeOfRepresentation.builder()
            .party(party)
            .clientName(clientName)
            .date(LocalDate.now())
            .by()
            .via(NOTICE_OF_CHANGE)
            .build();
    }

    private ChangeOfRepresentatives updateChangeOfRepresentatives() {

    }

    private ChangedRepresentative generateChangedRepresentativeSolicitorIsNotDigital(CaseDetails caseDetails) {

    }

    private Map<String, Object> updateCurrentSolicitorFields(CaseDetails caseDetails) {

    }

}
