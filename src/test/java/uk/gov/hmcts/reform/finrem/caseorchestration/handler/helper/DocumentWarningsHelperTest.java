package uk.gov.hmcts.reform.finrem.caseorchestration.handler.helper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HasUploadingDocuments;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadingDocumentAccessor;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NewUploadedDocumentsService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentchecker.DocumentCheckerService;
import uk.gov.hmcts.reform.finrem.caseorchestration.util.TestLogger;
import uk.gov.hmcts.reform.finrem.caseorchestration.util.TestLogs;

import java.util.List;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;

@ExtendWith(SpringExtension.class)
class DocumentWarningsHelperTest {

    @TestLogs
    private final TestLogger logs = new TestLogger(DocumentWarningsHelper.class);
    @Mock
    private NewUploadedDocumentsService newUploadedDocumentsService;
    @Mock
    private DocumentCheckerService documentCheckerService;
    @InjectMocks
    private DocumentWarningsHelper underTest;

    @Test
    void givenValidCaseDataWithoutDocumentUploaded_thenReturnEmptyList() {
        FinremCaseData mockedCaseData = mock(FinremCaseData.class);
        FinremCaseData mockedCaseDataBefore = mock(FinremCaseData.class);
        Function<FinremCaseData, List<DummyUploadingDocumentAccessor>> accessor = createAccessor();

        when(newUploadedDocumentsService.getNewUploadDocuments(mockedCaseData, mockedCaseDataBefore, accessor)).thenReturn(List.of());

        List<String> actual = underTest.getDocumentWarnings(buildFinremCallbackRequest(mockedCaseData, mockedCaseDataBefore), accessor, AUTH_TOKEN);
        assertThat(actual).isEmpty();
    }

    @Test
    void givenValidCaseDataWithDocumentUpload_whenUnexpectedExceptionThrownInWarningChecking_thenReturnEmptyListAndLogInErrorLevel() {
        FinremCaseData mockedCaseData = mock(FinremCaseData.class);
        FinremCaseData mockedCaseDataBefore = mock(FinremCaseData.class);
        Function<FinremCaseData, List<DummyUploadingDocumentAccessor>> accessor = createAccessor();
        DummyUploadingDocumentAccessor mockedDummyUploadingDocumentAccessor = mock(DummyUploadingDocumentAccessor.class);
        DummyHasUploadingDocuments mockedSampleHasDocumentLink = mock(DummyHasUploadingDocuments.class);
        CaseDocument mockedCaesDocument = mock(CaseDocument.class);

        when(mockedDummyUploadingDocumentAccessor.getValue()).thenReturn(mockedSampleHasDocumentLink);
        when(mockedSampleHasDocumentLink.getUploadingDocuments()).thenReturn(List.of(caseDocument()));
        when(newUploadedDocumentsService.getNewUploadDocuments(mockedCaseData, mockedCaseDataBefore, accessor)).thenReturn(List.of(
            mockedCaesDocument));
        when(documentCheckerService.getWarnings(eq(mockedCaesDocument), any(FinremCaseDetails.class), any(FinremCaseDetails.class), eq(AUTH_TOKEN)))
            .thenThrow(new RuntimeException("unexpected exception"));

        List<String> actual = underTest.getDocumentWarnings(buildFinremCallbackRequest(mockedCaseData, mockedCaseDataBefore), accessor, AUTH_TOKEN);
        assertThat(actual).isEmpty();
        assertThat(logs.getErrors()).contains(CASE_ID + " - Exception was caught when checking the warnings");
    }

