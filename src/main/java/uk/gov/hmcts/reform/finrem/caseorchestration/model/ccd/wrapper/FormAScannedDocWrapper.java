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
import uk.gov.hmcts.ccd.sdk.api.CCD;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FormAScannedDocWrapper {
    @CCD(ignore = true)
    private ScannedDocumentType formAType;
    @CCD(ignore = true)
    private String formASubtype;
    @CCD(ignore = true)
    private String formAControlNumber;
    @CCD(ignore = true)
    private String formAFileName;
    @CCD(ignore = true)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime formAScannedDate;
    @CCD(ignore = true)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime formADeliveryDate;
    @CCD(ignore = true)
    private String formAExceptionRecordReference;
}
