package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ScannedDocumentType;

import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConsentOrderScannedDocWrapper {
    private ScannedDocumentType consentOrderType;
    private String consentOrderSubtype;
    private String consentOrderControlNumber;
    private String consentOrderFileName;
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime consentOrderScannedDate;
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime consentOrderDeliveryDate;
    private String consentOrderExceptionRecordReference;
}
