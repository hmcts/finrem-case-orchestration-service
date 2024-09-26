package uk.gov.hmcts.reform.finrem.caseorchestration.service.documentremoval;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ApproveOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ApproveOrdersHolder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ApprovedOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ApprovedOrderConsolidateCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentToKeep;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentToKeepCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.OrderWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;


@ExtendWith(MockitoExtension.class)
class DocumentRemovalServiceTest {

    private DocumentRemovalService documentRemovalService;

    private ObjectMapper objectMapper;

    @Mock
    private GenericDocumentService genericDocumentService;

    @Mock
    private FeatureToggleService featureToggleService;

    @BeforeEach
    public void setUp() {
        objectMapper = JsonMapper
                .builder()
                .addModule(new JavaTimeModule())
                .addModule(new ParameterNamesModule(JsonCreator.Mode.PROPERTIES))
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .build();

        documentRemovalService = new DocumentRemovalService(objectMapper, genericDocumentService, featureToggleService);
    }

    @Test
    void testGetCaseDocumentsList_EmptyObject() {
        List<DocumentToKeepCollection> result = documentRemovalService.getCaseDocumentsList(FinremCaseData.builder()
            .ccdCaseId(TestConstants.CASE_ID)
            .build());
        assertEquals(0, result.size());
    }

    @Test
    void testGetCaseDocumentsList_testEmptyArray() {

        List<DocumentToKeepCollection> result = documentRemovalService.getCaseDocumentsList(FinremCaseData.builder()
            .ccdCaseId(TestConstants.CASE_ID)
            .uploadDocuments(new ArrayList<>())
            .build());
        assertEquals(0, result.size());
    }

