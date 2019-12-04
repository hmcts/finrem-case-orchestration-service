package uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.validation;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.UnsupportedFormTypeException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.junit.rules.ExpectedException.none;

@RunWith(MockitoJUnitRunner.class)
public class BulkScanFormValidatorFactoryTest {

    @Rule
    public ExpectedException expectedException = none();

    @Mock
    private FormAValidator formAValidator;

    @InjectMocks
    private BulkScanFormValidatorFactory classUnderTest;

    @Before
    public void setUp() {
        classUnderTest.initBean();
    }

    @Test
    public void shouldReturnValidatorForFormA() throws UnsupportedFormTypeException {
        BulkScanFormValidator validator = classUnderTest.getValidator("formA");

        assertThat(validator, is(instanceOf(FormAValidator.class)));
        assertThat(validator, is(formAValidator));
    }

    @Test
    public void shouldThrowExceptionWhenFormTypeIsNotSupported() throws UnsupportedFormTypeException {
        expectedException.expect(UnsupportedFormTypeException.class);
        expectedException.expectMessage("\"unsupportedFormType\" form type is not supported");

        classUnderTest.getValidator("unsupportedFormType");
    }
}