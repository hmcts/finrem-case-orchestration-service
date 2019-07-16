package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;

import javax.ws.rs.core.MediaType;
import java.io.File;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BulkPrintController.class)
public class BulkPrintControllerTest extends BaseControllerTest {

    private static final String BEARER_TOKEN = "Bearer eyJhbGciOiJIUzI1NiJ9";
    private final ObjectMapper objectMapper = new ObjectMapper();
    @MockBean private BulkPrintService bulkPrintService;

    @Test
    public void shouldSuccessfullyReturnStatus200() throws Exception {
        requestContent =
          objectMapper.readTree(
              new File(getClass().getResource("/fixtures/contested/hwf.json").toURI()));
        mvc.perform(
              post("/case-orchestration/bulk-print")
                  .content(requestContent.toString())
                  .header("Authorization", BEARER_TOKEN)
                  .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.confirmation_body", is("Bulk print is successful.")));
        verify(bulkPrintService).sendForBulkPrint(any(), anyString());
    }
}
