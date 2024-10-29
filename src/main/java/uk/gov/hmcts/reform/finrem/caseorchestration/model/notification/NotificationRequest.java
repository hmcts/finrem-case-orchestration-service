package uk.gov.hmcts.reform.finrem.caseorchestration.model.notification;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class NotificationRequest {
    @JsonProperty("caseReferenceNumber")
    private String caseReferenceNumber;
    @JsonProperty("solicitorReferenceNumber")
    private String solicitorReferenceNumber;
    @JsonProperty("divorceCaseNumber")
    private String divorceCaseNumber;
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
    @JsonProperty("phoneOpeningHours")
    private String phoneOpeningHours;
    @JsonProperty("caseOrderType")
    private String caseOrderType;
    @JsonProperty("camelCaseOrderType")
    private String camelCaseOrderType;
    @JsonProperty("generalApplicationRejectionReason")
    private String generalApplicationRejectionReason;
    @JsonProperty("applicantName")
    private String applicantName;
    @JsonProperty("respondentName")
    private String respondentName;
    @JsonProperty("barristerReferenceNumber")
    private String barristerReferenceNumber;
    @JsonProperty("hearingType")
    private String hearingType;
    @JsonProperty("intervenerSolicitorReferenceNumber")
    private String intervenerSolicitorReferenceNumber;
    @JsonProperty("intervenerFullName")
    private String intervenerFullName;
    @JsonProperty("intervenerSolicitorFirm")
    private String intervenerSolicitorFirm;
    @JsonProperty("")
    private byte[] documentContents;
    @JsonProperty("isNotDigital")
    private Boolean isNotDigital;
    @JsonProperty("hearingDate")
    private String hearingDate;
}
