package uk.gov.hmcts.reform.finrem.caseorchestration.service.bulk.scan.transformation;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.bsp.common.error.UnsupportedFormTypeException;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.transformations.BulkScanFormTransformerFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.transformations.FormAToCaseTransformer;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.rules.ExpectedException.none;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_BULK_UNSUPPORTED_FORM_TYPE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.BulkScanForms.FORM_A;

@RunWith(MockitoJUnitRunner.class)
public class BulkScanFormTransformerFactoryTest {

    @Rule
    public ExpectedException expectedException = none();

    @Mock
    private FormAToCaseTransformer formAToCaseTransformer;

    @InjectMocks
    private BulkScanFormTransformerFactory bulkScanFormTransformerFactory;

    @Before
    public void setUp() {
        bulkScanFormTransformerFactory.init();
    }

    @Test
    public void shouldReturnRightTransformationStrategy() {
        assertThat(bulkScanFormTransformerFactory.getTransformer(FORM_A), is(formAToCaseTransformer));
    }

    @Test
    public void shouldThrowExceptionForUnsupportedFormType() {
        expectedException.expect(UnsupportedFormTypeException.class);
        expectedException.expectMessage("Form type \"unsupportedFormType\" is not supported.");

        bulkScanFormTransformerFactory.getTransformer(TEST_BULK_UNSUPPORTED_FORM_TYPE);
    }
}