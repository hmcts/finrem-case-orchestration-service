package uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
public class TranslatedOrderRefusalDocument {
    private String orderRefusalAfterText;
    private List<String> orderRefusal;
    private String orderRefusalOther;
    private CaseDocument orderRefusalDocs;
    private String orderRefusalJudge;
    private String orderRefusalJudgeName;
    private String orderRefusalDate;
    private String orderRefusalAddComments;

}