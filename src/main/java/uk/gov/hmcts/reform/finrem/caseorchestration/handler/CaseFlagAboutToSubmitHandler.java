package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.caseflag.CaseFlag;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;

import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CASE_APPLICANT_FLAGS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CASE_LEVEL_FLAGS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CASE_LEVEL_ROLE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CASE_RESPONDENT_FLAGS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT;

@Slf4j
@Service
@RequiredArgsConstructor
public class CaseFlagAboutToSubmitHandler implements CallbackHandler {

    private final ObjectMapper objectMapper;
    private final CaseDataService caseDataService;

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && (CaseType.CONSENTED.equals(caseType) || CaseType.CONTESTED.equals(caseType))
            && (EventType.CASE_FLAG_CREATE.equals(eventType)
            || EventType.CASE_FLAG_MANAGE.equals(eventType));
    }

    @Override
    public AboutToStartOrSubmitCallbackResponse handle(CallbackRequest callbackRequest, String userAuthorisation) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        log.info("Received request to update case flags with Case ID: {}", caseDetails.getId());

        Map<String, Object> caseData = caseDetails.getData();

        updateCaseFlagsAtCaseLevel(caseData);
        updateCaseFlagsForParty(caseData, CASE_APPLICANT_FLAGS, caseDataService.buildFullApplicantName(caseDetails), APPLICANT);
        updateCaseFlagsForParty(caseData, CASE_RESPONDENT_FLAGS, caseDataService.buildFullRespondentName(caseDetails), RESPONDENT);

        return AboutToStartOrSubmitCallbackResponse.builder().data(caseData).build();
    }

    private void updateCaseFlagsAtCaseLevel(Map<String, Object> caseData) {
        CaseFlag caseFlag = Optional.ofNullable(objectMapper.convertValue(caseData.get(CASE_LEVEL_FLAGS), CaseFlag.class))
            .orElse(new CaseFlag());
        caseFlag.setRoleOnCase(CASE_LEVEL_ROLE);
        caseFlag.setPartyName(CASE_LEVEL_ROLE);
        caseData.put(CASE_LEVEL_FLAGS, caseFlag);
    }

    private void updateCaseFlagsForParty(Map<String, Object> caseData,
                                         String flagLevel,
                                         String party,
                                         String roleOnCase) {
        CaseFlag caseFlag = Optional.ofNullable(objectMapper.convertValue(caseData.get(flagLevel), CaseFlag.class))
            .orElse(new CaseFlag());
        caseFlag.setPartyName(party);
        caseFlag.setRoleOnCase(roleOnCase);
        caseData.put(flagLevel, caseFlag);
    }
}
