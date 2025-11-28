package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.hearings;

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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.HearingMode;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.HearingType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.ManageHearingsAction;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.PartyOnCaseCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.VacateOrAdjournReason;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.WorkingVacatedHearing;

import java.time.LocalDate;
import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VacateOrAdjournedHearing implements HearingLike {

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

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate vacatedOrAdjournedDate;
    private VacateOrAdjournReason vacateOrAdjournReason;
    private String specifyOtherReason;
    private ManageHearingsAction hearingStatus;

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
            .vacatedOrAdjournedDate(vacateHearingInput.getVacateHearingDate())
            .vacateOrAdjournReason(vacateHearingInput.getVacateReason())
            .hearingStatus(ManageHearingsAction.VACATE_HEARING)
            .specifyOtherReason(vacateHearingInput.getSpecifyOtherReason())
            .build();
    }
}
