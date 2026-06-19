package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NewUploadedDocumentsService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentchecker.DocumentCheckerService;

import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class UploadDocumentConsentedAboutToSubmitHandlerTest {

    @Mock
    private DocumentCheckerService documentCheckerService;

    @Mock
    private NewUploadedDocumentsService newUploadedDocumentsService;

    @InjectMocks
    private UploadDocumentConsentedAboutToSubmitHandler underTest;

    @Test
    void testCanHandle() {
        assertCanHandle(underTest, CallbackType.ABOUT_TO_SUBMIT, CaseType.CONSENTED, EventType.UPLOAD_DOCUMENT_CONSENTED);
    }

//    @Test
//    void givenValidCaseDataWithoutDocumentUploaded_thenNoWarningShouldBeReturned() {
//        when(newUploadedDocumentsService.getNewUploadDocuments(any(), any(), any())).thenReturn(List.of());
//        when(documentCheckerService.getWarnings(any(), any(), any(), any())).thenReturn(List.of("whatever")); // never reach
//
//        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = underTest.handle(
//            FinremCallbackRequest.builder()
//                .caseDetails(FinremCaseDetailsBuilderFactory.from(Long.valueOf(CASE_ID), CaseType.CONSENTED).build())
//                .caseDetailsBefore(FinremCaseDetailsBuilderFactory.from(Long.valueOf(CASE_ID), CaseType.CONSENTED).build()).build(),
//            AUTH_TOKEN);
//        assertThat(response.getWarnings()).isEmpty();
//    }
//
//    @Test
//    void givenValidCaseDataWithDocumentUpload_whenUnexpectedExceptionThrownInWarningChecking_thenNotBlockTheSubmission() {
//        when(newUploadedDocumentsService.getNewUploadDocuments(any(), any(), any())).thenReturn(List.of(caseDocument()));
//        when(documentCheckerService.getWarnings(any(), any(), any(), any())).thenThrow(new RuntimeException("unexpected exception"));
//
//        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = underTest.handle(
//            FinremCallbackRequest.builder()
//                .caseDetails(FinremCaseDetailsBuilderFactory.from(Long.valueOf(CASE_ID), CaseType.CONSENTED).build())
//                .caseDetailsBefore(FinremCaseDetailsBuilderFactory.from(Long.valueOf(CASE_ID), CaseType.CONSENTED).build()).build(),
//            AUTH_TOKEN);
//        assertThat(response.getWarnings()).isEmpty();
//    }
//
//    @ParameterizedTest
//    @ValueSource(booleans = {true, false})
//    void givenValidCaseData_whenWarningIsDetected_thenPopulateWarning(boolean hasWarnings) {
//        List<String> expectedWarnings = hasWarnings ? List.of("warnings") : List.of();
//        when(newUploadedDocumentsService.getNewUploadDocuments(any(), any(), any())).thenReturn(List.of(caseDocument()));
//        when(documentCheckerService.getWarnings(any(), any(), any(), any())).thenReturn(expectedWarnings);
//
//        FinremCaseDetails finremCaseDetails = FinremCaseDetailsBuilderFactory.from(Long.valueOf(CASE_ID), CaseType.CONSENTED, FinremCaseData.builder()
//            .uploadDocuments(List.of(
//                createUploadDocumentCollection(
//                    UploadDocumentType.APPLICATION, "New email content",
//                    caseDocument("/fileUrl", "/binaryUrl","document.extension"), LocalDate.now(), "New Example",
//                    "newDocument.filename")
//            )))
//            .build();
//
//        FinremCaseDetails finremCaseDetailsBefore = FinremCaseDetailsBuilderFactory.from(Long.valueOf(CASE_ID), CaseType.CONTESTED,
//            FinremCaseData.builder()).build();
//
//        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = underTest.handle(
//            FinremCallbackRequest.builder()
//                .caseDetails(finremCaseDetails)
//                .caseDetailsBefore(finremCaseDetailsBefore).build(),
//            AUTH_TOKEN);
//        assertThat(response.getWarnings()).isEqualTo(expectedWarnings);
//        if (hasWarnings) {
//            assertThat(logs.getInfos()).containsExactly(format(
//                "%s - Number of warnings encountered when uploading document: %s", CASE_ID, 1));
//        } else {
//            assertThat(logs.getInfos()).isEmpty();
//        }
//    }
//
//    @ParameterizedTest
//    @ValueSource(booleans = {true, false})
//    void givenValidCaseData_whenWarningsAreDetected_thenPopulateSortedWarnings(boolean hasWarnings) {
//        when(newUploadedDocumentsService.getNewUploadDocuments(any(), any(), any())).thenReturn(List.of(caseDocument()));
//        when(documentCheckerService.getWarnings(any(), any(), any(), any())).thenReturn(hasWarnings ? List.of("2warnings", "1warnings", "1warnings",
//            "abc", "Aae") : List.of());
//
//        FinremCaseDetails finremCaseDetails = FinremCaseDetailsBuilderFactory.from(Long.valueOf(CASE_ID), CaseType.CONSENTED, FinremCaseData.builder()
//                .uploadDocuments(List.of(
//                    createUploadDocumentCollection(
//                        UploadDocumentType.APPLICATION, "New email content",
//                        caseDocument("/fileUrl", "/binaryUrl","document.extension"), LocalDate.now(), "New Example",
//                        "newDocument.filename")
//                )))
//            .build();
//
//        FinremCaseDetails finremCaseDetailsBefore = FinremCaseDetailsBuilderFactory.from(Long.valueOf(CASE_ID), CaseType.CONTESTED,
//            FinremCaseData.builder()).build();
//
//        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = underTest.handle(
//            FinremCallbackRequest.builder()
//                .caseDetails(finremCaseDetails)
//                .caseDetailsBefore(finremCaseDetailsBefore).build(),
//            AUTH_TOKEN);
//        assertThat(response.getWarnings()).isEqualTo(hasWarnings ? List.of("1warnings", "2warnings", "Aae", "abc") : List.of());
//        if (hasWarnings) {
//            assertThat(logs.getInfos()).containsExactly(format(
//                "%s - Number of warnings encountered when uploading document: %s", CASE_ID, 4));
//        } else {
//            assertThat(logs.getInfos()).isEmpty();
//        }
//    }
//
//    private UploadDocumentCollection createUploadDocumentCollection(UploadDocumentType type, String emailContent,
//                                                                    CaseDocument link, LocalDate dateAdded, String comment,
//                                                                    String fileName) {
//        return UploadDocumentCollection.builder()
//            .value(UploadDocument
//                .builder()
//                .documentType(type)
//                .documentEmailContent(emailContent)
//                .documentLink(link)
//                .documentDateAdded(dateAdded)
//                .documentComment(comment)
//                .documentFileName(fileName)
//                .build())
//            .build();
//    }
}
