package uk.gov.hmcts.reform.finrem.caseorchestration.service.documentremoval;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentToKeepCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentRemovalServiceTest {

    private ObjectMapper objectMapper;

    private DocumentRemovalService documentRemovalService;

    @Mock
    private GenericDocumentService genericDocumentService;

    @Mock
    private ObjectMapper mockObjectMapper;

    @BeforeEach
    public void setUp() {
        objectMapper = new ObjectMapper();
        documentRemovalService = new DocumentRemovalService(mockObjectMapper, genericDocumentService);
    }

    @Test
    void testGetCaseDocumentsList_EmptyObject() throws Exception {

        FinremCaseData caseData = new FinremCaseData();

        String json = "{}";
        JsonNode root = objectMapper.readTree(json);

        when(mockObjectMapper.valueToTree(caseData)).thenReturn(root);
        List<DocumentToKeepCollection> result = documentRemovalService.getCaseDocumentsList(caseData);

        assertEquals(0, result.size());
    }

    @Test
    void testGetCaseDocumentsList_testEmptyArray() throws Exception {
        FinremCaseData caseData = new FinremCaseData();

        String json = "[]";
        JsonNode root = objectMapper.readTree(json);

        when(mockObjectMapper.valueToTree(caseData)).thenReturn(root);
        List<DocumentToKeepCollection> result = documentRemovalService.getCaseDocumentsList(caseData);

        assertEquals(0, result.size());
    }

    @Test
    void testGetCaseDocumentsList_SingleDocument() throws Exception {
        FinremCaseData caseData = new FinremCaseData();

        String json = """
             {
               "formC": {
                 "document_url": "https://example.com/123",
                 "upload_timestamp": "2024-07-21T12:24:58.964127000",
                 "document_filename": "Form-C.pdf",
                 "document_binary_url": "https://example.com/binary"
               }
             }
            """;

        JsonNode root = objectMapper.readTree(json);

        when(mockObjectMapper.valueToTree(caseData)).thenReturn(root);
        List<DocumentToKeepCollection> result = documentRemovalService.getCaseDocumentsList(caseData);

        assertEquals(1, result.size());
        assertEquals("https://example.com/123", result.get(0).getValue().getCaseDocument().getDocumentUrl());
        assertEquals("Form-C.pdf", result.get(0).getValue().getCaseDocument().getDocumentFilename());
        assertEquals("https://example.com/binary", result.get(0).getValue().getCaseDocument().getDocumentBinaryUrl());
        assertEquals("123", result.get(0).getValue().getDocumentId());
    }

    @Test
    void testGetCaseDocumentsList_withDuplicateDocument() throws Exception {
        FinremCaseData caseData = new FinremCaseData();

        String json = """
             {
               "formC": {
                 "document_url": "https://example.com/123",
                 "document_filename": "Form-C.pdf",
                 "document_binary_url": "https://example.com/binary"
               },
               "formD": {
                 "document_url": "https://example.com/123",
                 "document_filename": "Form-C.pdf",
                 "document_binary_url": "https://example.com/binary"
               }
             }
            """;

        JsonNode root = objectMapper.readTree(json);

        when(mockObjectMapper.valueToTree(caseData)).thenReturn(root);
        List<DocumentToKeepCollection> result = documentRemovalService.getCaseDocumentsList(caseData);

        assertEquals(1, result.size());
        assertEquals("https://example.com/123", result.get(0).getValue().getCaseDocument().getDocumentUrl());
        assertEquals("Form-C.pdf", result.get(0).getValue().getCaseDocument().getDocumentFilename());
        assertEquals("https://example.com/binary", result.get(0).getValue().getCaseDocument().getDocumentBinaryUrl());
        assertEquals("123", result.get(0).getValue().getDocumentId());
    }


    @Test
    void testGetCaseDocumentsList_NestedObjectWithDocumentUrl() throws Exception {
        FinremCaseData caseData = new FinremCaseData();

        String json = """
            {"nested":
                {"formC":
                    {
                        "document_url":"https://example.com/123",
                        "document_filename":"Form-C.pdf",
                        "document_binary_url":"https://example.com/binary"
                    }
                }
             }
            """;
        JsonNode root = objectMapper.readTree(json);

        when(mockObjectMapper.valueToTree(caseData)).thenReturn(root);
        List<DocumentToKeepCollection> result = documentRemovalService.getCaseDocumentsList(caseData);

        assertEquals(1, result.size());
        assertEquals("https://example.com/123", result.get(0).getValue().getCaseDocument().getDocumentUrl());
        assertEquals("Form-C.pdf", result.get(0).getValue().getCaseDocument().getDocumentFilename());
        assertEquals("https://example.com/binary", result.get(0).getValue().getCaseDocument().getDocumentBinaryUrl());
        assertEquals("123", result.get(0).getValue().getDocumentId());
    }

    @Test
    void testGetCaseDocumentsList_ArrayWithDocumentUrls() throws Exception {
        FinremCaseData caseData = new FinremCaseData();

        String json = """
            {"array":
                [
                    {"formA":
                        {
                            "document_url":"https://example1.com/123",
                            "document_filename":"Form-A.pdf",
                            "document_binary_url":"https://example1.com/binary"
                        }
                    },
                    {"formB":
                        {
                            "document_url":"https://example2.com/456",
                            "document_filename":"Form-B.pdf",
                            "document_binary_url":"https://example2.com/binary"
                        }
                    }
                ]
            }
            """;

        JsonNode root = objectMapper.readTree(json);

        when(mockObjectMapper.valueToTree(caseData)).thenReturn(root);
        List<DocumentToKeepCollection> result = documentRemovalService.getCaseDocumentsList(caseData);

        assertEquals(2, result.size());

        assertEquals("https://example1.com/123", result.get(0).getValue().getCaseDocument().getDocumentUrl());
        assertEquals("Form-A.pdf", result.get(0).getValue().getCaseDocument().getDocumentFilename());
        assertEquals("https://example1.com/binary", result.get(0).getValue().getCaseDocument().getDocumentBinaryUrl());
        assertEquals("123", result.get(0).getValue().getDocumentId());

        assertEquals("https://example2.com/456", result.get(1).getValue().getCaseDocument().getDocumentUrl());
        assertEquals("Form-B.pdf", result.get(1).getValue().getCaseDocument().getDocumentFilename());
        assertEquals("https://example2.com/binary", result.get(1).getValue().getCaseDocument().getDocumentBinaryUrl());
        assertEquals("456", result.get(1).getValue().getDocumentId());
    }

    @Test
    void testComplexNestedArrayStructure() throws Exception {
        FinremCaseData caseData = new FinremCaseData();

        String json = """
            {"array":
                [
                    {"nestedArray1":
                        [
                            {"formA":
                                {
                                    "document_url":"https://example1.com/123",
                                    "document_filename":"Form-A.pdf",
                                    "document_binary_url":"https://example1.com/binary"
                                }
                            },
                            {"formB":
                                {
                                    "document_url":"https://example2.com/456",
                                    "document_filename":"Form-B.pdf",
                                    "document_binary_url":"https://example2.com/binary"
                                }
                            }
                        ]
                    },
                    {"nestedArray2":
                        [
                            {"formC":
                                {
                                    "document_url":"https://example3.com/789",
                                    "document_filename":"Form-C.pdf",
                                    "document_binary_url":"https://example3.com/binary"
                                }
                            },
                            {"formD":
                                {
                                    "document_url":"https://example4.com/987",
                                    "document_filename":"Form-D.pdf",
                                    "document_binary_url":"https://example4.com/binary"
                                }
                            }
                        ]
                    }
                ]
            }
            """;

        JsonNode root = objectMapper.readTree(json);

        when(mockObjectMapper.valueToTree(caseData)).thenReturn(root);
        List<DocumentToKeepCollection> result = documentRemovalService.getCaseDocumentsList(caseData);

        assertEquals(4, result.size());

        assertEquals("https://example1.com/123", result.get(0).getValue().getCaseDocument().getDocumentUrl());
        assertEquals("Form-A.pdf", result.get(0).getValue().getCaseDocument().getDocumentFilename());
        assertEquals("https://example1.com/binary", result.get(0).getValue().getCaseDocument().getDocumentBinaryUrl());
        assertEquals("123", result.get(0).getValue().getDocumentId());

        assertEquals("https://example2.com/456", result.get(1).getValue().getCaseDocument().getDocumentUrl());
        assertEquals("Form-B.pdf", result.get(1).getValue().getCaseDocument().getDocumentFilename());
        assertEquals("https://example2.com/binary", result.get(1).getValue().getCaseDocument().getDocumentBinaryUrl());
        assertEquals("456", result.get(1).getValue().getDocumentId());

        assertEquals("https://example3.com/789", result.get(2).getValue().getCaseDocument().getDocumentUrl());
        assertEquals("Form-C.pdf", result.get(2).getValue().getCaseDocument().getDocumentFilename());
        assertEquals("https://example3.com/binary", result.get(2).getValue().getCaseDocument().getDocumentBinaryUrl());
        assertEquals("789", result.get(2).getValue().getDocumentId());

        assertEquals("https://example4.com/987", result.get(3).getValue().getCaseDocument().getDocumentUrl());
        assertEquals("Form-D.pdf", result.get(3).getValue().getCaseDocument().getDocumentFilename());
        assertEquals("https://example4.com/binary", result.get(3).getValue().getCaseDocument().getDocumentBinaryUrl());
        assertEquals("987", result.get(3).getValue().getDocumentId());
    }
}
