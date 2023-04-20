package uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain;

import lombok.Value;

import java.util.Map;

@Value
public final class EmailToSend {
    String emailAddress;
    String templateId;
    Map<String, String> templateFields;
    String referenceId;
}
