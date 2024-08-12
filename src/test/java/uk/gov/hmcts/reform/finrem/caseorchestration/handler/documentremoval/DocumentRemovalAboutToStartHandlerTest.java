package uk.gov.hmcts.reform.finrem.caseorchestration.handler.documentremoval;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.provider.Arguments;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentremoval.DocumentRemovalService;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;


@ExtendWith(MockitoExtension.class)
class DocumentRemovalAboutToStartHandlerTest {

    @Mock
    private DocumentRemovalService documentRemovalService;

    @InjectMocks
    private DocumentRemovalAboutToStartHandler handler;

    private FinremCallbackRequest callbackRequest;
    private FinremCaseData caseData;

    @BeforeEach
    public void setup() {
        caseData = FinremCaseData.builder().build();
        FinremCaseDetails caseDetails = FinremCaseDetails.builder().data(caseData).build();
        callbackRequest = FinremCallbackRequest.builder().caseDetails(caseDetails).build();
    }

    @Test
    void shouldHandleAllCaseTypes() {
        assertCanHandle(handler,
            Arguments.of(CallbackType.ABOUT_TO_START, CaseType.CONSENTED, EventType.REMOVE_CASE_DOCUMENT),
            Arguments.of(CallbackType.ABOUT_TO_START, CaseType.CONTESTED, EventType.REMOVE_CASE_DOCUMENT)
        );
    }

    @Test
    void testHandleWithEmptyDocumentNodes() throws Exception {
        when(documentRemovalService.getDocumentNodes(caseData)).thenReturn(new ArrayList<>());
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(callbackRequest, "auth");

        assertTrue(response.getData().getDocumentToKeepCollection().isEmpty());
    }

    @Test
    void testHandleWithValidDocumentNodes() throws Exception {

        JsonNode documentNode = mock(JsonNode.class);
        when(documentNode.get("document_url")).thenReturn(mock(JsonNode.class));
        when(documentNode.get("document_filename")).thenReturn(mock(JsonNode.class));
        when(documentNode.get("document_binary_url")).thenReturn(mock(JsonNode.class));
        when(documentNode.get("document_url").asText()).thenReturn("http://example.com/doc/123");
        when(documentNode.get("document_binary_url").asText()).thenReturn("http://example.com/doc/123/binary");
        when(documentNode.get("document_filename").asText()).thenReturn("example.pdf");

        List<JsonNode> documentNodes = new ArrayList<>();
        documentNodes.add(documentNode);

        when(documentRemovalService.getDocumentNodes(caseData)).thenReturn(documentNodes);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(callbackRequest, "auth");

        assertEquals(1, response.getData().getDocumentToKeepCollection().size());
        assertEquals("http://example.com/doc/123", response.getData().getDocumentToKeepCollection().get(0).getValue().getCaseDocument().getDocumentUrl());
        assertEquals("http://example.com/doc/123/binary", response.getData().getDocumentToKeepCollection().get(0).getValue().getCaseDocument().getDocumentBinaryUrl());
        assertEquals("example.pdf", response.getData().getDocumentToKeepCollection().get(0).getValue().getCaseDocument().getDocumentFilename());
        assertEquals("123", response.getData().getDocumentToKeepCollection().get(0).getValue().getDocumentId());
    }

    @Test
    void testHandleWithDuplicateDocumentNodes() throws Exception {

        JsonNode documentNode = mock(JsonNode.class);
        when(documentNode.get("document_url")).thenReturn(mock(JsonNode.class));
        when(documentNode.get("document_filename")).thenReturn(mock(JsonNode.class));
        when(documentNode.get("document_binary_url")).thenReturn(mock(JsonNode.class));
        when(documentNode.get("document_url").asText()).thenReturn("http://example.com/doc/123");
        when(documentNode.get("document_binary_url").asText()).thenReturn("http://example.com/doc/123/binary");
        when(documentNode.get("document_filename").asText()).thenReturn("example.pdf");

        List<JsonNode> documentNodes = new ArrayList<>();
        documentNodes.add(documentNode);
        documentNodes.add(documentNode); // Duplicate

        when(documentRemovalService.getDocumentNodes(caseData)).thenReturn(documentNodes);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(callbackRequest, "auth");

        assertEquals(1, response.getData().getDocumentToKeepCollection().size());
        assertEquals("http://example.com/doc/123", response.getData().getDocumentToKeepCollection().get(0).getValue().getCaseDocument().getDocumentUrl());
        assertEquals("http://example.com/doc/123/binary", response.getData().getDocumentToKeepCollection().get(0).getValue().getCaseDocument().getDocumentBinaryUrl());
        assertEquals("example.pdf", response.getData().getDocumentToKeepCollection().get(0).getValue().getCaseDocument().getDocumentFilename());
        assertEquals("123", response.getData().getDocumentToKeepCollection().get(0).getValue().getDocumentId());
    }
}
