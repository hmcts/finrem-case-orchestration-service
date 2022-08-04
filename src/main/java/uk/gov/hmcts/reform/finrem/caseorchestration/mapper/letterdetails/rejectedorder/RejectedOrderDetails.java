package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.rejectedorder;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.DocumentTemplateDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.FrcCourtDetails;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RejectedOrderDetails implements DocumentTemplateDetails {
    @JsonProperty("divorceCaseNumber")
    private String divorceCaseNumber;
    @JsonProperty("ApplicantName")
    private String applicantName;
    @JsonProperty("RespondentName")
    private String respondentName;
    private String civilPartnership;
    private List<TranslatedOrderRefusalDocumentCollection> orderRefusalCollectionNew;
    @JsonProperty("CourtName")
    private String courtName;
    private FrcCourtDetails courtDetails;
    @JsonProperty("RefusalOrderHeader")
    private String refusalOrderHeader;
    private String orderType;
}
