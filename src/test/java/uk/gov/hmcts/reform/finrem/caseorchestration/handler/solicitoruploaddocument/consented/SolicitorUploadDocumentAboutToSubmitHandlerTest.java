package uk.gov.hmcts.reform.finrem.caseorchestration.handler.solicitoruploaddocument.consented;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GenericInputFields;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.verifyTemporaryFieldsWereSanitised;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class SolicitorUploadDocumentAboutToSubmitHandlerTest {

    @InjectMocks
    private SolicitorUploadDocumentAboutToSubmitHandler underTest;

    @Mock
    private FinremCaseDetailsMapper finremCaseDetailsMapper;

    @Test
    void testCanHandle() {
        assertCanHandle(underTest, CallbackType.ABOUT_TO_SUBMIT, CaseType.CONSENTED, EventType.SOLICITOR_UPLOAD_DOCUMENT);
    }

    @Test
    void shouldRemoveReadyToSubmitDocumentWhenHandled() {
        FinremCaseData finremCaseData = FinremCaseData.builder()
            .genericInputFields(GenericInputFields.builder().readyToSubmitDocument(YesOrNo.YES).build())
            .build();
        FinremCaseDetails finremCaseDetails = FinremCaseDetails.builder().data(finremCaseData).build();

        verifyTemporaryFieldsWereSanitised(underTest,
            finremCaseDetails, finremCaseDetailsMapper, new HashMap<>(Map.of(
                "readyToSubmitDocument", YesOrNo.YES
            ))
        );
    }

    @Test
    void givenReadyToSubmit_whenHandled_thenSetInfoReceivedCaseStateAndWarningPopulated() {
        FinremCaseData finremCaseData = FinremCaseData.builder()
            .genericInputFields(GenericInputFields.builder().readyToSubmitDocument(YesOrNo.YES).build())
            .build();

        var response = underTest.handle(FinremCallbackRequestFactory.from(finremCaseData), AUTH_TOKEN);
        assertAll(
            () -> assertThat(response.getState()).isEqualTo("infoReceived"),
            () -> assertThat(response.getWarnings()).containsOnly(
                "Please note your documents will be submitted and you won't be able to upload any additional documents."
            )
        );
    }

    @Test
    void givenNotReadyToSubmit_whenHandled_thenCaseStateShouldBeNullAndWarningPopulated() {
        FinremCaseData finremCaseData = FinremCaseData.builder()
            .genericInputFields(GenericInputFields.builder().readyToSubmitDocument(YesOrNo.NO).build())
            .build();

        var response = underTest.handle(FinremCallbackRequestFactory.from(finremCaseData), AUTH_TOKEN);
        assertAll(
            () -> assertThat(response.getState()).isNull(),
            () -> assertThat(response.getWarnings()).containsOnly(
                "Please note your documents will not be submitted, to allow you upload additional documents."
            )
        );
    }
}
