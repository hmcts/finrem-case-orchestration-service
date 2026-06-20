package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.helper.DocumentWarningsHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadGeneralDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadGeneralDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory.UploadGeneralDocumentsCategoriser;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class UploadDocumentContestedAboutToSubmitHandlerTest {

    @Mock
    private UploadGeneralDocumentsCategoriser uploadGeneralDocumentsCategoriser;
    @Mock
    private DocumentWarningsHelper documentWarningsHelper;

    @InjectMocks
    private UploadDocumentContestedAboutToSubmitHandler underTest;

    @Test
    void testCanHandle() {
        assertCanHandle(underTest, CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.UPLOAD_DOCUMENT_CONTESTED);
    }

    @Test
    void givenAnyData_whenHandled_thenCategoriseUploadGeneralDocuments() {
        FinremCaseData finremCaseData = mock(FinremCaseData.class);
        underTest.handle(FinremCallbackRequestFactory.from(finremCaseData), AUTH_TOKEN);
        verify(uploadGeneralDocumentsCategoriser).categorise(finremCaseData);
    }

    @ParameterizedTest
    @NullAndEmptySource
    void givenUploadGeneralDocumentsProvided_whenHandledWithoutWarnings_thenPopulateWarnings(List<String> warnings) {
        List<UploadGeneralDocumentCollection> list = mock(List.class);

        FinremCaseData finremCaseData = spy(FinremCaseData.builder().build());
        finremCaseData.setUploadGeneralDocuments(list);

        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from(finremCaseData);

        ArgumentCaptor<Function> captor = ArgumentCaptor.forClass(Function.class);
        when(documentWarningsHelper.getDocumentWarnings(eq(callbackRequest), captor.capture(), eq(AUTH_TOKEN)))
            .thenReturn(warnings);

        var response = underTest.handle(callbackRequest, AUTH_TOKEN);
        assertThat(response.getWarnings()).isEmpty();

        verifyWarningParameter(captor);
    }

    @Test
    void givenUploadGeneralDocumentsProvided_whenHandledWithWarnings_thenPopulateWarnings() {
        List<UploadGeneralDocumentCollection> list = mock(List.class);

        FinremCaseData finremCaseData = spy(FinremCaseData.builder().build());
        finremCaseData.setUploadGeneralDocuments(list);

        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from(finremCaseData);

        ArgumentCaptor<Function> captor = ArgumentCaptor.forClass(Function.class);
        when(documentWarningsHelper.getDocumentWarnings(eq(callbackRequest), captor.capture(), eq(AUTH_TOKEN)))
            .thenReturn(List.of("warningsA"));

        var response = underTest.handle(callbackRequest, AUTH_TOKEN);
        assertAll(
            () -> assertThat(response.getWarnings()).containsOnly("warningsA"),
            () -> verifyWarningParameter(captor)
        );
    }

    @Test
    void givenUploadGeneralDocumentsProvided_whenHandled_thenDoSorting() {
        UploadGeneralDocumentCollection expectedFirstElement;
        UploadGeneralDocumentCollection expectedSecondElement;
        UploadGeneralDocumentCollection expectedLastElement;

        FinremCaseData finremCaseData = spy(FinremCaseData.builder().build());
        finremCaseData.setUploadGeneralDocuments(new ArrayList<>(List.of(
            expectedSecondElement = uploadGeneralDocumentCollection(LocalDateTime.of(2026, 1, 1, 23, 59, 59)),
            expectedFirstElement = uploadGeneralDocumentCollection(LocalDateTime.of(2026, 1, 2, 23, 59, 59)),
            expectedLastElement = uploadGeneralDocumentCollection((LocalDateTime) null)
        )));

        var response = underTest.handle(FinremCallbackRequestFactory.from(finremCaseData), AUTH_TOKEN);
        assertThat(response.getData().getUploadGeneralDocuments())
            .containsExactly(expectedFirstElement, expectedSecondElement, expectedLastElement);
    }

    private UploadGeneralDocumentCollection uploadGeneralDocumentCollection(LocalDateTime generalDocumentUploadDateTime) {
        return UploadGeneralDocumentCollection.builder()
            .value(UploadGeneralDocument.builder()
                .generalDocumentUploadDateTime(generalDocumentUploadDateTime)
                .build()
            ).build();
    }

    private UploadGeneralDocumentCollection uploadGeneralDocumentCollection(UploadGeneralDocument uploadGeneralDocument) {
        return UploadGeneralDocumentCollection.builder()
            .value(uploadGeneralDocument)
            .build();
    }

    private void verifyWarningParameter(ArgumentCaptor<Function> captor) {
        UploadGeneralDocument mockUploadGeneralDocument1 = mock(UploadGeneralDocument.class);
        UploadGeneralDocument mockUploadGeneralDocument2 = mock(UploadGeneralDocument.class);
        assertThat(
            captor.getValue().apply(FinremCaseData.builder().uploadGeneralDocuments(
                List.of(
                    uploadGeneralDocumentCollection(mockUploadGeneralDocument1),
                    uploadGeneralDocumentCollection(mockUploadGeneralDocument2)
                )
            ).build())
        ).isEqualTo(List.of(mockUploadGeneralDocument1, mockUploadGeneralDocument2));
    }
}