    @Test
    void givenValidCaseData_whenWarningIsDetected_thenReturnListOfWarningMessage() {
        FinremCaseData mockedCaseData = mock(FinremCaseData.class);
        FinremCaseData mockedCaseDataBefore = mock(FinremCaseData.class);
        Function<FinremCaseData, List<DummyUploadingDocumentAccessor>> accessor = createAccessor();
        DummyUploadingDocumentAccessor mockedDummyUploadingDocumentAccessor = mock(DummyUploadingDocumentAccessor.class);
        DummyHasUploadingDocuments mockedSampleHasDocumentLink = mock(DummyHasUploadingDocuments.class);

        CaseDocument unacceptedDocument = caseDocument("https://fakeurl/unaccepted", "unaccepted.docx");
        when(mockedDummyUploadingDocumentAccessor.getValue()).thenReturn(mockedSampleHasDocumentLink);
        when(mockedSampleHasDocumentLink.getUploadingDocuments()).thenReturn(List.of(unacceptedDocument));
        when(newUploadedDocumentsService.getNewUploadDocuments(mockedCaseData, mockedCaseDataBefore, accessor)).thenReturn(List.of(
            unacceptedDocument));
        when(documentCheckerService.getWarnings(eq(unacceptedDocument), any(FinremCaseDetails.class), any(FinremCaseDetails.class), eq(AUTH_TOKEN)))
            .thenReturn(List.of("unaccepted document detected"));

        List<String> actual = underTest.getDocumentWarnings(buildFinremCallbackRequest(mockedCaseData, mockedCaseDataBefore), accessor, AUTH_TOKEN);
        assertThat(actual).containsExactly("unaccepted document detected");
        assertThat(logs.getInfos()).contains(CASE_ID + " - Number of warnings encountered when uploading document: 1");
    }

    @Test
    void givenValidCaseData_whenMultipleWarningsAreDetected_thenReturnSortedListOfWarningMessages() {
        FinremCaseData mockedCaseData = mock(FinremCaseData.class);
        FinremCaseData mockedCaseDataBefore = mock(FinremCaseData.class);
        Function<FinremCaseData, List<DummyUploadingDocumentAccessor>> accessor = createAccessor();
        DummyUploadingDocumentAccessor mockedDummyUploadingDocumentAccessor = mock(DummyUploadingDocumentAccessor.class);
        DummyHasUploadingDocuments mockedSampleHasDocumentLink = mock(DummyHasUploadingDocuments.class);

        CaseDocument unacceptedDocument = caseDocument("https://fakeurl/unaccepted", "unaccepted.docx");
        when(mockedDummyUploadingDocumentAccessor.getValue()).thenReturn(mockedSampleHasDocumentLink);
        when(mockedSampleHasDocumentLink.getUploadingDocuments()).thenReturn(List.of(unacceptedDocument));
        when(newUploadedDocumentsService.getNewUploadDocuments(mockedCaseData, mockedCaseDataBefore, accessor)).thenReturn(List.of(
            unacceptedDocument));
        when(documentCheckerService.getWarnings(eq(unacceptedDocument), any(FinremCaseDetails.class), any(FinremCaseDetails.class), eq(AUTH_TOKEN)))
            .thenReturn(List.of("unaccepted document detected", "another warning detected"));

        List<String> actual = underTest.getDocumentWarnings(buildFinremCallbackRequest(mockedCaseData, mockedCaseDataBefore), accessor, AUTH_TOKEN);
        assertThat(actual).containsExactly("another warning detected", "unaccepted document detected");
        assertThat(logs.getInfos()).contains(CASE_ID + " - Number of warnings encountered when uploading document: 2");
    }

    private static class DummyHasUploadingDocuments implements HasUploadingDocuments {

        @Override
        public List<CaseDocument> getUploadingDocuments() {
            return List.of();
        }
    }

    private static class DummyUploadingDocumentAccessor implements UploadingDocumentAccessor<DummyHasUploadingDocuments> {

        public DummyHasUploadingDocuments getValue() {
            return null;
        }
    }

    private static Function<FinremCaseData, List<DummyUploadingDocumentAccessor>> createAccessor() {
        return caseData -> null;
    }

    private FinremCallbackRequest buildFinremCallbackRequest(FinremCaseData mockedCaseData, FinremCaseData mockedCaseDataBefore) {
        return FinremCallbackRequest.builder()
            .caseDetailsBefore(FinremCaseDetails.builder().data(mockedCaseDataBefore).build())
            .caseDetails(FinremCaseDetails.builder().id(Long.valueOf(CASE_ID)).data(mockedCaseData).build())
            .build();
    }

}
