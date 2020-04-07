package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;

import java.io.File;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;

@WebMvcTest(CaseDataController.class)
public class CaseDataControllerTest extends BaseControllerTest {

    private static final String MOVE_VALUES_SAMPLE_JSON = "/fixtures/move-values-sample.json";
    private static final String CONTESTED_HWF_JSON = "/fixtures/contested/hwf.json";
    private static final String CONTESTED_VALIDATE_HEARING_SUCCESSFULLY_JSON = "/fixtures/contested/validate-hearing-successfully.json";
    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockBean
    private  IdamService idamService;

    @Test
    public void shouldSuccessfullyMoveValues() throws Exception {
        when(idamService.isUserRoleAdmin(isA(String.class))).thenReturn(Boolean.TRUE);

        requestContent = objectMapper.readTree(
            new File(getClass()
                .getResource(MOVE_VALUES_SAMPLE_JSON)
                .toURI()));
        mvc.perform(post("/case-orchestration/move-collection/uploadHearingOrder/to/uploadHearingOrderRO")
                            .content(requestContent.toString())
                            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                            .contentType(APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.data.uploadHearingOrderRO[0]").exists())
                .andExpect(jsonPath("$.data.uploadHearingOrderRO[1]").exists())
                .andExpect(jsonPath("$.data.uploadHearingOrderRO[2]").exists())
                .andExpect(jsonPath("$.data.uploadHearingOrder").isEmpty());
    }

    @Test
    public void shouldSuccessfullyMoveValuesToNewCollections() throws Exception {
        when(idamService.isUserRoleAdmin(isA(String.class))).thenReturn(Boolean.TRUE);

        requestContent = objectMapper.readTree(
            new File(getClass()
                .getResource(MOVE_VALUES_SAMPLE_JSON)
                .toURI()));
        mvc.perform(post("/case-orchestration/move-collection/uploadHearingOrder/to/uploadHearingOrderNew")
                            .content(requestContent.toString())
                            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                            .contentType(APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.data.uploadHearingOrderNew[0]").exists())
                .andExpect(jsonPath("$.data.uploadHearingOrder").isEmpty());
    }

    @Test
    public void shouldDoNothingWithNonArraySourceValueMove() throws Exception {
        when(idamService.isUserRoleAdmin(isA(String.class))).thenReturn(Boolean.TRUE);

        requestContent = objectMapper.readTree(
            new File(getClass()
                .getResource(MOVE_VALUES_SAMPLE_JSON)
                .toURI()));
        mvc.perform(post("/case-orchestration/move-collection/someString/to/uploadHearingOrder")
                            .content(requestContent.toString())
                            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                            .contentType(APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.data.uploadHearingOrder[0]").exists());
    }

    @Test
    public void shouldDoNothingWithNonArrayDestinationValueMove() throws Exception {
        when(idamService.isUserRoleAdmin(isA(String.class))).thenReturn(Boolean.TRUE);

        requestContent = objectMapper.readTree(
            new File(getClass()
                .getResource(MOVE_VALUES_SAMPLE_JSON)
                .toURI()));
        mvc.perform(post("/case-orchestration/move-collection/uploadHearingOrder/to/someString")
                            .content(requestContent.toString())
                            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                            .contentType(APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.data.uploadHearingOrder[0]").exists());
    }

    @Test
    public void shouldDoNothingWhenSourceIsEmptyMove() throws Exception {
        when(idamService.isUserRoleAdmin(isA(String.class))).thenReturn(Boolean.TRUE);

        requestContent = objectMapper.readTree(
            new File(getClass()
                .getResource(MOVE_VALUES_SAMPLE_JSON)
                .toURI()));
        mvc.perform(post("/case-orchestration/move-collection/empty/to/uploadHearingOrder")
                            .content(requestContent.toString())
                            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                            .contentType(APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.data.uploadHearingOrder[0]").exists());
    }

    @Test
    public void shouldSuccessfullyReturnAsAdminConsented() throws Exception {
        when(idamService.isUserRoleAdmin(isA(String.class))).thenReturn(Boolean.TRUE);

        requestContent = objectMapper.readTree(
            new File(getClass()
                .getResource(CONTESTED_HWF_JSON).toURI()));
        mvc.perform(post("/case-orchestration/consented/set-defaults")
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data.isAdmin", is(YES_VALUE)));
    }

    @Test
    public void shouldSuccessfullyReturnNotAsAdminConsented() throws Exception {
        when(idamService.isUserRoleAdmin(isA(String.class))).thenReturn(Boolean.FALSE);

        requestContent = objectMapper.readTree(new File(getClass()
            .getResource(CONTESTED_VALIDATE_HEARING_SUCCESSFULLY_JSON).toURI()));
        mvc.perform(post("/case-orchestration/consented/set-defaults")
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data.isAdmin", is(NO_VALUE)))
            .andExpect(jsonPath("$.data.applicantRepresented", is(YES_VALUE)));
    }

    @Test
    public void shouldSuccessfullyReturnAsAdminContested() throws Exception {
        when(idamService.isUserRoleAdmin(isA(String.class))).thenReturn(Boolean.TRUE);

        requestContent = objectMapper.readTree(new File(getClass()
            .getResource(CONTESTED_HWF_JSON).toURI()));
        mvc.perform(post("/case-orchestration/contested/set-defaults")
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data.isAdmin", is(YES_VALUE)));
    }

    @Test
    public void shouldSuccessfullyReturnNotAsAdminContested() throws Exception {
        when(idamService.isUserRoleAdmin(isA(String.class))).thenReturn(Boolean.FALSE);

        requestContent = objectMapper.readTree(new File(getClass()
            .getResource(CONTESTED_VALIDATE_HEARING_SUCCESSFULLY_JSON).toURI()));
        mvc.perform(post("/case-orchestration/contested/set-defaults")
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data.isAdmin", is(NO_VALUE)))
            .andExpect(jsonPath("$.data.applicantRepresented", is(YES_VALUE)));
    }
}