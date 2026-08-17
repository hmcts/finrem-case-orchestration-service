package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "FR_UploadConfidentialDocument", generate = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Deprecated
public class UploadConfidentialDocument implements HasCaseDocument {
    @CCD(
            label = "Type",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_s_caseDocumentType",
            typeParameterClass = FRSCaseDocumentType.class
    )
    @JsonProperty("DocumentType")
    private CaseDocumentType documentType;
    @CCD(label = "Document Url", searchable = false, typeOverride = FieldType.Document)
    @JsonProperty("DocumentLink")
    private CaseDocument documentLink;
    @CCD(label = "Date received", searchable = false)
    @JsonProperty("DocumentDateAdded")
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate documentDateAdded;
    @CCD(label = "Comment", searchable = false)
    @JsonProperty("DocumentComment")
    private String documentComment;
    @CCD(label = "File name", searchable = false)
    @JsonProperty("DocumentFileName")
    private String documentFileName;
    @CCD(label = "Upload DateTime", searchable = false)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime confidentialDocumentUploadDateTime;
}
