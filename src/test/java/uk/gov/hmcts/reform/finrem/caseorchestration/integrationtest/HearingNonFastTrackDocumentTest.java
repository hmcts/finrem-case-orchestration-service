package uk.gov.hmcts.reform.finrem.caseorchestration.integrationtest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Resources;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.CaseOrchestrationApplication;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.FrcCourtDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.serialisation.FinremCallbackRequestDeserializer;
import uk.gov.hmcts.reform.finrem.ccd.callback.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.ccd.callback.CallbackRequest;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.feignError;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.newDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.ValidateHearingService.DATE_BETWEEN_12_AND_16_WEEKS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.ValidateHearingService.REQUIRED_FIELD_EMPTY_ERROR;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = CaseOrchestrationApplication.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@PropertySource(value = "classpath:application.properties")
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@Category(IntegrationTest.class)
public class HearingNonFastTrackDocumentTest extends BaseTest {

    protected static final String CASE_DETAILS = "caseDetails";
    protected static final String CASE_DATA = "case_data";

    private static final String API_URL = "/case-orchestration/documents/hearing";
    private static final String JSON_CONTENT_PATH = "/fixtures/contested/validate-hearing-withoutfastTrackDecision.json";

    @Autowired protected ObjectMapper objectMapper;
    @Autowired protected MockMvc webClient;
    @Autowired protected DocumentConfiguration config;
    @Qualifier("finremCallbackRequestDeserializer")
    @Autowired protected FinremCallbackRequestDeserializer deserializer;

    @MockBean
    GenericDocumentService genericDocumentService;

    @Captor
    ArgumentCaptor<Map<String, Object>> formCPlaceholdersMapCaptor;

    @Captor
    ArgumentCaptor<Map<String, Object>> formGPlaceholdersMapCaptor;

    private static String requestJson;
    protected CallbackRequest request;

    @BeforeClass
    public static void loadJsonString() throws IOException {
        requestJson = Resources.toString(HearingNonFastTrackDocumentTest.class.getResource(JSON_CONTENT_PATH), StandardCharsets.UTF_8);
    }

    @Before
    public void setUp() throws IOException {
        request = deserializer.deserialize(requestJson);
    }

    @Test
    public void missingIssueDate() throws Exception {
        FinremCaseDetails caseDetails = request.getCaseDetails();
        caseDetails.getCaseData().setIssueDate(null);

        performRequestWithExpectedError();
    }

    @Test
    public void missingHearingDate() throws Exception {
        FinremCaseDetails caseDetails = request.getCaseDetails();
        caseDetails.getCaseData().setHearingDate(null);

        performRequestWithExpectedError();
    }

    @Test
    public void missingFastTrackDecision() throws Exception {
        FinremCaseDetails caseDetails = request.getCaseDetails();
        caseDetails.getCaseData().setFastTrackDecision(null);

        performRequestWithExpectedError();
    }

