package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicList;

import java.time.LocalDate;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "FR_workingVacatedHearing", generate = true)
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WorkingVacatedHearing {
    @CCD(
            label = "Adjourn or Vacate a hearing?",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_VacateOrAdjournHearingAction"
    )
    private VacateOrAdjournAction vacateOrAdjournAction;
    @CCD(label = "Which hearing?", searchable = false, typeOverride = FieldType.DynamicList)
    private DynamicList chooseHearings;
    @CCD(
            label = "When was the hearing adjourned or vacated?",
            hint = "This is the date the hearing was adjourned or vacated by the listing team.",
            searchable = false
    )
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate vacateHearingDate;
    @CCD(
            label = "Why is the hearing being adjourned or vacated?",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_vacateOrAdjournHearingReason"
    )
    private VacateOrAdjournReason vacateReason;
    @CCD(
            label = "Other reason",
            showCondition = "vacateReason=\"Other\"",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    private String specifyOtherReason;
}
