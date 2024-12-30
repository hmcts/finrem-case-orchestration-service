package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.agreed;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UploadedDraftOrder {

    @JsonProperty("agreedDraftOrderDocument")
    private CaseDocument agreedDraftOrderDocument;

    @JsonProperty("resubmission")
    private List<String> resubmission;

    @JsonProperty("additionalDocuments")
    private List<AgreedDraftOrderAdditionalDocumentsCollection> agreedDraftOrderAdditionalDocumentsCollection;
}
