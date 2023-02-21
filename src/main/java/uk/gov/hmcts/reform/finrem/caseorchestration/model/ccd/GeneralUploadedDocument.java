package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
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
public class GeneralUploadedDocument {

    @JsonProperty("DocumentType")
    private String documentType;

    @JsonProperty("DocumentEmailContent")
    private String documentEmailContent;

    @JsonProperty("DocumentLink")
    private CaseDocument documentLink;

    @JsonProperty("DocumentDateAdded")
    private String documentDateAdded;

    @JsonProperty("DocumentComment")
    private String documentComment;

    @JsonProperty("DocumentFileName")
    private String documentFileName;

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime generalDocumentUploadDateTime;
}
