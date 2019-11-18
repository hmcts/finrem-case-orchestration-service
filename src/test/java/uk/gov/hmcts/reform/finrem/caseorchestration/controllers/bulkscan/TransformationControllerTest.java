package uk.gov.hmcts.reform.finrem.caseorchestration.controllers.bulkscan;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.BaseControllerTest;

import java.io.File;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static java.util.Collections.emptyList;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransformationController.class)
public class TransformationControllerTest extends BaseControllerTest {

    private static final String EXCEPTION_RECORD_JSON_PATH = "/fixtures/model/exception-record.json";
    private static final String API_URL = "/transform-exception-record";
    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void shouldReturnSuccessResponseForTransformationEndpoint() throws Exception {
        JsonNode formToValidate = objectMapper.readTree(new File(getClass()
            .getResource(EXCEPTION_RECORD_JSON_PATH).toURI()));
        mvc.perform(post(API_URL)
            .contentType(APPLICATION_JSON)
            .content(formToValidate.toString()))
            .andExpect(status().isOk())
            .andExpect(content().string(
                allOf(
                    isJson(),
                    hasJsonPath("$.warnings", equalTo(emptyList())),
                    hasJsonPath("$.case_creation_details.*", hasSize(3)),
                    hasJsonPath("$.case_creation_details", allOf(
                        hasJsonPath("case_type_id", is("FINANCIAL_REMEDY")),
                        hasJsonPath("event_id", is("EVENT_ID")),
                        hasJsonPath("case_data.*", hasSize(2)),
                        hasJsonPath("case_data", allOf(
                            hasJsonPath("D8FirstName", is("Christopher")),
                            hasJsonPath("D8LastName", is("O'John"))
                        ))
                    ))
                )));
    }
}
