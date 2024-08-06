package uk.gov.hmcts.reform.finrem.caseorchestration.service.documentremoval;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class DocumentRemovalServiceTest {

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
    void testEmptyObject() throws Exception {
        String json = "{}";
        JsonNode root = objectMapper.readTree(json);

        documentRemovalService.retrieveDocumentNodes(root, documentNodes);

        assertEquals(0, documentNodes.size());
    }

    @Test
    void testEmptyArray() throws Exception {
        String json = "[]";
        JsonNode root = objectMapper.readTree(json);

        documentRemovalService.retrieveDocumentNodes(root, documentNodes);

        assertEquals(0, documentNodes.size());
    }

    @Test
    void testDocumentWithUploadTimestamp() throws Exception {
        String json = "{\"formC\":{\"document_url\":\"https://example.com\",  \"upload_timestamp\": \"2024-07-21T12:24:58.964127000\",\"document_filename\":\"Form-C.pdf\",\"document_binary_url\":\"https://example.com/binary\"}}";
        JsonNode root = objectMapper.readTree(json);

        documentRemovalService.retrieveDocumentNodes(root, documentNodes);

        assertEquals(1, documentNodes.size());
        assertEquals("https://example.com", documentNodes.get(0).get("document_url").asText());
        assertEquals("2024-07-21T12:24:58.964127000", documentNodes.get(0).get("upload_timestamp").asText());
        assertEquals("Form-C.pdf", documentNodes.get(0).get("document_filename").asText());
        assertEquals("https://example.com/binary", documentNodes.get(0).get("document_binary_url").asText());
    }

    @Test
    void testSingleObjectWithDocumentUrl() throws Exception {
        String json = "{\"formC\":{\"document_url\":\"https://example.com\",\"document_filename\":\"Form-C.pdf\",\"document_binary_url\":\"https://example.com/binary\"}}";
        JsonNode root = objectMapper.readTree(json);

        documentRemovalService.retrieveDocumentNodes(root, documentNodes);

        assertEquals(1, documentNodes.size());
        assertEquals("https://example.com", documentNodes.get(0).get("document_url").asText());
        assertEquals("Form-C.pdf", documentNodes.get(0).get("document_filename").asText());
        assertEquals("https://example.com/binary", documentNodes.get(0).get("document_binary_url").asText());
    }

    @Test
    void testNestedObjectWithDocumentUrl() throws Exception {
        String json = "{\"nested\":{\"formC\":{\"document_url\":\"https://example.com\",\"document_filename\":\"Form-C.pdf\",\"document_binary_url\":\"https://example.com/binary\"}}}";
        JsonNode root = objectMapper.readTree(json);

        documentRemovalService.retrieveDocumentNodes(root, documentNodes);

        assertEquals(1, documentNodes.size());
        assertEquals("https://example.com", documentNodes.get(0).get("document_url").asText());
        assertEquals("Form-C.pdf", documentNodes.get(0).get("document_filename").asText());
        assertEquals("https://example.com/binary", documentNodes.get(0).get("document_binary_url").asText());
    }

    @Test
    void testArrayWithDocumentUrls() throws Exception {
        String json = "{\"array\":[{\"formA\":{\"document_url\":\"https://example1.com\",\"document_filename\":\"Form-A.pdf\",\"document_binary_url\":\"https://example1.com/binary\"}}, {\"formB\":{\"document_url\":\"https://example2.com\",\"document_filename\":\"Form-B.pdf\",\"document_binary_url\":\"https://example2.com/binary\"}}]}";
        JsonNode root = objectMapper.readTree(json);

        documentRemovalService.retrieveDocumentNodes(root, documentNodes);

        assertEquals(2, documentNodes.size());

        assertEquals("https://example1.com", documentNodes.get(0).get("document_url").asText());
        assertEquals("Form-A.pdf", documentNodes.get(0).get("document_filename").asText());
        assertEquals("https://example1.com/binary", documentNodes.get(0).get("document_binary_url").asText());

        assertEquals("https://example2.com", documentNodes.get(1).get("document_url").asText());
        assertEquals("Form-B.pdf", documentNodes.get(1).get("document_filename").asText());
        assertEquals("https://example2.com/binary", documentNodes.get(1).get("document_binary_url").asText());
    }

    @Test
    void testComplexNestedArrayStructure() throws Exception {
        String json = "{\"array\":[{\"nestedArray1\":[{\"formA\":{\"document_url\":\"https://example1.com\",\"document_filename\":\"Form-A.pdf\",\"document_binary_url\":\"https://example1.com/binary\"}},{\"formB\":{\"document_url\":\"https://example2.com\",\"document_filename\":\"Form-B.pdf\",\"document_binary_url\":\"https://example2.com/binary\"}}]},{\"nestedArray2\":[{\"formC\":{\"document_url\":\"https://example3.com\",\"document_filename\":\"Form-C.pdf\",\"document_binary_url\":\"https://example3.com/binary\"}},{\"formD\":{\"document_url\":\"https://example4.com\",\"document_filename\":\"Form-D.pdf\",\"document_binary_url\":\"https://example4.com/binary\"}}]}]}";
        JsonNode root = objectMapper.readTree(json);

        documentRemovalService.retrieveDocumentNodes(root, documentNodes);

        assertEquals(4, documentNodes.size());

        assertEquals("https://example1.com", documentNodes.get(0).get("document_url").asText());
        assertEquals("Form-A.pdf", documentNodes.get(0).get("document_filename").asText());
        assertEquals("https://example1.com/binary", documentNodes.get(0).get("document_binary_url").asText());

        assertEquals("https://example2.com", documentNodes.get(1).get("document_url").asText());
        assertEquals("Form-B.pdf", documentNodes.get(1).get("document_filename").asText());
        assertEquals("https://example2.com/binary", documentNodes.get(1).get("document_binary_url").asText());

        assertEquals("https://example3.com", documentNodes.get(2).get("document_url").asText());
        assertEquals("Form-C.pdf", documentNodes.get(2).get("document_filename").asText());
        assertEquals("https://example3.com/binary", documentNodes.get(2).get("document_binary_url").asText());

        assertEquals("https://example4.com", documentNodes.get(3).get("document_url").asText());
        assertEquals("Form-D.pdf", documentNodes.get(3).get("document_filename").asText());
        assertEquals("https://example4.com/binary", documentNodes.get(3).get("document_binary_url").asText());
    }
}
