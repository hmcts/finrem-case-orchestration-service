package uk.gov.hmcts.reform.finrem.caseorchestration.service.documentremoval;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;


import java.util.ArrayList;
import java.util.List;


import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class DocumentRemovalServiceTest extends BaseServiceTest {

    private static final String DOCS_TO_REMOVE_JSON = "/fixtures/documentRemoval/documents-to-remove.json";
    private static final String NO_DOCS_TO_REMOVE_JSON = "/fixtures/documentRemoval/no-documents-to-remove.json";

    private ObjectMapper objectMapper;
    private List<JsonNode> documentNodes;

    private DocumentRemovalService documentRemovalService;

    @BeforeEach
    public void setUp() {
        objectMapper = new ObjectMapper();
        documentNodes = new ArrayList<>();
        documentRemovalService = new DocumentRemovalService();
    }

    @Test
    void testSingleObjectWithDocumentUrl() throws Exception {
        String json = "{\"formC\":{\"document_url\":\"http://example.com\",\"document_filename\":\"Form-C.pdf\",\"document_binary_url\":\"http://example.com\"}}";
        JsonNode root = objectMapper.readTree(json);

        documentRemovalService.retrieveDocumentNodes(root, documentNodes);

        assertEquals(1, documentNodes.size());
        assertEquals("http://example.com", documentNodes.get(0).get("document_url").asText());
    }

//    @Test
//    public void testNestedObjectWithDocumentUrl() throws Exception {
//        String json = "{ \"nested\": { \"document_url\": \"http://example.com\" } }";
//        JsonNode root = objectMapper.readTree(json);
//
//        retrieveDocumentNodes(root, documentNodes);
//
//        assertEquals(1, documentNodes.size());
//        assertEquals("http://example.com", documentNodes.get(0).get("document_url").asText());
//    }
//
//    @Test
//    public void testArrayWithDocumentUrls() throws Exception {
//        String json = "[{ \"document_url\": \"http://example1.com\" }, { \"document_url\": \"http://example2.com\" }]";
//        JsonNode root = objectMapper.readTree(json);
//
//        retrieveDocumentNodes(root, documentNodes);
//
//        assertEquals(2, documentNodes.size());
//        assertEquals("http://example1.com", documentNodes.get(0).get("document_url").asText());
//        assertEquals("http://example2.com", documentNodes.get(1).get("document_url").asText());
//    }
//
//    @Test
//    public void testComplexNestedStructure() throws Exception {
//        String json = "{ \"level1\": { \"level2\": [ { \"document_url\": \"http://example1.com\" }, { \"level3\": { \"document_url\": \"http://example2.com\" } } ] } }";
//        JsonNode root = objectMapper.readTree(json);
//
//        retrieveDocumentNodes(root, documentNodes);
//
//        assertEquals(2, documentNodes.size());
//        assertEquals("http://example1.com", documentNodes.get(0).get("document_url").asText());
//        assertEquals("http://example2.com", documentNodes.get(1).get("document_url").asText());
//    }
//
//    @Test
//    public void testEmptyObject() throws Exception {
//        String json = "{}";
//        JsonNode root = objectMapper.readTree(json);
//
//        retrieveDocumentNodes(root, documentNodes);
//
//        assertEquals(0, documentNodes.size());
//    }
//
//    @Test
//    public void testEmptyArray() throws Exception {
//        String json = "[]";
//        JsonNode root = objectMapper.readTree(json);
//
//        retrieveDocumentNodes(root, documentNodes);
//
//        assertEquals(0, documentNodes.size());
//    }

}
