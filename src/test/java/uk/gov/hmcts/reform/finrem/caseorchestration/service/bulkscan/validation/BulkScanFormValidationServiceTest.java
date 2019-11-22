package uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.validation;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.BulkScanForms;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BulkScanFormValidationServiceTest extends BaseServiceTest {

    @Mock
    private FormAValidator formAValidator;

    @InjectMocks
    private BulkScanFormValidatorFactory bulkScanFormValidatorFactory;

    private BulkScanValidationService bulkScanValidationService;

    @Before
    public void init() {
        bulkScanValidationService = new BulkScanValidationService(bulkScanFormValidatorFactory);
    }

    @Test
    public void whenValidateCalled_thenItShouldDelegateToFormValidator() {
        bulkScanValidationService.validate(BulkScanForms.FORM_A, Collections.emptyList());
        verify(formAValidator, times(1)).validate(anyList());
    }
}
