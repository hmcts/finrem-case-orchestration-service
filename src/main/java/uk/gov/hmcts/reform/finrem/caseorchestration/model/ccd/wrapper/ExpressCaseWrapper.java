package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ExpressCaseParticipation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.LabelForExpressCaseAmendment;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExpressCaseWrapper {

    private ExpressCaseParticipation expressCaseParticipation;
    private LabelForExpressCaseAmendment labelForExpressCaseAmendment;
    private YesOrNo expressPilotQuestion;

    private DynamicMultiSelectList confirmRemoveCaseFromExpressPilot;
}
