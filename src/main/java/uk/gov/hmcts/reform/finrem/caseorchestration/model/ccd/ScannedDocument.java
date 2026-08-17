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
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ScannedDocument implements HasCaseDocument {
    @CCD(
            label = "Document Type",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "ScannedDocumentType"
    )
    private ScannedDocumentType type;
    @CCD(label = "Document Subtype", searchable = false)
    private String subtype;
    @CCD(
            label = "Scanned document url",
            categoryID = "lipOrScannedDocuments",
            searchable = false,
            typeOverride = FieldType.Document
    )
    private CaseDocument url;
    @CCD(label = "Document Control Number", searchable = false)
    private String controlNumber;
    @CCD(label = "File Name", searchable = false)
    private String fileName;
    @CCD(label = "Scanned Date", searchable = false)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime scannedDate;
    @CCD(label = "Delivery Date", searchable = false)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime deliveryDate;
    @CCD(label = "Exception record reference", searchable = false)
    private String exceptionRecordReference;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @JsonProperty("# Record Meta data")
  @CCD(label = "Scanned Records", searchable = false, typeOverride = FieldType.Label)
  private String __Record_Meta_data;
  // ==== end synthesised definition-only fields ====
}
