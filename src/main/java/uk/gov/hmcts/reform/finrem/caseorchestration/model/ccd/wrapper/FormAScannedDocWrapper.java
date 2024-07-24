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
public class FormAScannedDocWrapper {
    private ScannedDocumentType formAType;
    private String formASubtype;
    private String formAControlNumber;
    private String formAFileName;
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime formAScannedDate;
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime formADeliveryDate;
    private String formAExceptionRecordReference;
}
