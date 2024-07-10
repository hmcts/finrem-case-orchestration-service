package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;

@Builder
@Data
public class CallbackContext {
    private CallbackType callbackType;
    private String pageId;
}
