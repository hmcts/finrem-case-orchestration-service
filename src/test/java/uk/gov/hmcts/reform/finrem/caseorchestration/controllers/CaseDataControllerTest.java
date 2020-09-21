package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.authorisation.generators.ServiceAuthTokenGenerator;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.OrganisationClient;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.ServiceAuthTokenGeneratorService;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.organisation.Organisation;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;

import java.io.File;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyString;
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

    @MockBean
    private OrganisationClient organisationClient;

    @MockBean
    private ServiceAuthTokenGeneratorService serviceAuthTokenGeneratorService;

    @Mock
    private ServiceAuthTokenGenerator serviceAuthTokenGenerator;

    @Before
    public void setUp() {
        super.setUp();
        Organisation defaultOrg = new Organisation();
        defaultOrg.setName("org");
        defaultOrg.setOrganisationIdentifier("12345");
        when(organisationClient.findOrganisationById(anyString(), anyString())).thenReturn(defaultOrg);
        when(serviceAuthTokenGenerator.generate()).thenReturn("abc");
        when(serviceAuthTokenGeneratorService.createTokenGenerator()).thenReturn(serviceAuthTokenGenerator);

    }

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

    @Test
    public void shouldSuccessfullyReturnAsAdminConsentedPaperCase() throws Exception {
        when(idamService.isUserRoleAdmin(isA(String.class))).thenReturn(Boolean.TRUE);

        requestContent = objectMapper.readTree(
            new File(getClass()
                .getResource(CONTESTED_HWF_JSON).toURI()));
        mvc.perform(post("/case-orchestration/contested/set-paper-case-defaults")
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data.isAdmin", is(YES_VALUE)))
            .andExpect(jsonPath("$.data.fastTrackDecision", is(NO_VALUE)))
            .andExpect(jsonPath("$.data.paperApplication", is(YES_VALUE)));
    }

    @Test
    public void shouldSuccessfullyReturnNotAsAdminConsentedPaperCase() throws Exception {
        when(idamService.isUserRoleAdmin(isA(String.class))).thenReturn(Boolean.FALSE);

        requestContent = objectMapper.readTree(new File(getClass()
            .getResource(CONTESTED_VALIDATE_HEARING_SUCCESSFULLY_JSON).toURI()));
        mvc.perform(post("/case-orchestration/contested/set-paper-case-defaults")
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data.isAdmin", is(NO_VALUE)))
            .andExpect(jsonPath("$.data.fastTrackDecision", is(NO_VALUE)))
            .andExpect(jsonPath("$.data.paperApplication", is(YES_VALUE)))
            .andExpect(jsonPath("$.data.applicantRepresented", is(YES_VALUE)));
    }

    @Test
    public void shouldSuccessfullyReturnAsAdminContestedPaperCase() throws Exception {
        when(idamService.isUserRoleAdmin(isA(String.class))).thenReturn(Boolean.TRUE);

        requestContent = objectMapper.readTree(new File(getClass()
            .getResource(CONTESTED_HWF_JSON).toURI()));
        mvc.perform(post("/case-orchestration/contested/set-paper-case-defaults")
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data.isAdmin", is(YES_VALUE)))
            .andExpect(jsonPath("$.data.fastTrackDecision", is(NO_VALUE)))
            .andExpect(jsonPath("$.data.paperApplication", is(YES_VALUE)));
    }

    @Test
    public void shouldSuccessfullyReturnNotAsAdminContestedPaperCase() throws Exception {
        when(idamService.isUserRoleAdmin(isA(String.class))).thenReturn(Boolean.FALSE);

        requestContent = objectMapper.readTree(new File(getClass()
            .getResource(CONTESTED_VALIDATE_HEARING_SUCCESSFULLY_JSON).toURI()));
        mvc.perform(post("/case-orchestration/contested/set-paper-case-defaults")
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data.isAdmin", is(NO_VALUE)))
            .andExpect(jsonPath("$.data.fastTrackDecision", is(NO_VALUE)))
            .andExpect(jsonPath("$.data.paperApplication", is(YES_VALUE)))
            .andExpect(jsonPath("$.data.applicantRepresented", is(YES_VALUE)));
    }
}