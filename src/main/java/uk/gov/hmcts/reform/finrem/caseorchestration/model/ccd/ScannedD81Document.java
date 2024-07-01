package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ScannedD81Document {
    private CaseDocument documentLink;
    private ScannedDocumentType scannedD81Type;
    private String scannedD81Subtype;
    private String scannedD81ControlNumber;
    private String scannedD81FileName;
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime scannedD81ScannedDate;
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime scannedD81DeliveryDate;
    private String scannedD81ExceptionRecordReference;
}
