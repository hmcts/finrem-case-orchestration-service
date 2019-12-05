package uk.gov.hmcts.reform.finrem.caseorchestration.model.bulk.scan.validation.out;

import org.junit.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.scan.validation.out.OcrValidationResult;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.scan.validation.out.ValidationStatus.ERRORS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.scan.validation.out.ValidationStatus.SUCCESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.scan.validation.out.ValidationStatus.WARNINGS;

public class OcrValidationResultTest {

    @Test
    public void shouldHaveErrorsStatusWhenThereIsAnErrorMessage() {
        OcrValidationResult warningAndErrorMessagesResult = OcrValidationResult.builder()
            .addWarning("Warning message")
            .addError("Error message")
            .build();
        assertThat(warningAndErrorMessagesResult.getStatus(), is(ERRORS));

        OcrValidationResult errorMessagesOnlyResult = OcrValidationResult.builder().addError("Error message").build();
        assertThat(errorMessagesOnlyResult.getStatus(), is(ERRORS));
    }

    @Test
    public void shouldHaveWarningsStatusWhenThereIs_WarningMessage_ButNoErrorMessage() {
        OcrValidationResult warningMessagesOnlyResult = OcrValidationResult.builder().addWarning("Error message").build();
        assertThat(warningMessagesOnlyResult.getStatus(), is(WARNINGS));
    }

    @Test
    public void shouldHaveSuccessStatusWhenThereIs_NoWarningMessage_NorErrorMessage() {
        OcrValidationResult noMessagesResult = OcrValidationResult.builder().build();
        assertThat(noMessagesResult.getStatus(), is(SUCCESS));
    }
}