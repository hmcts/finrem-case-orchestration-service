package uk.gov.hmcts.reform.finrem.caseorchestration.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.PAPER_APPLICATION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_NATURE_OF_APPLICATION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_RESPONDENT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_NOTIFICATIONS_EMAIL_CONSENT;

public class ConsentedHearingHelperTest {

    private final ObjectMapper mapper =  new ObjectMapper();
    @Test
    public void isNotEmpty() {
        CallbackRequest callbackRequest =  callbackRequest();
        callbackRequest.getCaseDetails().getData()
            .put(CONSENTED_NATURE_OF_APPLICATION, List.of("Variation Order","Pension document","Lump sum"));
        ConsentedHearingHelper helper = new ConsentedHearingHelper(mapper);
        assertTrue(helper.isNotEmpty(CONSENTED_NATURE_OF_APPLICATION, callbackRequest.getCaseDetails().getData()));
    }

    @Test
    public void givenConsentCase_whenAllConditionSatisfied_thenReturnTrue() {
        CallbackRequest callbackRequest =  callbackRequest();
        Map<String, Object> data = callbackRequest.getCaseDetails().getData();
        data.put(PAPER_APPLICATION, NO_VALUE);
        data.put(CONTESTED_RESPONDENT_REPRESENTED, YES_VALUE);
        data.put(RESP_SOLICITOR_EMAIL, "test@test.com");
        data.put(RESP_SOLICITOR_NOTIFICATIONS_EMAIL_CONSENT, YES_VALUE);

        ConsentedHearingHelper helper = new ConsentedHearingHelper(mapper);
        assertTrue(helper.isRespondentSolicitorAgreeToReceiveEmails(data));
    }

    @Test
    public void givenConsentCase_whenAllConditionNotSatisfied_thenReturnFalse() {
        CallbackRequest callbackRequest =  callbackRequest();
        Map<String, Object> data = callbackRequest.getCaseDetails().getData();
        data.put(PAPER_APPLICATION, YES_VALUE);
        data.put(CONTESTED_RESPONDENT_REPRESENTED, YES_VALUE);
        data.put(RESP_SOLICITOR_EMAIL, "test@test.com");
        data.put(RESP_SOLICITOR_NOTIFICATIONS_EMAIL_CONSENT, YES_VALUE);

        ConsentedHearingHelper helper = new ConsentedHearingHelper(mapper);
        assertFalse(helper.isRespondentSolicitorAgreeToReceiveEmails(data));
    }

    @Test
    public void givenConsentCase_whenCallToCovertToCaseDocument_thenReturnCaseDocument() {
        CallbackRequest callbackRequest =  callbackRequest();
        Map<String, Object> data = callbackRequest.getCaseDetails().getData();
        data.put("HearingNotice", TestSetUpUtils.caseDocument());


        ConsentedHearingHelper helper = new ConsentedHearingHelper(mapper);
        CaseDocument caseDocument = helper.convertToCaseDocument(data.get("HearingNotice"));
        TestSetUpUtils.assertCaseDocument(caseDocument);
    }


    private CallbackRequest callbackRequest() {
        return CallbackRequest
            .builder()
            .caseDetails(CaseDetails.builder()
                .data(new HashMap<>()).build())
            .build();
    }
}