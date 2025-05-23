package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UploadGeneralDocument implements HasUploadingDocuments, HasCaseDocument {
    @JsonProperty("DocumentType")
    private UploadGeneralDocumentType documentType;
    @JsonProperty("DocumentEmailContent")
    private String documentEmailContent;
    @JsonProperty("DocumentLink")
    private CaseDocument documentLink;
    @JsonProperty("DocumentDateAdded")
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate documentDateAdded;
    @JsonProperty("DocumentComment")
    private String documentComment;
    @JsonProperty("DocumentFileName")
    private String documentFileName;
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime generalDocumentUploadDateTime;

    @Override
    @JsonIgnore
    public List<CaseDocument> getUploadingDocuments() {
        return documentLink != null ? List.of(documentLink) : List.of();
    }
}
