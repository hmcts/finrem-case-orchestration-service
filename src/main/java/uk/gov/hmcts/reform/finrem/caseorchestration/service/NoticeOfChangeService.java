package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.ChangeOfRepresentativesParseException;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.CourtDetailsParseException;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangeOfRepresentation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangeOfRepresentatives;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangedRepresentative;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Organisation;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.nio.charset.StandardCharsets.UTF_8;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CHANGE_OF_REPRESENTATIVES;
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
    ObjectMapper objectMapper;

    public Map<String, Object> updateRepresentation(CaseDetails caseDetails, String authorizationToken) {
        Map<String,Object> caseData = caseDetails.getData();

        ChangedRepresentative changedRepresentative = generateChangedRepresentative(caseDetails);
        ChangeOfRepresentation changeOfRepresentation = generateChangeOfRepresentation(caseDetails, authorizationToken);


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

    private void generateChangeOfRepresentatives(CaseDetails caseDetails) {
        Map<String,Object> caseData = caseDetails.getData();



    }

    private ChangedRepresentative generateChangedRepresentative(CaseDetails caseDetails) {

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

    private ChangeOfRepresentation generateChangeOfRepresentation(CaseDetails caseDetails, String authorizationToken) {
        Map<String, Object> caseData = caseDetails.getData();
        boolean isApplicant = caseData.get(NOC_PARTY).equals(APPLICANT);
        String party = isApplicant ? APPLICANT : RESPONDENT;
        String clientName = isApplicant ? caseDataService.buildFullApplicantName(caseDetails) : caseDataService.buildFullRespondentName(caseDetails);

        return ChangeOfRepresentation.builder()
            .party(party)
            .clientName(clientName)
            .date(LocalDate.now())
            .by(idamService.getIdamFullName(authorizationToken))
            .via(NOTICE_OF_CHANGE)
            .build();
    }

    private ChangedRepresentative generateChangedRepresentativeSolicitorIsNotDigital(CaseDetails caseDetails) {
        return null;
    }

    private Map<String, Object> updateCurrentSolicitorFields(CaseDetails caseDetails) {
        return null;
    }

    private ChangeOfRepresentatives updateChangeOfRepresentatives(CaseDetails caseDetails, ChangeOfRepresentation latestRepresentationChange) throws JsonProcessingException {
        Map<String, Object> caseData = caseDetails.getData();

        if (Optional.ofNullable(caseData.get(CHANGE_OF_REPRESENTATIVES)).isEmpty()) {
            return ChangeOfRepresentatives.builder()
                .changeOfRepresentation(List.of(latestRepresentationChange))
                .build();
        } else {
            ChangeOfRepresentatives changeOfRepresentatives = objectMapper.readValue(getChangeOfRepresentativesString(), ChangeOfRepresentatives.class);
            changeOfRepresentatives.addChangeOfRepresentation(latestRepresentationChange);
            return changeOfRepresentatives;
        }
    }

    private String getChangeOfRepresentativesString() {
        try (InputStream inputStream = NoticeOfChangeService.class.getResourceAsStream(CHANGE_OF_REPRESENTATIVES)) {
            return IOUtils.toString(inputStream, UTF_8);
        } catch (IOException e) {
            throw new ChangeOfRepresentativesParseException();
        }
    }

}
