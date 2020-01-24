package uk.gov.hmcts.reform.finrem.functional.bulkscan;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.authorisation.validators.AuthTokenValidator;
import uk.gov.hmcts.reform.bsp.common.model.validation.out.OcrValidationResult;
import uk.gov.hmcts.reform.finrem.caseorchestration.CaseOrchestrationApplication;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.validation.FormAValidator;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static java.util.Collections.emptyList;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.SERVICE_AUTHORISATION_HEADER;
import static uk.gov.hmcts.reform.finrem.functional.bulkscan.S2SAuthTokens.ALLOWED_SERVICE_TOKEN;
import static uk.gov.hmcts.reform.finrem.functional.bulkscan.S2SAuthTokens.I_AM_NOT_ALLOWED_SERVICE_TOKEN;
import static uk.gov.hmcts.reform.finrem.functional.util.ResourceLoader.loadResourceAsString;

@ContextConfiguration(classes = CaseOrchestrationApplication.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@PropertySource(value = "classpath:application.yml")
@AutoConfigureMockMvc
@RunWith(SpringRunner.class)
public class TransformationBulkScanITest {

    private static final String FULL_D8_FORM_JSON_PATH = "jsonExamples/payloads/bulk/scan/d8/fullD8Form.json";
    private static final String PARTIAL_D8_FORM_JSON_PATH = "jsonExamples/payloads/bulk/scan/d8/partialD8Form.json";
    private static final String TRANSFORMATION_URL = "/transform-exception-record";

    private String jsonPayload;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    AuthTokenValidator authTokenValidator;

    @MockBean
    FormAValidator bulkScanFormValidator;

    @Before
    public void setup() throws Exception {
        when(authTokenValidator.getServiceName(ALLOWED_SERVICE_TOKEN)).thenReturn("bulk_scan_orchestrator");
        when(authTokenValidator.getServiceName(I_AM_NOT_ALLOWED_SERVICE_TOKEN)).thenReturn("don't let me do it!");

        jsonPayload = loadResourceAsString(FULL_D8_FORM_JSON_PATH);
    }

    @Test
    public void shouldReturnForbiddenStatusWhenInvalidS2SToken() throws Exception {
        mockMvc.perform(
            post(TRANSFORMATION_URL)
                .contentType(APPLICATION_JSON)
                .content(jsonPayload)
                .header(SERVICE_AUTHORISATION_HEADER, I_AM_NOT_ALLOWED_SERVICE_TOKEN)
        ).andExpect(status().isForbidden());
    }

    @Test
    public void shouldReturnUnauthorisedStatusWhenNoS2SToken() throws Exception {
        mockMvc.perform(
            post(TRANSFORMATION_URL)
                .contentType(APPLICATION_JSON)
                .content(jsonPayload)
                .header(SERVICE_AUTHORISATION_HEADER, "")
        ).andExpect(status().isUnauthorized());
    }

    @Test
    public void shouldReturnSuccessResponseForTransformationEndpointWhenUsingFullDataSet() throws Exception {
        when(bulkScanFormValidator.validateBulkScanForm(any())).thenReturn(OcrValidationResult.builder().build());

        mockMvc.perform(
            post(TRANSFORMATION_URL)
                .contentType(APPLICATION_JSON)
                .content(jsonPayload)
                .header(SERVICE_AUTHORISATION_HEADER, ALLOWED_SERVICE_TOKEN)
        )
            .andExpect(status().isOk())
            .andExpect(content().string(
                allOf(
                    isJson(),
                    hasJsonPath("$.warnings", equalTo(emptyList())),
                    hasJsonPath("$.case_creation_details.*", hasSize(3)),
                    hasJsonPath("$.case_creation_details", allOf(
                        hasJsonPath("case_type_id", is("DIVORCE")),
                        hasJsonPath("event_id", is("createCase")),
                        hasJsonPath("case_data", allOf(
                            hasJsonPath("bulkScanCaseReference", is("LV481297"))
                        ))
                    ))
                )));
    }

    @Test
    public void shouldReturnSuccessResponseForTransformationEndpointWhenUsingOnlyASubsetOfData() throws Exception {
        String formToTransform = loadResourceAsString(PARTIAL_D8_FORM_JSON_PATH);

        when(bulkScanFormValidator.validateBulkScanForm(any())).thenReturn(OcrValidationResult.builder().build());

        mockMvc.perform(
            post(TRANSFORMATION_URL)
                .contentType(APPLICATION_JSON)
                .content(formToTransform)
                .header(SERVICE_AUTHORISATION_HEADER, ALLOWED_SERVICE_TOKEN)
        )
            .andExpect(status().isOk())
            .andExpect(content().string(
                allOf(
                    isJson(),
                    hasJsonPath("$.warnings", equalTo(emptyList())),
                    hasJsonPath("$.case_creation_details.*", hasSize(3)),
                    hasJsonPath("$.case_creation_details", allOf(
                        hasJsonPath("case_type_id", is("DIVORCE")),
                        hasJsonPath("event_id", is("createCase")),
                        hasJsonPath("case_data.*", hasSize(10)),
                        hasJsonPath("case_data", allOf(
                            hasJsonPath("bulkScanCaseReference", is("LV481297"))
                        ))
                    ))
                )));
    }

    @Test
    public void shouldReturnErrorResponseForUnsupportedFormType() throws Exception {
        String jsonToTransform = new ObjectMapper().createObjectNode()
            .put("form_type", "unsupportedFormType")
            .toPrettyString();

        mockMvc.perform(
            post(TRANSFORMATION_URL)
                .contentType(APPLICATION_JSON)
                .content(jsonToTransform)
                .header(SERVICE_AUTHORISATION_HEADER, ALLOWED_SERVICE_TOKEN)
        ).andExpect(status().isUnprocessableEntity());
    }

}