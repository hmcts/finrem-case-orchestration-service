package uk.gov.hmcts.reform.finrem.caseorchestration.handler.solicitoruploaddocument.consented;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.SolUploadDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.SolUploadDocumentCollection;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class SolicitorUploadDocumentAboutToStartHandlerTest {

    @InjectMocks
    private SolicitorUploadDocumentAboutToStartHandler underTest;

    @Test
    void testCanHandle() {
        assertCanHandle(underTest, CallbackType.ABOUT_TO_START, CaseType.CONSENTED, EventType.SOLICITOR_UPLOAD_DOCUMENT);
    }

    @NullAndEmptySource
    @ParameterizedTest
    void givenNullOrEmptySolUploadDocumentCollection_whenHandled_shouldPrepopulateAnEmptyEntry(
        List<SolUploadDocumentCollection> solUploadDocumentCollections
    ) {
        FinremCallbackRequest request = FinremCallbackRequestFactory.from(
            FinremCaseData.builder()
                .solUploadDocuments(solUploadDocumentCollections)
                .build()
        );

        var response = underTest.handle(request, AUTH_TOKEN);

        assertThat(response.getData().getSolUploadDocuments())
            .isEqualTo(List.of(
                SolUploadDocumentCollection.builder().value(SolUploadDocument.builder().build()).build()
            ));
    }
}
