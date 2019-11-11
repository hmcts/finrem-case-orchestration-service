package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import java.io.File;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static java.util.Collections.emptyList;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.ResultMatcher.matchAll;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransformationController.class)
public class TransformationControllerTest extends BaseControllerTest {
    private static final String API_URL = "/transform-exception-record";
    ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void shouldReturnSuccessResponseForTransformationEndpoint() throws Exception {
        JsonNode formToValidate = objectMapper.readTree(new File(getClass()
                .getResource("/fixtures/model/exception-record.json").toURI()));
        mvc.perform(post(API_URL)
                .contentType(APPLICATION_JSON)
                .content(formToValidate.toString())
        ).andExpect(matchAll(
                status().isOk(),
                content().string(allOf(
                        isJson(),
                        hasJsonPath("$.warnings", equalTo(emptyList())),
                        hasJsonPath("$.case_creation_details", is(notNullValue()))
                ))
        ));
    }
}