    private void performRequestWithExpectedError() throws Exception {
        webClient.perform(MockMvcRequestBuilders.post(API_URL)
                .content(objectMapper.writeValueAsString(request))
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(APPLICATION_JSON_VALUE)
                .accept(APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(content().json(expectedErrorData(), true));
    }

    @Test
    public void generateFormCAndFormGSuccess() throws Exception {
        setUpMockContext();

        MvcResult mvcResult = getMvcResult();

        assertThat(mvcResult.getResponse().getStatus(), is(HttpStatus.OK.value()));
        assertThat(mvcResult.getResponse().getContentAsString(), is(expectedCaseData()));

        assertDocumentServiceInteraction();
        assertPlaceHoldersMap(formCPlaceholdersMapCaptor);
        assertPlaceHoldersMap(formGPlaceholdersMapCaptor);
    }

    @Test
    public void generateFormCAndFormGServiceError() throws Exception {
        setUpErrorMockContext();

        webClient.perform(MockMvcRequestBuilders.post(API_URL)
            .content(objectMapper.writeValueAsString(request))
            .header(AUTHORIZATION, AUTH_TOKEN)
            .contentType(APPLICATION_JSON_VALUE)
            .accept(APPLICATION_JSON_VALUE))
            .andExpect(status().isInternalServerError());
    }

    private MvcResult getMvcResult() throws Exception {
        MvcResult mvcResult;
        int requestsMade = 0;
        do {
            if (requestsMade > 0) {
                Thread.sleep(100);
            }
            mvcResult = performRequest();
        } while (++requestsMade < 5 && mvcResult.getResponse().getStatus() == HttpStatus.INTERNAL_SERVER_ERROR.value());

        if (requestsMade > 1) {
            System.out.println("generateFormCAndFormGSuccess requests made: " + requestsMade);
        }
        return mvcResult;
    }

    private MvcResult performRequest() throws Exception {
        return webClient.perform(MockMvcRequestBuilders.post(API_URL)
                .content(objectMapper.writeValueAsString(request))
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(APPLICATION_JSON_VALUE)
                .accept(APPLICATION_JSON_VALUE))
            .andReturn();
    }

    private void setUpMockContext() {
        when(genericDocumentService.generateDocumentFromPlaceholdersMap(any(), any(),
            eq(config.getFormCNonFastTrackTemplate()), eq(config.getFormCFileName()))).thenReturn(newDocument());
        when(genericDocumentService.generateDocumentFromPlaceholdersMap(any(), any(),
            eq(config.getFormGTemplate()), eq(config.getFormGFileName()))).thenReturn(newDocument());
        when(genericDocumentService.generateDocumentFromPlaceholdersMap(any(), any(),
            eq(config.getOutOfFamilyCourtResolutionTemplate()), eq(config.getOutOfFamilyCourtResolutionName())).thenReturn(newDocument()));
        when(genericDocumentService.generateDocumentFromPlaceholdersMap(any(), any(),
            eq(config.getBulkPrintTemplate()), eq(config.getBulkPrintFileName()))).thenReturn(newDocument());
    }

    private void setUpErrorMockContext() {
        when(genericDocumentService.generateDocumentFromPlaceholdersMap(any(), any(),
            eq(config.getFormCNonFastTrackTemplate()), eq(config.getFormCFileName()))).thenThrow(feignError());
        when(genericDocumentService.generateDocumentFromPlaceholdersMap(any(), any(),
            eq(config.getFormGTemplate()), eq(config.getFormGFileName()))).thenThrow(feignError());
    }

    private void assertDocumentServiceInteraction() {
        verify(genericDocumentService, atLeastOnce()).generateDocumentFromPlaceholdersMap(any(), formCPlaceholdersMapCaptor.capture(),
            eq(config.getFormCNonFastTrackTemplate()),  eq(config.getFormCFileName()));
        verify(genericDocumentService, atLeastOnce()).generateDocumentFromPlaceholdersMap(any(), formGPlaceholdersMapCaptor.capture(),
            eq(config.getFormGTemplate()), eq(config.getFormGFileName()));
        verify(genericDocumentService, atLeastOnce()).generateDocumentFromPlaceholdersMap(any(), newPlaceholdersMapCaptor.capture(),
            eq(config.getOutOfFamilyCourtResolutionTemplate()), eq(config.getOutOfFamilyCourtResolutionName()));

    }

    private String expectedErrorData() throws JsonProcessingException {
        return objectMapper.writeValueAsString(
            AboutToStartOrSubmitCallbackResponse.builder()
                .errors(ImmutableList.of(REQUIRED_FIELD_EMPTY_ERROR))
                .build());
    }

    private String expectedCaseData() throws JsonProcessingException {
        FinremCaseDetails caseDetails = request.getCaseDetails();
        caseDetails.getCaseData().setFormC(newDocument());
        caseDetails.getCaseData().setFormG(newDocument());
        caseDetails.getData().put(OUT_OF_FAMILY_COURT_RESOLUTION, caseDocument());
        caseDetails.getCaseData().setBulkPrintCoverSheetApp(newDocument());
        caseDetails.getCaseData().setBulkPrintCoverSheetRes(newDocument());

        return objectMapper.writeValueAsString(
            AboutToStartOrSubmitCallbackResponse.builder()
                .data(caseDetails.getCaseData())
                .warnings(ImmutableList.of(DATE_BETWEEN_12_AND_16_WEEKS))
                .build());
    }

    private void assertPlaceHoldersMap(ArgumentCaptor<Map<String, Object>> placeholdersMapCaptor) {
        Map<String, Object> caseDetailsMap = (Map<String, Object>) placeholdersMapCaptor.getValue().get(CASE_DETAILS);
        Map<String, Object> placeholdersMap = (Map<String, Object>) caseDetailsMap.get(CASE_DATA);
        assertEquals("Poor", placeholdersMap.get("applicantFMName"));
        assertEquals("Guy", placeholdersMap.get("applicantLName"));
        assertEquals("DD12D12345", placeholdersMap.get("divorceCaseNumber"));
        assertEquals("2019-03-24", placeholdersMap.get("hearingDate"));
        FrcCourtDetails courtDetails = objectMapper.convertValue(placeholdersMap.get("courtDetails"), FrcCourtDetails.class);
        assertEquals("Bristol Civil and Family Justice Centre", courtDetails.getCourtName());
        assertEquals("2 Redcliff Street, Bristol, BS1 6GR", courtDetails.getCourtAddress());
    }
}
