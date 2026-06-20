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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadDocumentCollection;

import java.util.List;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class UploadDocumentConsentedAboutToSubmitHandlerTest {

    @Mock
    private DocumentWarningsHelper documentWarningsHelper;

    @InjectMocks
    private UploadDocumentConsentedAboutToSubmitHandler underTest;

    @Test
    void testCanHandle() {
        assertCanHandle(underTest, CallbackType.ABOUT_TO_SUBMIT, CaseType.CONSENTED, EventType.UPLOAD_DOCUMENT_CONSENTED);
    }

    @ParameterizedTest
    @NullAndEmptySource
    void givenUploadDocumentsProvided_whenHandledWithoutWarnings_thenPopulateWarnings(List<String> warnings) {
        List<UploadDocumentCollection> list = mock(List.class);

        FinremCaseData finremCaseData = spy(FinremCaseData.builder().build());
        finremCaseData.setUploadDocuments(list);

        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from(finremCaseData);

        ArgumentCaptor<Function> captor = ArgumentCaptor.forClass(Function.class);
        when(documentWarningsHelper.getDocumentWarnings(eq(callbackRequest), captor.capture(), eq(AUTH_TOKEN)))
            .thenReturn(warnings);

        //var response =
        var response = underTest.handle(callbackRequest, AUTH_TOKEN);
        assertThat(response.getWarnings()).isEmpty();

        verifyWarningParameter(captor);
    }

    @Test
    void givenUploadDocumentsProvided_whenHandledWithWarnings_thenPopulateWarnings() {
        List<UploadDocumentCollection> list = mock(List.class);

        FinremCaseData finremCaseData = spy(FinremCaseData.builder().build());
        finremCaseData.setUploadDocuments(list);

        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from(finremCaseData);

        ArgumentCaptor<Function> captor = ArgumentCaptor.forClass(Function.class);
        when(documentWarningsHelper.getDocumentWarnings(eq(callbackRequest), captor.capture(), eq(AUTH_TOKEN)))
            .thenReturn(List.of("warningsA"));

        //var response =
        var response = underTest.handle(callbackRequest, AUTH_TOKEN);
        assertAll(
            () -> assertThat(response.getWarnings()).containsOnly("warningsA"),
            () -> verifyWarningParameter(captor)
        );
    }

    private void verifyWarningParameter(ArgumentCaptor<Function> captor) {
        UploadDocumentCollection mockUploadGeneralDocument1 = mock(UploadDocumentCollection.class);
        UploadDocument uploadDocument = mock(UploadDocument.class);
        when(mockUploadGeneralDocument1.getValue()).thenReturn(uploadDocument);
        assertThat(
            captor.getValue().apply(FinremCaseData.builder().uploadDocuments(
                List.of(
                    mockUploadGeneralDocument1
                )
            ).build())
        ).isEqualTo(List.of(uploadDocument));
    }
}
