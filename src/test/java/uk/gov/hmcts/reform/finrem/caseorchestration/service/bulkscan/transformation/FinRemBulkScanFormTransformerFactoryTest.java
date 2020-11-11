package uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.transformation;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.bsp.common.error.UnsupportedFormTypeException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.rules.ExpectedException.none;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_BULK_UNSUPPORTED_FORM_TYPE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.BulkScanForms.FORM_A;

@RunWith(MockitoJUnitRunner.class)
public class FinRemBulkScanFormTransformerFactoryTest {

    @Rule
    public ExpectedException expectedException = none();

    @Mock
    private FormAToCaseTransformer formAToCaseTransformer;

    @InjectMocks
    private FinRemBulkScanFormTransformerFactory finRemBulkScanFormTransformerFactory;

    @Before
    public void setUp() {
        finRemBulkScanFormTransformerFactory.init();
    }

    @Test
    public void shouldReturnRightTransformationStrategy() {
        assertThat(finRemBulkScanFormTransformerFactory.getTransformer(FORM_A), is(formAToCaseTransformer));
    }

    @Test
    public void shouldThrowExceptionForUnsupportedFormType() {
        expectedException.expect(UnsupportedFormTypeException.class);
        expectedException.expectMessage("Form type \"unsupportedFormType\" is not supported.");

        finRemBulkScanFormTransformerFactory.getTransformer(TEST_BULK_UNSUPPORTED_FORM_TYPE);
    }
}