package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.JudgeType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FRFlRefusalOrderJudgeType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "FR_extraReportFieldsInput", generate = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExtraReportFieldsInput {

    @CCD(label = " ", searchable = false, typeOverride = FieldType.YesOrNo)
    @JsonProperty("showRequireExtraReportFieldsInputQuestion")
    private YesOrNo showRequireExtraReportFieldsInputQuestion;

    @CCD(
            label = "Which judge title will be shown in the cover letter or refusal order?",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_fl_RefusalOrderJudgeType",
            typeParameterClass = FRFlRefusalOrderJudgeType.class
    )
    @JsonProperty("judgeType")
    private JudgeType judgeType;

}
