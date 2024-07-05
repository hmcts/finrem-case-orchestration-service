package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCaseDetailsBuilderFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NewUploadedDocumentsService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentchecker.DocumentCheckerService;
import uk.gov.hmcts.reform.finrem.caseorchestration.util.TestLogger;
import uk.gov.hmcts.reform.finrem.caseorchestration.util.TestLogs;

import java.time.LocalDate;
import java.util.List;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(SpringExtension.class)
class UploadDocumentConsentedAboutToSubmitHandlerTest {

    @TestLogs
    private final TestLogger logs = new TestLogger(UploadDocumentConsentedAboutToSubmitHandler.class);

    @Mock
    private DocumentCheckerService documentCheckerService;

    @Mock
    private NewUploadedDocumentsService newUploadedDocumentsService;

    private UploadDocumentConsentedAboutToSubmitHandler underTest;

    @BeforeEach
    public void setUpTest() {
        FinremCaseDetailsMapper finremCaseDetailsMapper = new FinremCaseDetailsMapper(new ObjectMapper().registerModule(new JavaTimeModule()));
        underTest = new UploadDocumentConsentedAboutToSubmitHandler(finremCaseDetailsMapper, documentCheckerService, newUploadedDocumentsService);
    }

    @Test
    void givenACcdCallbackContestedCase_WhenAnAboutToSubmitEventUploadDocumentConsented_thenHandlerCanHandle() {
        assertCanHandle(underTest, CallbackType.ABOUT_TO_SUBMIT, CaseType.CONSENTED, EventType.UPLOAD_DOCUMENT_CONSENTED);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void givenValidCaseData_whenWarningIsDetected_thenPopulateWarning(boolean hasWarnings) {
        List<String> expectedWarnings = hasWarnings ? List.of("warnings") : List.of();
        when(newUploadedDocumentsService.getNewUploadDocuments(any(), any(), any())).thenReturn(List.of(
            UploadDocumentCollection.builder()
                .value(UploadDocument.builder().build())
                .build()
        ));
        when(documentCheckerService.getWarnings(any(), any(), any(), any())).thenReturn(expectedWarnings);

        FinremCaseDetails finremCaseDetails = FinremCaseDetailsBuilderFactory.from(Long.valueOf(CASE_ID), CaseType.CONSENTED, FinremCaseData.builder()
            .uploadDocuments(List.of(
                createUploadDocumentCollection(
                    UploadDocumentType.APPLICATION, "New email content",
                    caseDocument("/fileUrl", "/binaryUrl","document.extension"), LocalDate.now(), "New Example",
                    "newDocument.filename")
            )))
            .build();

        FinremCaseDetails finremCaseDetailsBefore = FinremCaseDetailsBuilderFactory.from(Long.valueOf(CASE_ID), CaseType.CONTESTED,
            FinremCaseData.builder()).build();

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = underTest.handle(
            FinremCallbackRequest.builder()
                .caseDetails(finremCaseDetails)
                .caseDetailsBefore(finremCaseDetailsBefore).build(),
            AUTH_TOKEN);
        assertThat(response.getWarnings()).isEqualTo(expectedWarnings);
        if (hasWarnings) {
            assertThat(logs.getInfos()).containsExactly(format(
                "Number of warnings encountered when uploading document for a case %s: %s", CASE_ID, 1));
        } else {
            assertThat(logs.getInfos()).isEmpty();
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void givenValidCaseData_whenWarningsAreDetected_thenPopulateSortedWarnings(boolean hasWarnings) {
        when(newUploadedDocumentsService.getNewUploadDocuments(any(), any(), any())).thenReturn(List.of(
            UploadDocumentCollection.builder()
                .value(UploadDocument.builder().build())
                .build()
        ));
        when(documentCheckerService.getWarnings(any(), any(), any(), any())).thenReturn(hasWarnings ? List.of("2warnings", "1warnings", "1warnings",
            "abc", "Aae") : List.of());

        FinremCaseDetails finremCaseDetails = FinremCaseDetailsBuilderFactory.from(Long.valueOf(CASE_ID), CaseType.CONSENTED, FinremCaseData.builder()
                .uploadDocuments(List.of(
                    createUploadDocumentCollection(
                        UploadDocumentType.APPLICATION, "New email content",
                        caseDocument("/fileUrl", "/binaryUrl","document.extension"), LocalDate.now(), "New Example",
                        "newDocument.filename")
                )))
            .build();

        FinremCaseDetails finremCaseDetailsBefore = FinremCaseDetailsBuilderFactory.from(Long.valueOf(CASE_ID), CaseType.CONTESTED,
            FinremCaseData.builder()).build();

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = underTest.handle(
            FinremCallbackRequest.builder()
                .caseDetails(finremCaseDetails)
                .caseDetailsBefore(finremCaseDetailsBefore).build(),
            AUTH_TOKEN);
        assertThat(response.getWarnings()).isEqualTo(hasWarnings ? List.of("1warnings", "2warnings", "Aae", "abc") : List.of());
        if (hasWarnings) {
            assertThat(logs.getInfos()).containsExactly(format(
                "Number of warnings encountered when uploading document for a case %s: %s", CASE_ID, 2));
        } else {
            assertThat(logs.getInfos()).isEmpty();
        }
    }

    private UploadDocumentCollection createUploadDocumentCollection(UploadDocumentType type, String emailContent,
                                                                    CaseDocument link, LocalDate dateAdded, String comment,
                                                                    String fileName) {
        return UploadDocumentCollection.builder()
            .value(UploadDocument
                .builder()
                .documentType(type)
                .documentEmailContent(emailContent)
                .documentLink(link)
                .documentDateAdded(dateAdded)
                .documentComment(comment)
                .documentFileName(fileName)
                .build())
            .build();
    }
}
