package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "FR_hearingUploadBundle", generate = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HearingUploadBundleHolder {
    @CCD(label = "Hearing Bundle Date", searchable = false)
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate hearingBundleDate;
    @CCD(
            label = "Is this a Financial dispute resolution(FDR) hearing?",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private YesOrNo hearingBundleFdr;
    @CCD(
            label = "Upload Hearing Bundle",
            hint = "Please note you should only update and amend your own firm's hearing bundle",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_hearingBundle"
    )
    private List<HearingBundleDocumentCollection> hearingBundleDocuments;
    @CCD(label = "Add a description", searchable = false)
    private String hearingBundleDescription;
}
