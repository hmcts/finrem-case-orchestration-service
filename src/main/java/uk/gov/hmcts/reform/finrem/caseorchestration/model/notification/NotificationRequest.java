package uk.gov.hmcts.reform.finrem.caseorchestration.model.notification;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class NotificationRequest {
    @JsonProperty("caseReferenceNumber")
    private String caseReferenceNumber;
    @JsonProperty("solicitorReferenceNumber")
    private String solicitorReferenceNumber;
    @JsonProperty("name")
    private String name;
    @JsonProperty("notificationEmail")
    private String notificationEmail;
    @JsonProperty("selectedCourt")
    private String selectedCourt;
    @JsonProperty("caseType")
    private String caseType;
    @JsonProperty("generalEmailBody")
    private String generalEmailBody;
}
