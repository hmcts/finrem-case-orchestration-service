package uk.gov.hmcts.reform.finrem.caseorchestration.service.documentremoval;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ApproveOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ApprovedOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentToKeep;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentToKeepCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.OrderWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;


@ExtendWith(MockitoExtension.class)
class DocumentRemovalServiceTest {

    private DocumentRemovalService documentRemovalService;

    private ObjectMapper objectMapperForTest;

    @Mock
    private GenericDocumentService genericDocumentService;

    @Mock
    private FeatureToggleService featureToggleService;

    @BeforeEach
    public void setUp() {
        objectMapperForTest = new ObjectMapper();
        documentRemovalService = new DocumentRemovalService(objectMapperForTest, genericDocumentService, featureToggleService);
    }

    @Test
    void testGetCaseDocumentsList_EmptyObject() throws Exception {
        List<DocumentToKeepCollection> result = documentRemovalService.getCaseDocumentsList(FinremCaseData.builder()
            .ccdCaseId(TestConstants.CASE_ID)
            .build());
        assertEquals(0, result.size());
    }

    @Test
    void testGetCaseDocumentsList_testEmptyArray() throws Exception {

        List<DocumentToKeepCollection> result = documentRemovalService.getCaseDocumentsList(FinremCaseData.builder()
            .ccdCaseId(TestConstants.CASE_ID)
            .uploadDocuments(new ArrayList<>())
            .build());
        assertEquals(0, result.size());
    }

    @Test
    void testGetCaseDocumentsList_RootDocument() throws Exception {

        List<DocumentToKeepCollection> result = documentRemovalService.getCaseDocumentsList(FinremCaseData.builder()
            .ccdCaseId(TestConstants.CASE_ID)
            .miniFormA(CaseDocument.builder()
                .documentUrl("https://example.com/123")
                .documentFilename("Form-C.pdf")
                .documentBinaryUrl("https://example.com/binary")
                .build())
            .build());


        assertEquals(1, result.size());
        assertEquals("https://example.com/123", result.get(0).getValue().getCaseDocument().getDocumentUrl());
        assertEquals("Form-C.pdf", result.get(0).getValue().getCaseDocument().getDocumentFilename());
        assertEquals("https://example.com/binary", result.get(0).getValue().getCaseDocument().getDocumentBinaryUrl());
        assertEquals("123", result.get(0).getValue().getDocumentId());
    }

    @Test
    void testGetCaseDocumentsList_withDuplicateDocument() throws Exception {

        List<DocumentToKeepCollection> result = documentRemovalService.getCaseDocumentsList(FinremCaseData.builder()
            .ccdCaseId(TestConstants.CASE_ID)
            .miniFormA(CaseDocument.builder()
                .documentUrl("https://example.com/123")
                .documentFilename("Form-C.pdf")
                .documentBinaryUrl("https://example.com/binary")
                .build())
            .divorceUploadEvidence1(CaseDocument.builder()
                .documentUrl("https://example.com/123")
                .documentFilename("Form-C.pdf")
                .documentBinaryUrl("https://example.com/binary")
                .build())
            .build());


        assertEquals(1, result.size());
        assertEquals("https://example.com/123", result.get(0).getValue().getCaseDocument().getDocumentUrl());
        assertEquals("Form-C.pdf", result.get(0).getValue().getCaseDocument().getDocumentFilename());
        assertEquals("https://example.com/binary", result.get(0).getValue().getCaseDocument().getDocumentBinaryUrl());
        assertEquals("123", result.get(0).getValue().getDocumentId());
    }


