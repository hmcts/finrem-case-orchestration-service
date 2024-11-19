package uk.gov.hmcts.reform.finrem.caseorchestration.handler.hearingbundles;

import org.junit.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ListForHearingWrapper;

import java.time.LocalDate;

import static org.junit.Assert.assertTrue;

public class HearingDatePopulatedValidatorTest {

    HearingDatePopulatedValidator hearingDatePopulatedValidator = new HearingDatePopulatedValidator();

    @Test
    public void shouldReturn_error_whenHearingDateNotPopulated() {
        assertTrue(hearingDatePopulatedValidator.validateHearingDate(FinremCaseData.builder().build()).contains("Missing hearing date."));
    }

    @Test
    public void shouldReturn_no_error_whenHearingDatePopulated() {
        assertTrue(hearingDatePopulatedValidator.validateHearingDate(FinremCaseData.builder()
            .listForHearingWrapper(ListForHearingWrapper.builder().hearingDate(LocalDate.now()).build())
            .build()).isEmpty());
    }
}
