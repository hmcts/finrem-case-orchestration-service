package uk.gov.hmcts.reform.finrem.caseorchestration.helper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ConsentedHearingDataWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.PAPER_APPLICATION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_SOLICITOR_AGREE_TO_RECEIVE_EMAILS_CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_RESPONDENT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LIST_FOR_HEARING_COLLECTION_CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_NOTIFICATIONS_EMAIL_CONSENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService.nullToEmpty;

@Component
@RequiredArgsConstructor
@Slf4j
public class ConsentedHearingHelper {

    private final ObjectMapper objectMapper;

    public List<ConsentedHearingDataWrapper> getHearings(Map<String, Object> caseData) {
        return Optional.ofNullable(caseData.get(LIST_FOR_HEARING_COLLECTION_CONSENTED))
            .map(this::convertToHearingDataList).orElse(new ArrayList<>());
    }

    public List<ConsentedHearingDataWrapper> convertToHearingDataList(Object object) {
        return objectMapper.convertValue(object, new TypeReference<>() {
        });
    }

    public Map<String, Object> convertToMap(Object object) {
        return objectMapper.convertValue(object, new TypeReference<>() {
        });
    }

    public boolean isPaperApplication(Map<String, Object> caseData) {
        return YES_VALUE.equalsIgnoreCase(nullToEmpty(caseData.get(PAPER_APPLICATION)));
    }

    public boolean isApplicantSolicitorAgreeToReceiveEmails(Map<String, Object> caseData) {
        return YES_VALUE.equalsIgnoreCase(nullToEmpty(caseData.get(APP_SOLICITOR_AGREE_TO_RECEIVE_EMAILS_CONSENTED)));
    }

    public boolean isRespondentRepresentedByASolicitor(Map<String, Object> caseData) {
        return YES_VALUE.equalsIgnoreCase(nullToEmpty(caseData.get(CONTESTED_RESPONDENT_REPRESENTED)));
    }

    public boolean isNotEmpty(String field, Map<String, Object> caseData) {
        return StringUtils.isNotEmpty(nullToEmpty(caseData.get(field)));
    }

    public boolean isRespondentSolicitorAgreeToReceiveEmails(Map<String, Object> caseData) {
        return !isPaperApplication(caseData)
            && isRespondentRepresentedByASolicitor(caseData)
            && isNotEmpty(RESP_SOLICITOR_EMAIL, caseData)
            && !NO_VALUE.equalsIgnoreCase(nullToEmpty(caseData.get(RESP_SOLICITOR_NOTIFICATIONS_EMAIL_CONSENT)));
    }
}
