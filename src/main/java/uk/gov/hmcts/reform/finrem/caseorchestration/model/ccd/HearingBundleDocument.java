package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "FR_hearingBundle", generate = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HearingBundleDocument {
    @CCD(label = "Bundle Name", searchable = false, typeOverride = FieldType.Document)
    private CaseDocument bundleDocuments;
    @CCD(label = "Bundle Upload Date", searchable = false, retainHiddenValue = true)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime bundleUploadDate;
}
