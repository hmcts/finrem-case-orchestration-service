package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.authorisation.exceptions.InvalidTokenException;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.ForbiddenException;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.UnauthenticatedException;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.bulkscan.validation.out.OcrValidationResult;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AuthService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.validation.BulkScanValidationService;

import java.io.File;
import java.io.IOException;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static java.util.Collections.emptyList;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BulkScanController.class)
public class BulkScanControllerTest extends BaseControllerTest {

    private static final String EXCEPTION_RECORD_JSON_PATH = "/fixtures/model/exception-record.json";
    private static final String VALIDATION_BASIC_FORM_JSON_PATH = "fixtures/bulkscan/validation/basic-form.json";
    private static final String VALIDATION_VALID_OCR_RESPONSE_JSON_PATH = "fixtures/bulkscan/validation/valid-ocr-response.json";
    private static final String VALIDATE_API_URL = "/forms/PERSONAL/validate-ocr";
    private static final String TRANSFORM_API_URL = "/transform-exception-record";
    private static final String UPDATE_API_URL = "/update-case";
    private static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";


    private ObjectMapper objectMapper = new ObjectMapper();

    @MockBean
    private AuthService authService;

    @MockBean
    private BulkScanValidationService bulkScanValidationService;

    @Before
    public void setUp() {
        super.setUp();
    }

    @Test
    public void shouldReturn401StatusWhenAuthServiceThrowsUnauthenticatedException() throws Exception {
        String requestBody = readResource(VALIDATION_BASIC_FORM_JSON_PATH);
        given(authService.authenticate("")).willThrow(UnauthenticatedException.class);

        mvc.perform(
            post(VALIDATE_API_URL)
                .header(SERVICE_AUTHORIZATION, "")
                .contentType(APPLICATION_JSON_VALUE)
                .content(requestBody)
        )
            .andExpect(status().isUnauthorized());
    }

    @Test
    public void shouldReturn401StatusWhenAuthServiceThrowsInvalidTokenException() throws Exception {
        String requestBody = readResource(VALIDATION_BASIC_FORM_JSON_PATH);
        given(authService.authenticate("test-token")).willThrow(InvalidTokenException.class);

        mvc.perform(
            post(VALIDATE_API_URL)
                .header(SERVICE_AUTHORIZATION, "test-token")
                .contentType(APPLICATION_JSON_VALUE)
                .content(requestBody)
        )
            .andExpect(status().isUnauthorized());
    }

    @Test
    public void shouldReturn403StatusWhenAuthServiceThrowsForbiddenException() throws Exception {
        String requestBody = readResource(VALIDATION_BASIC_FORM_JSON_PATH);
        given(authService.authenticate(any())).willThrow(ForbiddenException.class);

        mvc.perform(
            post(VALIDATE_API_URL)
                .header(SERVICE_AUTHORIZATION, "test-token")
                .contentType(APPLICATION_JSON_VALUE)
                .content(requestBody)
        )
            .andExpect(status().isForbidden())
            .andExpect(content().json("{\"error\":\"S2S token is not authorized to use the service\"}"));
    }

    @Test
    public void shouldReturnSuccessMessageWhenOcrDataIsValid() throws Exception {
        String requestBody = readResource(VALIDATION_BASIC_FORM_JSON_PATH);

        String testFormName = "PERSONAL";

        given(authService.authenticate("testServiceAuthHeader")).willReturn("testServiceName");
        given(bulkScanValidationService.validate(eq(testFormName),
            anyList())).willReturn(new OcrValidationResult(emptyList(), emptyList()));

        mvc.perform(
            post(String.format("/forms/%s/validate-ocr", testFormName))
                .contentType(APPLICATION_JSON_VALUE)
                .header(SERVICE_AUTHORIZATION, "testServiceAuthHeader")
                .content(requestBody)
        )
            .andExpect(status().isOk())
            .andExpect(content().json(readResource(VALIDATION_VALID_OCR_RESPONSE_JSON_PATH)));
    }

    private String readResource(final String fileName) throws IOException {
        return Resources.toString(Resources.getResource(fileName), Charsets.UTF_8);
    }

    @Test
    public void shouldReturnSuccessResponseForTransformationEndpoint() throws Exception {
        JsonNode formToValidate = objectMapper.readTree(new File(getClass()
            .getResource(EXCEPTION_RECORD_JSON_PATH).toURI()));

        mvc.perform(post(TRANSFORM_API_URL)
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
                        hasJsonPath("event_id", is("bulkScanCaseCreate")),
                        hasJsonPath("case_data.*", hasSize(2)),
                        hasJsonPath("case_data", allOf(
                            hasJsonPath("D8FirstName", is("Christopher")),
                            hasJsonPath("D8LastName", is("O'John"))
                        ))
                    ))
                )));
    }

    @Test
    public void shouldReturnSuccessResponseForUpdateEndpoint() throws Exception {
        JsonNode formToValidate = objectMapper.readTree(new File(getClass()
            .getResource(EXCEPTION_RECORD_JSON_PATH).toURI()));

        mvc.perform(post(UPDATE_API_URL)
            .contentType(APPLICATION_JSON)
            .content(formToValidate.toString()))
            .andExpect(status().isOk())
            .andExpect(content().string(
                allOf(
                    isJson(),
                    hasJsonPath("$.warnings", equalTo(emptyList())),
                    hasJsonPath("$.case_update_details.*", hasSize(3)),
                    hasJsonPath("$.case_update_details", allOf(
                        hasJsonPath("case_type_id", is("FINANCIAL_REMEDY")),
                        hasJsonPath("event_id", is("bulkScanCaseUpdate"))
                    ))
                )));
    }
}
