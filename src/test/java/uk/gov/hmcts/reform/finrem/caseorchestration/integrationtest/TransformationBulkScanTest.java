package uk.gov.hmcts.reform.finrem.caseorchestration.integrationtest;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.authorisation.validators.AuthTokenValidator;
import uk.gov.hmcts.reform.bsp.common.model.validation.out.OcrValidationResult;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.CaseOrchestrationApplication;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CcdService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SystemUserService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.validation.FormAValidator;

import java.util.Arrays;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
public class TransformationBulkScanTest extends BaseTest {

    private static final String TRANSFORMATION_URL = "/transform-exception-record";
    private static final String FORM_A_JSON_PATH = "fixtures/bulkscan/transformation/simple-FormA.json";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    AuthTokenValidator authTokenValidator;

    @MockitoBean
    FormAValidator bulkScanFormValidator;

    @MockitoBean
    CcdService ccdService;

    @MockitoBean
    SystemUserService systemUserService;

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

    @Test
    public void shouldReturnErrorResponseForContestedCaseReferenceInDivorceCaseNumber() throws Exception {
        String contestedCaseReference = "1234123412341234";
        String systemUserToken = "system-user-token";
        String jsonPayload = loadResourceAsString(FORM_A_JSON_PATH)
            .replace("DD12D12345", contestedCaseReference);
        String warningMsg = "A Contested Financial Remedy case already exists for the supplied divorce case number. "
            + "Consented within Contested applications must be progressed on the existing Contested case.";

        when(bulkScanFormValidator.validateBulkScanForm(any()))
            .thenReturn(OcrValidationResult.builder().build());
        when(systemUserService.getSysUserToken()).thenReturn(systemUserToken);
        when(ccdService.contestedCaseExistsWithReference(eq(contestedCaseReference), eq(systemUserToken))).thenReturn(true);

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
