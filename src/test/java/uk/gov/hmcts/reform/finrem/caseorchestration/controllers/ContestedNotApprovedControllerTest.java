package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import org.junit.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ContestedNotApprovedService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ContestedNotApprovedController.class)
public class ContestedNotApprovedControllerTest extends BaseControllerTest {

    private static final String CONTESTED_NOT_APPROVED_SUBMIT_URL = "/case-orchestration/contested-application-not-approved-submit";

    @MockBean
    private ContestedNotApprovedService contestedNotApprovedService;

    @Test
    public void submitContestedNotApproved() throws Exception {
        doValidCaseDataSetUp();

        mvc.perform(post(CONTESTED_NOT_APPROVED_SUBMIT_URL)
            .content(requestContent.toString())
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk());

        verify(contestedNotApprovedService, times(1)).addContestedNotApprovedEntry(any());
    }
}
