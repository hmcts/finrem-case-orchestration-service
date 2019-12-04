package uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.helper;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.exception.FormFieldValidationException;

import java.time.LocalDate;

import static java.time.Month.FEBRUARY;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.rules.ExpectedException.none;

public class BulkScanHelperTest {

    @Rule
    public ExpectedException expectedException = none();

    @Test
    public void shouldTransformDateWithRightLeapYearDate() {
        LocalDate date = BulkScanHelper.transformFormDateIntoLocalDate("DateFieldName", "29/02/2020");

        assertThat(date.getDayOfMonth(), is(29));
        assertThat(date.getMonth(), is(FEBRUARY));
        assertThat(date.getYear(), is(2020));
    }

    @Test
    public void shouldFailDateTransformationWithWrongLeapYearDate() {
        expectedException.expect(FormFieldValidationException.class);
        expectedException.expectMessage("DateFieldName must be a valid date");

        BulkScanHelper.transformFormDateIntoLocalDate("DateFieldName", "29/02/2019");
    }

}