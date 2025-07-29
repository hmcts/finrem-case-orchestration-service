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

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.WorkingHearing.initialiseHearingTypeDynamicListWithSelection;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.WorkingHearing.initialisePartiesOnCaseMultiSelectList;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Hearing {

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

    public static WorkingHearing mapHearingToWorkingHearing(Hearing hearing, List<HearingType> hearingTypes) {
        return WorkingHearing.builder()
            .hearingDate(hearing.getHearingDate())
            .hearingTimeEstimate(hearing.getHearingTimeEstimate())
            .hearingTime(hearing.getHearingTime())
            .hearingCourtSelection(hearing.getHearingCourtSelection())
            .hearingMode(hearing.getHearingMode())
            .additionalHearingInformation(hearing.getAdditionalHearingInformation())
            .hearingNoticePrompt(hearing.getHearingNoticePrompt())
            .additionalHearingDocPrompt(hearing.getAdditionalHearingDocPrompt())
            .additionalHearingDocs(hearing.getAdditionalHearingDocs())
            .partiesOnCaseMultiSelectList(initialisePartiesOnCaseMultiSelectList(hearing.getPartiesOnCase()))
            .hearingTypeDynamicList(initialiseHearingTypeDynamicListWithSelection(hearingTypes, hearing.getHearingType()))
            .build();
    }
}