    @Test
    void testGetCaseDocumentsList_NestedObjectWithinArrayWithDocumentUrl() throws Exception {
        List<DocumentToKeepCollection> result = documentRemovalService.getCaseDocumentsList(FinremCaseData.builder()
            .ccdCaseId(TestConstants.CASE_ID)
            .uploadDocuments(List.of(UploadDocumentCollection.builder()
                .value(UploadDocument.builder()
                    .documentLink(CaseDocument.builder()
                        .documentUrl("https://example1.com/123")
                        .documentFilename("Form-C.pdf")
                        .documentBinaryUrl("https://example1.com/binary")
                        .build()).build()).build(),
                UploadDocumentCollection.builder().value(UploadDocument.builder()
                    .documentLink(CaseDocument.builder()
                        .documentUrl("https://example2.com/456")
                        .documentFilename("Form-D.pdf")
                        .documentBinaryUrl("https://example2.com/binary")
                        .build()).build()).build()))
            .build());

        assertEquals(2, result.size());

        assertEquals("https://example1.com/123", result.get(0).getValue().getCaseDocument().getDocumentUrl());
        assertEquals("Form-C.pdf", result.get(0).getValue().getCaseDocument().getDocumentFilename());
        assertEquals("https://example1.com/binary", result.get(0).getValue().getCaseDocument().getDocumentBinaryUrl());
        assertEquals("123", result.get(0).getValue().getDocumentId());

        assertEquals("https://example2.com/456", result.get(1).getValue().getCaseDocument().getDocumentUrl());
        assertEquals("Form-D.pdf", result.get(1).getValue().getCaseDocument().getDocumentFilename());
        assertEquals("https://example2.com/binary", result.get(1).getValue().getCaseDocument().getDocumentBinaryUrl());
        assertEquals("456", result.get(1).getValue().getDocumentId());
    }

    @Test
    void testGetCaseDocumentsList_ComplexNestedArrayStructure() throws Exception {

        List<DocumentToKeepCollection> result = documentRemovalService.getCaseDocumentsList(FinremCaseData.builder()
            .ccdCaseId(TestConstants.CASE_ID)
            .orderWrapper(OrderWrapper.builder()
                .appOrderCollection(List.of(
                    ApprovedOrderCollection.builder()
                        .value(ApproveOrder.builder()
                            .caseDocument(CaseDocument.builder()
                                .documentUrl("https://example1.com/123")
                                .documentFilename("Form-A.pdf")
                                .documentBinaryUrl("https://example1.com/binary")
                                .build())
                            .build())
                        .build(),
                    ApprovedOrderCollection.builder()
                        .value(ApproveOrder.builder()
                            .caseDocument(CaseDocument.builder()
                                .documentUrl("https://example2.com/456")
                                .documentFilename("Form-B.pdf")
                                .documentBinaryUrl("https://example2.com/binary")
                                .build())
                            .build())
                        .build(),
                    ApprovedOrderCollection.builder()
                        .value(ApproveOrder.builder()
                            .caseDocument(CaseDocument.builder()
                                .documentUrl("https://example3.com/789")
                                .documentFilename("Form-C.pdf")
                                .documentBinaryUrl("https://example3.com/binary")
                                .build())
                            .build())
                        .build()))
                .build())
            .build());



        assertEquals(3, result.size());

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

    }

    //TODO: Currently when mapping back allocatedRegionWrapper is not mapped
    // back when passed in as null (note shouldn't be an issue as maps back to case data when no null )
    @Test
    void testRemoveDocuments_NoDocuments() {
        FinremCaseData caseData = FinremCaseData.builder()
            .contactDetailsWrapper(ContactDetailsWrapper.builder()
                .applicantLname("Some Name")
                .build())
            .build();
        FinremCaseData result = documentRemovalService.removeDocuments(caseData, 1L, "Auth");

        assertEquals(caseData.getContactDetailsWrapper().getApplicantLname(),result.getContactDetailsWrapper().getApplicantLname());
        assertNull(result.getDocumentToKeepCollection());
    }

    @Test
    void testRemoveDocuments_KeepAllDocuments() {
        FinremCaseData caseData = FinremCaseData.builder()
            .ccdCaseId(TestConstants.CASE_ID)
            .uploadDocuments(List.of(UploadDocumentCollection.builder()
                    .value(UploadDocument.builder()
                        .documentLink(CaseDocument.builder()
                            .documentUrl("https://example1.com/123")
                            .documentFilename("Form-C.pdf")
                            .documentBinaryUrl("https://example1.com/binary")
                            .build())
                        .build())
                    .build(),
                UploadDocumentCollection.builder().value(UploadDocument.builder()
                    .documentLink(CaseDocument.builder()
                        .documentUrl("https://example2.com/456")
                        .documentFilename("Form-D.pdf")
                        .documentBinaryUrl("https://example2.com/binary")
                        .build())
                    .build())
                    .build()))
            .documentToKeepCollection(List.of(DocumentToKeepCollection.builder()
                .value(DocumentToKeep.builder()
                    .documentId("123")
                    .caseDocument(CaseDocument.builder()
                        .documentUrl("https://example1.com/123")
                        .documentFilename("Form-C.pdf")
                        .documentBinaryUrl("https://example1.com/binary")
                        .build())
                    .build())
                .build(),
                DocumentToKeepCollection.builder()
                .value(DocumentToKeep.builder()
                    .documentId("456")
                    .caseDocument(CaseDocument.builder()
                        .documentUrl("https://example2.com/456")
                        .documentFilename("Form-D.pdf")
                        .documentBinaryUrl("https://example2.com/binary")
                        .build())
                    .build())
                .build()))
            .build();

        FinremCaseData result = documentRemovalService.removeDocuments(caseData, 1L, "Auth");

        assertEquals(2, result.getUploadDocuments().size());
        assertNull(result.getDocumentToKeepCollection());
    }
    
