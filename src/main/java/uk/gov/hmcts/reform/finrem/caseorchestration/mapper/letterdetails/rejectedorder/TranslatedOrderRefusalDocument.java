package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.rejectedorder;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.ccd.domain.Document;

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
    private Document orderRefusalDocs;
    private String orderRefusalJudge;
    private String orderRefusalJudgeName;
    private String orderRefusalDate;
    private String orderRefusalAddComments;

}