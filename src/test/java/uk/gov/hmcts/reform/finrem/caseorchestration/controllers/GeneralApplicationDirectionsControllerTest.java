package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import org.junit.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralApplicationDirectionsService;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;

@WebMvcTest(GeneralApplicationDirectionsController.class)
public class GeneralApplicationDirectionsControllerTest extends BaseControllerTest {

    private static final String INTERIM_HEARING_URL = "/case-orchestration/submit-for-interim-hearing";

    @MockBean
    private GeneralApplicationDirectionsService generalApplicationDirectionsService;

    @Test
    public void submitInterimHearing() throws Exception {
        mvc.perform(post(INTERIM_HEARING_URL)
                .content(resourceContentAsString("/fixtures/general-application.json"))
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk());

        verify(generalApplicationDirectionsService, times(1)).submitInterimHearing(isA(CaseDetails.class), eq(AUTH_TOKEN));
    }
}
