package uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UploadOrder {
    @JsonProperty("DocumentType")
    private UploadOrderDocumentType documentType;
    @JsonProperty("DocumentLink")
    private Document documentLink;
    @JsonProperty("DocumentEmailContent")
    private String documentEmailContent;
    @JsonProperty("DocumentDateAdded")
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate documentDateAdded;
    @JsonProperty("DocumentComment")
    private String documentComment;
    @JsonProperty("DocumentFileName")
    private String documentFileName;
}
