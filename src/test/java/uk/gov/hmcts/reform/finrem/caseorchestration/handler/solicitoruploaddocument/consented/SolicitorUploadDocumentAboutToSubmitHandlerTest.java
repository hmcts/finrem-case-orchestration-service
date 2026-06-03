package uk.gov.hmcts.reform.finrem.caseorchestration.handler.solicitoruploaddocument.consented;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
}
