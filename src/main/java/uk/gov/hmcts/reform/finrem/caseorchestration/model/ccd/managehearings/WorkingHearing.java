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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;

import java.time.LocalDate;
import java.util.List;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.HearingType.getHearingType;

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
            .partiesOnCase(workingHearing.getPartiesOnCaseMultiSelectList().getValue().stream()
                .map(element -> PartyOnCaseCollectionItem.builder()
                    .value(PartyOnCase.builder()
                        .role(element.getCode())
                        .label(element.getLabel())
                        .build())
                    .build())
                .toList())
            .hearingType(getHearingType(workingHearing.getHearingTypeDynamicList()))
            .build();
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

    public static DynamicList initialiseHearingTypeDynamicListWithSelection(List<HearingType> hearingTypes, HearingType selectedHearingType) {
        DynamicList dynamicList = initialiseHearingTypeDynamicList(hearingTypes);
        dynamicList.setValue(DynamicListElement.builder()
            .code(selectedHearingType.name())
            .label(selectedHearingType.getId())
            .build());
        return dynamicList;
    }

    public static DynamicMultiSelectList initialisePartiesOnCaseMultiSelectList(List<PartyOnCaseCollectionItem> partiesOnCase) {
        List<DynamicMultiSelectListElement> listElements = partiesOnCase.stream()
            .map(party -> DynamicMultiSelectListElement.builder()
                .code(party.getValue().getRole())
                .label(party.getValue().getLabel())
                .build())
            .toList();

        return DynamicMultiSelectList.builder()
            .listItems(listElements)
            .build();
    }
}
