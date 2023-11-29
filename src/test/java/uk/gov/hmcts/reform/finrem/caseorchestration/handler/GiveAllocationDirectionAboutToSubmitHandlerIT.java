package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.CaseOrchestrationApplication;

import java.io.InputStream;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;


@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { CaseOrchestrationApplication.class })
@TestPropertySource(value = "classpath:application.properties")
@AutoConfigureMockMvc
@WebAppConfiguration
public class GiveAllocationDirectionAboutToSubmitHandlerIT {

    private static final String ABOUT_TO_SUBMIT_CALLBACK_URL = "/case-orchestration/ccdAboutToSubmitEvent";
    public static final String REQUEST_JSON_PAYLOAD =
        "/fixtures/contested-giveAllocationDirection-twoCourtLists.json";

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;
    private CallbackRequest request;

    @BeforeEach
    public void setup() throws Exception {
        try (InputStream resourceAsStream =
                 getClass().getResourceAsStream(REQUEST_JSON_PAYLOAD)) {
            request = objectMapper.readValue(resourceAsStream, CallbackRequest.class);
        }
    }

    @Test
    public void givenWac_whenServletContext_thenItProvidesGreetController() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.post(ABOUT_TO_SUBMIT_CALLBACK_URL)
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data.cfcCourtList").doesNotExist())
            .andExpect(jsonPath("$.data.londonFRCList").doesNotExist())
            .andExpect(jsonPath("$.data.regionList").value("midlands"))
            .andExpect(jsonPath("$.data.midlandsFRCList").value("birmingham"))
            .andExpect(jsonPath("$.data.birminghamCourtList").value("FR_birmingham_hc_list_1"));
    }
}
