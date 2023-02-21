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
public class ScannedDocument {
    private ScannedDocumentType type;
    private String subtype;
    private CaseDocument url;
    private String controlNumber;
    private String fileName;
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime scannedDate;
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime deliveryDate;
    private String exceptionRecordReference;
}
