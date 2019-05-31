package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import org.mockito.stubbing.OngoingStubbing;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.ResultMatcher;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;

import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.AUTH_TOKEN;

@WebMvcTest(GeneralLetterController.class)
public class GeneralLetterControllerTest extends DocumentControllerTest {

    @Override
    public String endpoint() {
        return "/case-orchestration/documents/general-letter";
    }

    @Override
    String jsonFixture() {
        return "/fixtures/contested/general-letter.json";
    }

    @Override
    public OngoingStubbing<CaseDocument> whenServiceGeneratesDocument() {
        return when(documentService.createGeneralLetter(eq(AUTH_TOKEN), isA(CaseDetails.class)));
    }

    @Override
    ResultMatcher stateCheck() {
        return jsonPath("$.data.generalLetterBody", isEmptyOrNullString());
    }

    @Override
    String documentName() {
        return "generalLetterBody";
    }
}