package uk.gov.hmcts.reform.finrem.caseorchestration.controllers.bulkscan;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.authorisation.exceptions.InvalidTokenException;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.BaseControllerTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.ForbiddenException;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.UnauthenticatedException;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.bulkscan.OcrValidationResult;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AuthService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.validation.BulkScanValidationService;

import java.io.IOException;

import static java.util.Collections.emptyList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OcrValidationController.class)
public class OcrValidationControllerTest extends BaseControllerTest {

    private static final String BULKSCAN_VALIDATION_BASIC_FORM_JSON = "fixtures/bulkscan/validation/basic-form.json";
    private static final String BULKSCAN_VALIDATION_VALID_OCR_RESPONSE_JSON =
            "fixtures/bulkscan/validation/valid-ocr-response.json";

    @MockBean private AuthService authService;
    @MockBean private BulkScanValidationService bulkScanValidationService;

    @Before
    public void setUp() {
        super.setUp();
    }

    @Test
    public void should_return_401_status_when_auth_service_throws_unauthenticated_exception() throws Exception {
        String requestBody = readResource(BULKSCAN_VALIDATION_BASIC_FORM_JSON);
        given(authService.authenticate("")).willThrow(UnauthenticatedException.class);

        mvc
                .perform(
                        post("/forms/PERSONAL/validate-ocr")
                                .header("ServiceAuthorization", "")
                                .contentType(APPLICATION_JSON_VALUE)
                                .content(requestBody)
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void should_return_401_status_when_auth_service_throws_invalid_token_exception() throws Exception {
        String requestBody = readResource(BULKSCAN_VALIDATION_BASIC_FORM_JSON);
        given(authService.authenticate("test-token")).willThrow(InvalidTokenException.class);

        mvc
                .perform(
                        post("/forms/PERSONAL/validate-ocr")
                                .header("ServiceAuthorization", "test-token")
                                .contentType(APPLICATION_JSON_VALUE)
                                .content(requestBody)
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void should_return_403_status_when_auth_service_throws_forbidden_exception() throws Exception {
        String requestBody = readResource(BULKSCAN_VALIDATION_BASIC_FORM_JSON);
        given(authService.authenticate(any())).willThrow(ForbiddenException.class);

        mvc
                .perform(
                        post("/forms/PERSONAL/validate-ocr")
                                .header("ServiceAuthorization", "test-token")
                                .contentType(APPLICATION_JSON_VALUE)
                                .content(requestBody)
                )
                .andExpect(status().isForbidden())
                .andExpect(content().json("{\"error\":\"S2S token is not authorized to use the service\"}"));
    }

    @Test
    public void should_return_success_message_when_ocr_data_is_valid() throws Exception {
        String requestBody = readResource(BULKSCAN_VALIDATION_BASIC_FORM_JSON);

        String testFormName = "PERSONAL";

        given(authService.authenticate("testServiceAuthHeader")).willReturn("testServiceName");
        given(bulkScanValidationService.validate(eq(testFormName), anyList())).willReturn(new OcrValidationResult(
            emptyList(), emptyList()));

        mvc
            .perform(
                post(String.format("/forms/%s/validate-ocr", testFormName))
                    .contentType(APPLICATION_JSON_VALUE)
                    .header("ServiceAuthorization", "testServiceAuthHeader")
                    .content(requestBody)
            )
            .andExpect(status().isOk())
            .andExpect(content().json(readResource(BULKSCAN_VALIDATION_VALID_OCR_RESPONSE_JSON)));
    }

    private String readResource(final String fileName) throws IOException {
        return Resources.toString(Resources.getResource(fileName), Charsets.UTF_8);
    }
}
