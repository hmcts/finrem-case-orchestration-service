package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.hmcts.reform.ccd.client.model.CaseResource;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.financialremedy.ContestedRegisterService;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ContestedRegisterController.class)
public class ContestedRegisterControllerTest extends BaseControllerTest {

    @MockitoBean
    private ContestedRegisterService contestedRegisterService;

    @Test
    public void shouldSuccessfullyReturnCaseDetails() throws Exception {
        given(contestedRegisterService.getCaseDetails("case123")).willReturn(getCaseResource());

        mvc.perform(get("/case-orchestration/{case-reference}", "case123")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.caseType.id").value("CONTESTED"))
            .andExpect(jsonPath("$.data.caseType.jurisdiction").value("London"));
    }

    private CaseResource getCaseResource() {
        final CaseResource caseResource = new CaseResource();
        Map<String, Object> caseTypeData = new HashMap<>();
        caseTypeData.put("id", "CONTESTED");
        caseTypeData.put("jurisdiction", "London");

        final JsonNode caseTypeNode = objectMapper.valueToTree(caseTypeData);

        final Map<String, JsonNode> dataMap = new HashMap<>();
        dataMap.put("caseType", caseTypeNode);

        caseResource.setData(dataMap);
        return caseResource;
    }
}
