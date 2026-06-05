package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.notifications;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class NotificationAuditCollectionItem {

    private UUID id;
    private NotificationAudit value;
}
