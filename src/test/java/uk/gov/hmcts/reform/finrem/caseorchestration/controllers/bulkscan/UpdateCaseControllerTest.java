package uk.gov.hmcts.reform.finrem.caseorchestration.controllers.bulkscan;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.BaseControllerTest;

import java.io.File;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UpdateCaseController.class)
public class UpdateCaseControllerTest extends BaseControllerTest {

    private static final String EXCEPTION_RECORD_JSON_PATH = "/fixtures/model/exception-record.json";
    private static final String API_URL = "/update-case";
    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void shouldReturnSuccessResponseForUpdateEndpoint() throws Exception {
        JsonNode formToValidate = objectMapper.readTree(new File(getClass()
            .getResource(EXCEPTION_RECORD_JSON_PATH).toURI()));
        mvc.perform(post(API_URL)
            .contentType(APPLICATION_JSON)
            .content(formToValidate.toString()))
            .andExpect(status().isOk());
    }
}