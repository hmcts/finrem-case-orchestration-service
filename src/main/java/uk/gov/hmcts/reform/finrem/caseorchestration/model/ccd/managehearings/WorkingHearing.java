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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;

import java.time.LocalDate;
import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WorkingHearing {
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate hearingDate;
    private DynamicList hearingTypeDynamicList;
    private String hearingTimeEstimate;
    private String hearingTime;
    private Court hearingCourtSelection;
    private HearingMode hearingMode;
    private String additionalHearingInformation;
    private YesOrNo hearingNoticePrompt;
    private YesOrNo additionalHearingDocPrompt;
    private List<DocumentCollectionItem> additionalHearingDocs;
    private DynamicMultiSelectList partiesOnCaseMultiSelectList;

    public static Hearing transformHearingInputsToHearing(WorkingHearing workingHearing) {
        return Hearing.builder()
            .hearingDate(workingHearing.getHearingDate())
            .hearingTimeEstimate(workingHearing.getHearingTimeEstimate())
            .hearingTime(workingHearing.getHearingTime())
            .hearingCourtSelection(workingHearing.getHearingCourtSelection())
            .hearingMode(workingHearing.getHearingMode())
            .additionalHearingInformation(workingHearing.getAdditionalHearingInformation())
            .hearingNoticePrompt(workingHearing.getHearingNoticePrompt())
            .additionalHearingDocPrompt(workingHearing.getAdditionalHearingDocPrompt())
            .additionalHearingDocs(workingHearing.getAdditionalHearingDocs())
            .partiesOnCaseMultiSelectList(workingHearing.getPartiesOnCaseMultiSelectList())
            .hearingType(getHearingType(workingHearing.getHearingTypeDynamicList()))
            .build();
    }

    public static HearingType getHearingType(DynamicList hearingTypeDynamicList) {
        return HearingType.valueOf(hearingTypeDynamicList.getValue().getCode());
    }

    public static DynamicList initialiseHearingTypeDynamicList(List<HearingType> hearingTypes) {
        List<DynamicListElement> listElements = hearingTypes.stream()
            .map(hearingType -> DynamicListElement.builder()
                .code(hearingType.name())
                .label(hearingType.getId())
                .build())
            .toList();

        return DynamicList.builder()
            .listItems(listElements)
            .build();
    }
}