    @Test
    void testGetCaseDocumentsList_RootDocument() {

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
    void testGetCaseDocumentsList_withDuplicateDocument() {

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
    void testGetCaseDocumentsList_NestedObjectWithinArrayWithDocumentUrl() {
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
    void testGetCaseDocumentsList_ComplexNestedArrayStructure() {

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

    /**
     * Sorts files by upload timestamp in reverse ascending order, nulls last.
     * The data for this test includes the various file structures.
     */
    @Test
    void testGetCaseDocumentsList_SortingIsCorrect() {

        String firstDate = "2024-01-01T00:00:00.000000";
        String secondDate = "2024-01-02T00:00:00.000000";
        String thirdDate = "2024-01-03T00:00:00.000000";
        String fourthDate = "2024-01-04T00:00:00.000000";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");

        List<DocumentToKeepCollection> result = documentRemovalService.getCaseDocumentsList(FinremCaseData.builder()
                // ApprovedOrderCollection as example of complex nested data.  Has third-oldest upload date
                .ccdCaseId(TestConstants.CASE_ID)
                .orderWrapper(OrderWrapper.builder()
                    .appOrderCollection(List.of(
                        ApprovedOrderCollection.builder()
                            .value(ApproveOrder.builder()
                                .caseDocument(CaseDocument.builder()
                                    .documentUrl("https://example3.com/789")
                                    .documentFilename("ThirdDoc.pdf")
                                    .documentBinaryUrl("https://example3.com/binary")
                                    .uploadTimestamp(LocalDateTime.parse(thirdDate, formatter))
                                    .build())
                                .build())
                            .build()))
                        .build())
                // uploaded documents collection as example of data at in an array.  Has first, fourth and a null upload date.
                .uploadDocuments(List.of(UploadDocumentCollection.builder()
                                .value(UploadDocument.builder()
                                        .documentLink(CaseDocument.builder()
                                                .documentUrl("https://example1.com/123")
                                                .documentFilename("FirstDoc.pdf")
                                                .documentBinaryUrl("https://example1.com/binary")
                                                .uploadTimestamp(LocalDateTime.parse(firstDate, formatter))
                                                .build()).build()).build(),
                        UploadDocumentCollection.builder().value(UploadDocument.builder()
                                .documentLink(CaseDocument.builder()
                                        .documentUrl("https://example4.com/101112")
                                        .documentFilename("FourthDoc.pdf")
                                        .documentBinaryUrl("https://example4.com/binary")
                                        .uploadTimestamp(LocalDateTime.parse(fourthDate, formatter))
                                        .build()).build()).build(),
                        UploadDocumentCollection.builder().value(UploadDocument.builder()
                                .documentLink(CaseDocument.builder()
                                        .documentUrl("https://example5.com/131415")
                                        .documentFilename("NullDoc.pdf")
                                        .documentBinaryUrl("https://example5.com/binary")
                                        .build()).build()).build()))
                // miniFormA as example of data at a root level.  Has second-oldest upload date
                .miniFormA(CaseDocument.builder()
                        .documentUrl("https://example2.com/456")
                        .documentFilename("secondDoc.pdf")
                        .documentBinaryUrl("https://example2.com/binary")
                        .uploadTimestamp(LocalDateTime.parse(secondDate, formatter))
                        .build())
                .build());

        assertEquals(5, result.size());

        assertEquals("https://example4.com/101112", result.get(0).getValue().getCaseDocument().getDocumentUrl());
        assertEquals("FourthDoc.pdf", result.get(0).getValue().getCaseDocument().getDocumentFilename());

        assertEquals("https://example3.com/789", result.get(1).getValue().getCaseDocument().getDocumentUrl());
        assertEquals("ThirdDoc.pdf", result.get(1).getValue().getCaseDocument().getDocumentFilename());

        assertEquals("https://example2.com/456", result.get(2).getValue().getCaseDocument().getDocumentUrl());
        assertEquals("secondDoc.pdf", result.get(2).getValue().getCaseDocument().getDocumentFilename());

        assertEquals("https://example1.com/123", result.get(3).getValue().getCaseDocument().getDocumentUrl());
        assertEquals("FirstDoc.pdf", result.get(3).getValue().getCaseDocument().getDocumentFilename());

        assertEquals("https://example5.com/131415", result.get(4).getValue().getCaseDocument().getDocumentUrl());
        assertEquals("NullDoc.pdf", result.get(4).getValue().getCaseDocument().getDocumentFilename());
    }

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
    void testRemoveDocuments_TopLevelDoc() {
        FinremCaseData caseData = FinremCaseData.builder()
            .ccdCaseId(TestConstants.CASE_ID)
            .d11(CaseDocument.builder()
                .documentUrl("https://example1.com/123")
                .documentFilename("Approved Order Doc.pdf")
                .documentBinaryUrl("https://example1.com/binary")
                .build())
            .divorceUploadEvidence1(CaseDocument.builder()
                .documentUrl("https://example2.com/456")
                .documentFilename("Additional Hearing Doc.pdf")
                .documentBinaryUrl("https://example2.com/binary")
                .build())
            .documentToKeepCollection(List.of(DocumentToKeepCollection.builder()
                .value(DocumentToKeep.builder()
                    .documentId("456")
                    .caseDocument(CaseDocument.builder()
                        .documentUrl("https://example2.com/456")
                        .documentFilename("Additional Hearing Doc.pdf")
                        .documentBinaryUrl("https://example2.com/binary")
                        .build())
                    .build())
                .build()))
            .build();

        FinremCaseData result = documentRemovalService.removeDocuments(caseData, 1L, "Auth");

        assertNull(result.getD11());
        assertNotNull(result.getDivorceUploadEvidence1());
        assertEquals("Additional Hearing Doc.pdf", result.getDivorceUploadEvidence1().getDocumentFilename());
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
    void testRemoveDocuments_RemoveDocFromArray() {
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
    void testRemoveDocuments_NestedObjectInArray() {
        FinremCaseData caseData = FinremCaseData.builder()
            .ccdCaseId(TestConstants.CASE_ID)
            .hearingNoticeDocumentPack(List.of(DocumentCollection.builder()
                    .value(CaseDocument.builder()
                        .documentUrl("https://example1.com/123")
                        .documentFilename("Approved Order1.pdf")
                        .documentBinaryUrl("https://example1.com/binary")
                        .build())
                .build(),
                DocumentCollection.builder()
                    .value(CaseDocument.builder()
                        .documentUrl("https://example2.com/456")
                        .documentFilename("Additional Hearing Doc.pdf")
                        .documentBinaryUrl("https://example2.com/binary")
                        .build())
                .build()
            ))
            .documentToKeepCollection(List.of(DocumentToKeepCollection.builder()
                .value(DocumentToKeep.builder()
                    .documentId("456")
                    .caseDocument(CaseDocument.builder()
                        .documentUrl("https://example2.com/456")
                        .documentFilename("Additional Hearing Doc.pdf")
                        .documentBinaryUrl("https://example2.com/binary")
                        .build())
                    .build())
                .build()))
            .build();

        FinremCaseData result = documentRemovalService.removeDocuments(caseData, 1L, "Auth");

        assertEquals(1, result.getHearingNoticeDocumentPack().size());
        assertEquals("Additional Hearing Doc.pdf", result.getHearingNoticeDocumentPack().get(0).getValue().getDocumentFilename());
        assertNull(result.getDocumentToKeepCollection());
    }

    @Test
    void testRemoveDocuments_NestedObjectInNestedArray() {
        FinremCaseData caseData = FinremCaseData.builder()
            .ccdCaseId(TestConstants.CASE_ID)
            .orderWrapper(OrderWrapper.builder()
                .appOrderCollections(List.of(ApprovedOrderConsolidateCollection.builder()
                        .value(ApproveOrdersHolder.builder()
                            .approveOrders(List.of(
                                ApprovedOrderCollection.builder()
                                    .value(ApproveOrder.builder()
                                        .caseDocument(CaseDocument.builder()
                                            .documentUrl("https://example1.com/123")
                                            .documentFilename("Approved Order Doc.pdf")
                                            .documentBinaryUrl("https://example1.com/binary")
                                            .build())
                                        .build())
                                    .build(),
                                ApprovedOrderCollection.builder()
                                    .value(ApproveOrder.builder()
                                        .caseDocument(CaseDocument.builder()
                                            .documentUrl("https://example2.com/456")
                                            .documentFilename("Additional Hearing Doc.pdf")
                                            .documentBinaryUrl("https://example2.com/binary")
                                            .build())
                                        .build())
                                    .build()))
                            .build())
                    .build()))
                .build())
            .documentToKeepCollection(List.of(DocumentToKeepCollection.builder()
                .value(DocumentToKeep.builder()
                    .documentId("456")
                    .caseDocument(CaseDocument.builder()
                        .documentUrl("https://example2.com/456")
                        .documentFilename("Additional Hearing Doc.pdf")
                        .documentBinaryUrl("https://example2.com/binary")
                        .build())
                    .build())
                .build()))
            .build();

        FinremCaseData result = documentRemovalService.removeDocuments(caseData, 1L, "Auth");

        assertEquals(1, result.getOrderWrapper().getAppOrderCollections().get(0).getValue().getApproveOrders().size());
        assertEquals("Additional Hearing Doc.pdf", result.getOrderWrapper().getAppOrderCollections()
            .get(0).getValue().getApproveOrders().get(0)
            .getValue().getCaseDocument().getDocumentFilename());
        assertNull(result.getDocumentToKeepCollection());
    }
}
