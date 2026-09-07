package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.notifications;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum NotificationType {

    EMAIL("email"),
    POSTAL("postal");

    private final String id;

    @JsonValue
    public String getId() {
        return id;
    }
}
