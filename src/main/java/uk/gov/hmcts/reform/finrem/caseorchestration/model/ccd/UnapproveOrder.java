package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UnapproveOrder implements HasCaseDocument {

    @JsonProperty("unapproveOrder")
    CaseDocument caseDocument;

    @JsonProperty("additionalConsentDocuments")
    private List<DocumentCollectionItem> additionalConsentDocuments;

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime orderReceivedAt;
}
