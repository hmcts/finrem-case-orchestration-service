package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpServerErrorException;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;

@WebMvcTest(GeneralEmailStartController.class)
public class GeneralEmailStartControllerTest extends BaseControllerTest {

    @MockBean
    private IdamService idamService;

    private String bearerToken = "some-access-token";

    @Test
    public void storeGeneralEmailSuccess() throws Exception {
        generalEmailStartControllerSetUp();
        when(idamService.getIdamFullName(bearerToken)).thenReturn("Firstname LastName");
        mvc.perform(post("/case-orchestration/general-email-start")
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, bearerToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.generalEmailCreatedBy", is("Firstname LastName")));

    }

    @Test
    public void generateGeneralEmail400Error() throws Exception {
        doEmptyCaseDataSetUp();

        mvc.perform(post("/case-orchestration/general-email-start")
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, bearerToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void generateGeneralLetter500Error() throws Exception {
        generalEmailStartControllerSetUp();
        when(idamService.getIdamFullName(bearerToken))
            .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        mvc.perform(post("/case-orchestration/general-email-start")
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, bearerToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isInternalServerError());
    }

    private void generalEmailStartControllerSetUp() throws IOException, URISyntaxException {
        ObjectMapper objectMapper = new ObjectMapper();
        requestContent = objectMapper.readTree(new File(getClass()
            .getResource("/fixtures/general-letter.json").toURI()));
    }
}
