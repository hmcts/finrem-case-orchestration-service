package uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.validation;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.CaseOrchestrationApplication;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.junit.rules.ExpectedException.none;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = CaseOrchestrationApplication.class)
@TestPropertySource(locations = "/application.properties")
public class BulkScanFormValidatorFactoryTest {

    @Rule
    public ExpectedException expectedException = none();

    @Autowired
    private BulkScanFormValidatorFactory bulkScanFormValidatorFactory;

    @Test
    public void shouldReturnValidatorForFormA() throws Exception {
        BulkScanFormValidator validator = bulkScanFormValidatorFactory.getValidator("formA");

        assertThat(validator, is(instanceOf(FormAValidator.class)));
    }

    @Test
    public void shouldThrowExceptionWhenFormTypeIsNotSupported() throws Exception {
        expectedException.expect(UnsupportedOperationException.class);
        expectedException.expectMessage("\"unsupportedFormType\" form type is not supported");

        bulkScanFormValidatorFactory.getValidator("unsupportedFormType");
    }
}
