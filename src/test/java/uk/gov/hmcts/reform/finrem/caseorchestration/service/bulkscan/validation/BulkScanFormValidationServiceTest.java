package uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.validation;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.BulkScanForm;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BulkScanFormValidationServiceTest extends BaseServiceTest {

    @Mock
    private FormAValidator formAValidator;

    @Mock
    private BulkScanFormValidatorFactory bulkScanFormValidatorFactory;

    private BulkScanValidationService bulkScanValidationService;

    @Before
    public void init() throws Exception {
        bulkScanValidationService = new BulkScanValidationService(bulkScanFormValidatorFactory);
        when(bulkScanFormValidatorFactory.getValidator(BulkScanForm.FORM_A.getFormName())).thenReturn(formAValidator);
    }

    @Test
    public void whenValidateCalled_thenItShouldDelegateToFormValidator() throws Exception {
        bulkScanValidationService.validate(BulkScanForm.FORM_A.getFormName(), Collections.emptyList());
        verify(formAValidator, times(1)).validate(anyList());
    }
}
