package uk.gov.hmcts.reform.finrem.caseorchestration.handler.sendorder.contested;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CcdService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CorrespondenceEventAuditOrchestrationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralOrderService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.sendorder.SendOrderCorresponder;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement.EvidenceManagementDeleteService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry.RetryExecutor;

import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class SendOrderSubmittedHandlerTest {

    @InjectMocks
    private SendOrderSubmittedHandler underTest;
    @Mock
    private GeneralOrderService generalOrderService;
    @Mock
    private CcdService ccdService;
    @Mock
    private SendOrderCorresponder sendCorrespondences;
    @Mock
    private RetryExecutor retryExecutor;
    @Mock
    private EvidenceManagementDeleteService evidenceManagementDeleteService;
    @Mock
    private CorrespondenceEventAuditOrchestrationService correspondenceEventAuditOrchestrationService;

    @Test
    void testCanHandle() {
        assertCanHandle(underTest, CallbackType.SUBMITTED, CaseType.CONTESTED, EventType.SEND_ORDER);
    }
}
