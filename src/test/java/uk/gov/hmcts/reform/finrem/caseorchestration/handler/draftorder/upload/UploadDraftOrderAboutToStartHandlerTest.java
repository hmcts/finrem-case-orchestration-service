package uk.gov.hmcts.reform.finrem.caseorchestration.handler.draftorder.upload;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.draftorders.upload.UploadDraftOrdersAboutToStartHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;

import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

class UploadDraftOrderAboutToStartHandlerTest {

    private UploadDraftOrdersAboutToStartHandler handler;

    @BeforeEach
    public void setup() {
        handler = new UploadDraftOrdersAboutToStartHandler(new FinremCaseDetailsMapper(new ObjectMapper().registerModule(new JavaTimeModule())));
    }

    @Test
    void canHandle() {
        assertCanHandle(handler, CallbackType.ABOUT_TO_START, CaseType.CONTESTED, EventType.DRAFT_ORDERS);
    }

}
