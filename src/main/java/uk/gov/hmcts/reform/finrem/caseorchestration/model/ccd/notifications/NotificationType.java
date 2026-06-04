package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.notifications;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.RequiredArgsConstructor;

/**
 * Channel through which a notification was (or will be) sent to a party.
 */

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