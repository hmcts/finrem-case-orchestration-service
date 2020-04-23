package uk.gov.hmcts.reform.finrem.caseorchestration.integrationtest;

import org.hamcrest.Matcher;
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

import java.util.Arrays;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.bsp.common.utils.ResourceLoader.loadResourceAsString;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.SERVICE_AUTHORISATION_HEADER;

@ContextConfiguration(classes = CaseOrchestrationApplication.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@PropertySource(value = "classpath:application.yml")
@AutoConfigureMockMvc
@RunWith(SpringRunner.class)
public class TransformationBulkScanTest {

    private static final String TRANSFORMATION_URL = "/transform-exception-record";
    private static final String FORM_A_JSON_PATH = "fixtures/bulkscan/transformation/simple-formA.json";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    AuthTokenValidator authTokenValidator;

    @MockBean
    FormAValidator bulkScanFormValidator;

    @Before
    public void setup() {
        when(authTokenValidator.getServiceName(any())).thenReturn("bulk_scan_orchestrator");
    }

    @Test
    public void shouldReturnErrorResponseForInvalidData() throws Exception {
        String jsonPayload = loadResourceAsString(FORM_A_JSON_PATH);
        String warningMsg = "warn!";

        when(bulkScanFormValidator.validateBulkScanForm(any()))
            .thenReturn(OcrValidationResult.builder().addWarning(warningMsg).build());

        mockMvc.perform(
            post(TRANSFORMATION_URL)
                .contentType(APPLICATION_JSON)
                .content(jsonPayload)
                .header(SERVICE_AUTHORISATION_HEADER, "I'm whitelisted service")
        )
            .andExpect(status().isUnprocessableEntity())
            .andExpect(content().string(hasErrorWithMessageCopiedFromWarning(warningMsg)));
    }

    private Matcher<String> hasErrorWithMessageCopiedFromWarning(String warningMsg) {
        return allOf(isJson(), hasJsonPath("$.errors", equalTo(Arrays.asList(warningMsg))));
    }
}
