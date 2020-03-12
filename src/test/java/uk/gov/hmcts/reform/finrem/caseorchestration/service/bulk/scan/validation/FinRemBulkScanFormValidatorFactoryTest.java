package uk.gov.hmcts.reform.finrem.caseorchestration.service.bulk.scan.validation;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.bsp.common.error.UnsupportedFormTypeException;
import uk.gov.hmcts.reform.bsp.common.service.BulkScanFormValidator;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.validation.FinRemBulkScanFormValidatorFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.validation.FormAValidator;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.junit.rules.ExpectedException.none;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_BULK_UNSUPPORTED_FORM_TYPE;

@RunWith(MockitoJUnitRunner.class)
public class FinRemBulkScanFormValidatorFactoryTest {

    @Rule
    public ExpectedException expectedException = none();

    @Mock
    private FormAValidator formAValidator;

    @InjectMocks
    private FinRemBulkScanFormValidatorFactory classUnderTest;

    @Before
    public void setUp() {
        classUnderTest.initBean();
    }

    @Test
    public void shouldReturnValidatorForFormA() {
        BulkScanFormValidator validator = classUnderTest.getValidator("formA");

        assertThat(validator, is(instanceOf(FormAValidator.class)));
        assertThat(validator, is(formAValidator));
    }

    @Test
    public void shouldThrowExceptionWhenFormTypeIsNotSupported() throws UnsupportedFormTypeException {
        expectedException.expect(UnsupportedFormTypeException.class);
        expectedException.expectMessage("\"unsupportedFormType\" form type is not supported");

        classUnderTest.getValidator(TEST_BULK_UNSUPPORTED_FORM_TYPE);
    }
}
