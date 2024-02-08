package uk.gov.hmcts.reform.finrem.caseorchestration.handler.scanneddocs;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ScannedDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ScannedDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocumentCollection;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType.MID_EVENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.MANAGE_SCANNED_DOCS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.UPLOAD_CASE_FILES;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;

class ManageScannedDocsContestedMidEventHandlerTest {

    private ManagedScannedDocsContestedMidEventHandler handler;

    @BeforeEach
    void setup() {
        handler = new ManagedScannedDocsContestedMidEventHandler(Mockito.mock(FinremCaseDetailsMapper.class));
    }

    @ParameterizedTest
    @MethodSource
    void testCanHandle(CallbackType callbackType, CaseType caseType, EventType eventType, boolean expected) {
        assertThat(handler.canHandle(callbackType, caseType, eventType)).isEqualTo(expected);
    }

    private static Stream<Arguments> testCanHandle() {
        return Stream.of(
            Arguments.of(ABOUT_TO_START, CONTESTED, MANAGE_SCANNED_DOCS, false),
            Arguments.of(MID_EVENT, CONTESTED, MANAGE_SCANNED_DOCS, true),
            Arguments.of(ABOUT_TO_SUBMIT, CONTESTED, MANAGE_SCANNED_DOCS, false),
            Arguments.of(SUBMITTED, CONTESTED, MANAGE_SCANNED_DOCS, false),
            Arguments.of(MID_EVENT, CONSENTED, MANAGE_SCANNED_DOCS, false),
            Arguments.of(MID_EVENT, CONTESTED, UPLOAD_CASE_FILES, false)
        );
    }

    @Test
    void givenNoScannedDocumentsSelected_whenHandleCalled_thenReturnsError() {
        DynamicMultiSelectList options = DynamicMultiSelectList.builder()
            .listItems(List.of(
                DynamicMultiSelectListElement.builder().code("104").label("option 1").build(),
                DynamicMultiSelectListElement.builder().code("953").label("option 2").build(),
                DynamicMultiSelectListElement.builder().code("6337").label("option 3").build()
            ))
            .build();
        FinremCaseData caseData = FinremCaseData.builder()
            .scannedDocsToUpdate(options)
            .build();
        FinremCallbackRequest request = FinremCallbackRequestFactory.from(caseData);

        var response = handler.handle(request, "some-token");

        List<String> errors = response.getErrors();
        assertThat(errors.size()).isEqualTo(1);
        assertThat(errors.get(0)).isEqualTo("No scanned document has been selected");
    }

    @Test
    void givenScannedDocumentsSelected_whenHandleCalled_thenCreatesManageScannedDocumentsCollection() {
        FinremCaseData caseData = FinremCaseData.builder()
            .scannedDocuments(createScannedDocuments())
            .scannedDocsToUpdate(createScannedDocumentsToUpdateOptions())
            .build();
        FinremCallbackRequest request = FinremCallbackRequestFactory.from(caseData);

        var response = handler.handle(request, "some-token");

        assertThat(response.getErrors().isEmpty()).isTrue();
        List<UploadCaseDocumentCollection> collection = response.getData().getManageScannedDocumentCollection();
        assertThat(collection.size()).isEqualTo(2);

        UploadCaseDocument uploadCaseDocument = collection.get(0).getUploadCaseDocument();
        assertThat(uploadCaseDocument.getFileName()).isEqualTo("file1.pdf");
        assertThat(uploadCaseDocument.getScannedDate()).isEqualTo(LocalDateTime.of(2024,1,1,1,10));
        assertThat(uploadCaseDocument.getCaseDocuments().getDocumentUrl()).isEqualTo("file1Url");
        assertThat(uploadCaseDocument.getCaseDocuments().getDocumentBinaryUrl()).isEqualTo("file1BinaryUrl");
        assertThat(uploadCaseDocument.getCaseDocuments().getDocumentFilename()).isEqualTo("file1Scanned.pdf");

        uploadCaseDocument = collection.get(1).getUploadCaseDocument();
        assertThat(uploadCaseDocument.getFileName()).isEqualTo("file3.pdf");
        assertThat(uploadCaseDocument.getScannedDate()).isEqualTo(LocalDateTime.of(2024,3,3,3,30));
        assertThat(uploadCaseDocument.getCaseDocuments().getDocumentUrl()).isEqualTo("file3Url");
        assertThat(uploadCaseDocument.getCaseDocuments().getDocumentBinaryUrl()).isEqualTo("file3BinaryUrl");
        assertThat(uploadCaseDocument.getCaseDocuments().getDocumentFilename()).isEqualTo("file3Scanned.pdf");
    }

    private DynamicMultiSelectList createScannedDocumentsToUpdateOptions() {
        return DynamicMultiSelectList.builder()
            .listItems(List.of(
                DynamicMultiSelectListElement.builder().code("104").label("option 1").build(),
                DynamicMultiSelectListElement.builder().code("953").label("option 2").build(),
                DynamicMultiSelectListElement.builder().code("6337").label("option 3").build()
            ))
            .value(List.of(
                DynamicMultiSelectListElement.builder().code("104").label("option 1").build(),
                DynamicMultiSelectListElement.builder().code("6337").label("option 3").build()
            ))
            .build();
    }

    private List<ScannedDocumentCollection> createScannedDocuments() {
        return List.of(
            createScannedDocumentCollection("104", LocalDateTime.of(2024,1,1,1,10), "file1.pdf",
                createCaseDocument("file1Url", "file1BinaryUrl", "file1Scanned.pdf")),
            createScannedDocumentCollection("953", LocalDateTime.of(2024,2,2,2,20), "file2.pdf",
                createCaseDocument("file2Url", "file2BinaryUrl", "file2Scanned.pdf")),
            createScannedDocumentCollection("6337", LocalDateTime.of(2024,3,3,3,30), "file3.pdf",
                createCaseDocument("file3Url", "file3BinaryUrl", "file3Scanned.pdf"))
        );
    }

    private CaseDocument createCaseDocument(String documentUrl, String documentBinaryUrl, String documentFilename) {
        return CaseDocument.builder()
            .documentUrl(documentUrl)
            .documentBinaryUrl(documentBinaryUrl)
            .documentFilename(documentFilename)
            .build();
    }

    private ScannedDocumentCollection createScannedDocumentCollection(String id, LocalDateTime scannedDate,
                                                                      String filename, CaseDocument caseDocument) {
        return ScannedDocumentCollection.builder()
            .id(id)
            .value(ScannedDocument.builder()
                .scannedDate(scannedDate)
                .fileName(filename)
                .url(caseDocument)
                .build())
            .build();
    }
}
