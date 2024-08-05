package uk.gov.hmcts.reform.finrem.caseorchestration.handler.documentremoval;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class DocumentRemovalAboutToStartHandlerTest {

    private ObjectMapper objectMapper;

    private static final String DOCS_TO_REMOVE_JSON = "/fixtures/documentRemoval/documents-to-remove.json";
    private static final String NO_DOCS_TO_REMOVE_JSON = "/fixtures/documentRemoval/no-documents-to-remove.json";

    @InjectMocks
    private DocumentRemovalAboutToStartHandler handler;

    @BeforeEach
    public void setup() {
        objectMapper = new ObjectMapper();
    }

    @Test
    void shouldHandleEventContested() {
        assertTrue(handler.canHandle(CallbackType.ABOUT_TO_START, CaseType.CONTESTED, EventType.REMOVE_CASE_DOCUMENT));
    }

    @Test
    void shouldHandleEventConsented() {
        assertTrue(handler.canHandle(CallbackType.ABOUT_TO_START, CaseType.CONSENTED, EventType.REMOVE_CASE_DOCUMENT));
    }

    @Test
    void shouldGenerateDocumentListFromCaseData() {
        FinremCallbackRequest callbackRequestFromFile = FinremCallbackRequest.builder().caseDetails(buildCaseDetailsWithPath(DOCS_TO_REMOVE_JSON)).build();
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(callbackRequestFromFile, "authToken");
        assertEquals(6, response.getData().getDocumentToRemoveCollection().size());
    }

    @Test
    void shouldGenerateEmptyDocumentListFromCaseDataWithoutDocs() {
        FinremCallbackRequest callbackRequestFromFile = FinremCallbackRequest.builder().caseDetails(buildCaseDetailsWithPath(NO_DOCS_TO_REMOVE_JSON)).build();
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(callbackRequestFromFile, "authToken");
        assertEquals(0, response.getData().getDocumentToRemoveCollection().size());
    }

    // Copied from UpdateGereralApplicationStatusAboutToStartHandler.java - consider whether utility
    // Converts a JSON file to a FinremCallbackRequest to test a handler.
    private FinremCaseDetails buildCaseDetailsWithPath(String path) {
        try (InputStream resourceAsStream = getClass().getResourceAsStream(path)) {
            FinremCaseDetails caseDetails =
                    objectMapper.readValue(resourceAsStream, FinremCallbackRequest.class).getCaseDetails();
            return FinremCallbackRequest.builder().caseDetails(caseDetails).build().getCaseDetails();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
