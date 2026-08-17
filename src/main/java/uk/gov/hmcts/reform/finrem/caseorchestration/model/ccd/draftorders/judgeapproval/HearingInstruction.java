package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "FR_hearingInstruction", generate = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HearingInstruction {

    @CCD(label = "Is there another hearing to be listed?", searchable = false, typeOverride = FieldType.YesOrNo)
    @JsonProperty("requireAnotherHearing")
    private YesOrNo requireAnotherHearing;

    @CCD(label = " ", searchable = false, typeOverride = FieldType.YesOrNo)
    @JsonProperty("showRequireAnotherHearingQuestion")
    private YesOrNo showRequireAnotherHearingQuestion;

    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_anotherHearingRequest"
    )
    @JsonProperty("anotherHearingRequestCollection")
    private List<AnotherHearingRequestCollection> anotherHearingRequestCollection;

}
