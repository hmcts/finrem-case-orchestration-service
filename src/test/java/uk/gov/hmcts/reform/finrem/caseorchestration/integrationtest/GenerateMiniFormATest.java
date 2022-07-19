package uk.gov.hmcts.reform.finrem.caseorchestration.integrationtest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentGenerationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;
import uk.gov.hmcts.reform.finrem.ccd.callback.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.ccd.domain.ChildrenOrder;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.NatureApplication;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.feignError;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.newDocument;

public class GenerateMiniFormATest extends AbstractDocumentTest {

    protected static final String CASE_DETAILS = "caseDetails";
    protected static final String CASE_DATA = "case_data";
    private static final String API_URL = "/case-orchestration/documents/generate-mini-form-a";

    @MockBean
    GenericDocumentService genericDocumentServiceMock;

    @Captor
    ArgumentCaptor<Map<String, Object>> placeholdersMapCaptor;

    @Override
    protected String apiUrl() {
        return API_URL;
    }

    @Test
    public void generateMiniFormA() throws Exception {
        setUpMockContext();
        idamServiceStub();
        generateDocument();

        verify(genericDocumentServiceMock, times(1))
            .generateDocumentFromPlaceholdersMap(any(), placeholdersMapCaptor.capture(),
                eq(documentConfiguration.getMiniFormTemplate()),  eq(documentConfiguration.getMiniFormFileName()));

        assertPlaceholdersMap();
    }

    @Test
    public void documentGeneratorServiceError() throws Exception {
        when(genericDocumentServiceMock.generateDocumentFromPlaceholdersMap(any(), any(),
            eq(documentConfiguration.getMiniFormTemplate()),
            eq(documentConfiguration.getMiniFormFileName()))).thenThrow(feignError());

        webClient.perform(MockMvcRequestBuilders.post(apiUrl())
                .content(objectMapper.writeValueAsString(newRequest))
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(APPLICATION_JSON_VALUE)
                .accept(APPLICATION_JSON_VALUE))
            .andExpect(status().isInternalServerError());
    }

    void setUpMockContext() {
        when(genericDocumentServiceMock.generateDocumentFromPlaceholdersMap(any(), any(),
            eq(documentConfiguration.getMiniFormTemplate()),
            eq(documentConfiguration.getMiniFormFileName()))).thenReturn(newDocument());
    }

    void generateDocument() throws Exception {
        webClient.perform(MockMvcRequestBuilders.post(apiUrl())
            .content(requestContent.toString())
            .header(AUTHORIZATION, AUTH_TOKEN)
            .contentType(APPLICATION_JSON_VALUE)
            .accept(APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(content().json(getCaseDataAsString()));
    }

    private Map<String, Object> expectedCaseData() {
        FinremCaseDetails caseDetails = newRequest.getCaseDetails();
        caseDetails.getCaseData().setMiniFormA(newDocument());

        return objectMapper.convertValue(
            AboutToStartOrSubmitCallbackResponse.builder().data(caseDetails.getCaseData()).build(),
            TypeFactory.defaultInstance().constructMapType(HashMap.class, String.class, Object.class));
    }

    private String getCaseDataAsString() throws JsonProcessingException {
        return objectMapper.writeValueAsString(expectedCaseData());
    }

    @Override
    protected DocumentGenerationRequest documentRequest() {
        return DocumentGenerationRequest.builder()
            .template(documentConfiguration.getMiniFormTemplate())
            .fileName(documentConfiguration.getMiniFormFileName())
            .values(expectedCaseData())
            .build();
    }

    CaseDetails copyWithOptionValueTranslation(CaseDetails caseDetails) {
        try {
            CaseDetails deepCopy = objectMapper.readValue(objectMapper.writeValueAsString(caseDetails), CaseDetails.class);

            optionIdToValueTranslator.translateFixedListOptions(deepCopy);
            return deepCopy;
        } catch (IOException e) {
            throw new IllegalStateException();
        }
    }

    private void assertPlaceholdersMap() {
        Map<String, Object> caseDetailsMap = (Map<String, Object>) placeholdersMapCaptor.getValue().get(CASE_DETAILS);
        Map<String, Object> placeholdersMap = (Map<String, Object>) caseDetailsMap.get(CASE_DATA);
        assertTrue(((List<String>) placeholdersMap.get("natureOfApplication2")).containsAll(getNatureApplication2List()));
        assertTrue(((List<String>) placeholdersMap.get("natureOfApplication6")).containsAll(getNatureApplication6List()));
        assertEquals("Poor", placeholdersMap.get("applicantFMName"));
        assertEquals("LL01", placeholdersMap.get("solicitorReference"));
        assertEquals(NO_VALUE, placeholdersMap.get("natureOfApplication5"));
        assertEquals(NO_VALUE, placeholdersMap.get("respondentAddressConfidential"));
        assertEquals("9963472494", placeholdersMap.get("respondentPhone"));
        assertEquals("2010-01-01", placeholdersMap.get("authorisation3"));
        assertEquals("Guy", placeholdersMap.get("applicantLName"));
        assertEquals(YES_VALUE, placeholdersMap.get("orderForChildrenQuestion1"));
        assertEquals("DD12D12345", placeholdersMap.get("divorceCaseNumber"));
        assertEquals("Korivi", placeholdersMap.get("appRespondentLName"));
        assertEquals("test", placeholdersMap.get("authorisationFirm"));
    }

    private List<String> getNatureApplication2List() {
        return List.of(NatureApplication.LUMP_SUM_ORDER.getText(),
            NatureApplication.PENSION_SHARING_ORDER.getText(),
            NatureApplication.PENSION_ATTACHMENT_ORDER.getText(),
            NatureApplication.PENSION_COMPENSATION_ATTACHMENT_ORDER.getText(),
            NatureApplication.A_SETTLEMENT_OR_A_TRANSFER_OF_PROPERTY.getText());
    }

    private List<String> getNatureApplication6List() {
        return List.of(ChildrenOrder.STEP_CHILD_OR_STEP_CHILDREN.getValue(),
            ChildrenOrder.DISABILITY_EXPENSES.getValue(),
            ChildrenOrder.IN_ADDITION_TO_CHILD_SUPPORT.getValue(),
            ChildrenOrder.TRAINING.getValue(),
            ChildrenOrder.WHEN_NOT_HABITUALLY_RESIDENT.getValue());
    }
}