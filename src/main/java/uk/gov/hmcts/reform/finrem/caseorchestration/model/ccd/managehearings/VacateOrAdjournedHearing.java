package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Court;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;

import java.time.LocalDate;
import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VacateOrAdjournedHearing {

    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate hearingDate;
    private HearingType hearingType;
    private String hearingTimeEstimate;
    private String hearingTime;
    private Court hearingCourtSelection;
    private HearingMode hearingMode;
    private String additionalHearingInformation;
    private YesOrNo hearingNoticePrompt;
    private YesOrNo additionalHearingDocPrompt;
    private List<DocumentCollectionItem> additionalHearingDocs;
    private List<PartyOnCaseCollectionItem> partiesOnCase;
    private YesOrNo wasMigrated;
    // TODO: Use ENUM for vacateOrAdjournReason
    private String vacateOrAdjournReason;
    private String specifyOtherReason;
    // TODO: Use Enum for hearingStatus
    private String hearingStatus;

    public static VacateOrAdjournedHearing fromHearingToVacatedHearing(ManageHearingsCollectionItem hearingToVacate,
                                                                       WorkingVacatedHearing vacateHearingInput) {
        Hearing hearing = hearingToVacate.getValue();
        return VacateOrAdjournedHearing.builder()
            .hearingDate(hearing.getHearingDate())
            .hearingType(hearing.getHearingType())
            .hearingTimeEstimate(hearing.getHearingTimeEstimate())
            .hearingTime(hearing.getHearingTime())
            .hearingCourtSelection(hearing.getHearingCourtSelection())
            .hearingMode(hearing.getHearingMode())
            .additionalHearingInformation(hearing.getAdditionalHearingInformation())
            .hearingNoticePrompt(hearing.getHearingNoticePrompt())
            .additionalHearingDocPrompt(hearing.getAdditionalHearingDocPrompt())
            .additionalHearingDocs(hearing.getAdditionalHearingDocs())
            .partiesOnCase(hearing.getPartiesOnCase())
            .wasMigrated(hearing.getWasMigrated())
            .vacateOrAdjournReason(vacateHearingInput.getVacateReason())
            .specifyOtherReason(vacateHearingInput.getSpecifyOtherReason())
            .build();
    }
}
