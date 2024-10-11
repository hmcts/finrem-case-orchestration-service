package uk.gov.hmcts.reform.finrem.caseorchestration.handler.draftorder.upload;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.draftorders.upload.UploadDraftOrdersAboutToSubmitHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory.DraftOrdersCategoriser;

import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class UploadDraftOrderAboutToSubmitHandlerTest {

    private UploadDraftOrdersAboutToSubmitHandler handler;

    @Mock
    DraftOrdersCategoriser draftOrdersCategoriser;

    @BeforeEach
    public void setup() {
        handler = new UploadDraftOrdersAboutToSubmitHandler(new FinremCaseDetailsMapper(new ObjectMapper().registerModule(new JavaTimeModule())),
            draftOrdersCategoriser);
    }

    @Test
    void canHandle() {
        assertCanHandle(handler, CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.DRAFT_ORDERS);
    }

}