    @Test
    void testRemoveDocuments_RemoveDoc() {
        FinremCaseData caseData = FinremCaseData.builder()
            .ccdCaseId(TestConstants.CASE_ID)
            .uploadDocuments(List.of(UploadDocumentCollection.builder()
                    .value(UploadDocument.builder()
                        .documentLink(CaseDocument.builder()
                            .documentUrl("https://example1.com/123")
                            .documentFilename("Form-C.pdf")
                            .documentBinaryUrl("https://example1.com/binary")
                            .build())
                        .build())
                    .build(),
                UploadDocumentCollection.builder().value(UploadDocument.builder()
                        .documentLink(CaseDocument.builder()
                            .documentUrl("https://example2.com/456")
                            .documentFilename("Form-D.pdf")
                            .documentBinaryUrl("https://example2.com/binary")
                            .build())
                        .build())
                    .build()))
            .documentToKeepCollection(List.of(DocumentToKeepCollection.builder()
                    .value(DocumentToKeep.builder()
                        .documentId("123")
                        .caseDocument(CaseDocument.builder()
                            .documentUrl("https://example1.com/123")
                            .documentFilename("Form-C.pdf")
                            .documentBinaryUrl("https://example1.com/binary")
                            .build())
                        .build())
                    .build()))
            .build();

        FinremCaseData result = documentRemovalService.removeDocuments(caseData, 1L, "Auth");

        assertEquals(1, result.getUploadDocuments().size());
        assertEquals("Form-C.pdf", result.getUploadDocuments().get(0).getValue().getDocumentLink().getDocumentFilename());
        assertNull(result.getDocumentToKeepCollection());
    }

    @Test
    void testBuildCaseDocumentList() {
        // Todo, spotted that this test is missing.  Added this failing placeholder
        // Needs a test with a given JSON Node containing the docs
        // Then test everything is built as expected
        // split the sorting out if that makes more sense.
        // Assert that getUploadTimestampFromDocumentNode called.
        assertEquals(true,false);
    }

    @Test
    void test_getUploadTimestampFromDocumentNode_returnsDate() throws IOException {
        String testData = "{" +
                "\"document_url\": \"a url\"," +
                "\"document_filename\": \"a filename\"," +
                "\"document_binary_url\": \"a binary url\"," +
                "\"upload_timestamp\": \"2024-08-20T07:20:43.416964\"" +
                "}";
        JsonNode documentNode = objectMapperForTest.readTree(testData);
        LocalDateTime testTimestamp = documentRemovalService.getUploadTimestampFromDocumentNode(documentNode);
        assertEquals("2024-08-20T07:20:43.416964", testTimestamp.toString());
    }

    @Test
    void test_getUploadTimestampFromDocumentNode_handlesNullfromSource() throws IOException {
        String testData = "{" +
                "\"document_url\": \"a url\"," +
                "\"document_filename\": \"a filename\"," +
                "\"document_binary_url\": \"a binary url\"" +
                "}";
        JsonNode documentNode = objectMapperForTest.readTree(testData);
        LocalDateTime testTimestamp = documentRemovalService.getUploadTimestampFromDocumentNode(documentNode);
        assertNull(testTimestamp);
    }

    @Test
    void test_getUploadTimestampFromDocumentNode_givesNullWithInvalidDate() throws IOException {
        String testData = "{" +
                "\"document_url\": \"a url\"," +
                "\"document_filename\": \"a filename\"," +
                "\"document_binary_url\": \"a binary url\"," +
                "\"upload_timestamp\": \"an invalid date\"" +
                "}";
        JsonNode documentNode = objectMapperForTest.readTree(testData);
        LocalDateTime testTimestamp = documentRemovalService.getUploadTimestampFromDocumentNode(documentNode);
        assertNull(testTimestamp);
    }
}
