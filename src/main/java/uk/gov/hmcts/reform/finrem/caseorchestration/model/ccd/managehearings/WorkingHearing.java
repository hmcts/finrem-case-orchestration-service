package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Court;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static java.util.Optional.ofNullable;
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

    /**
     * Creates a WorkingHearing instance from a Hearing instance.
     * @param hearing the Hearing instance to convert
     * @return a WorkingHearing instance with fields populated from the Hearing instance
     */
    public static WorkingHearing from(Hearing hearing) {
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
            .withPartiesOnCaseSelected(hearing.getPartiesOnCase())
            .withHearingTypes(HearingType.values())
            .withHearingTypeSelected(hearing.getHearingType())
            .build();
    }

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

    public void addDocumentToAdditionalHearingDocs(CaseDocument caseDocument) {
        additionalHearingDocs.add(DocumentCollectionItem.builder().value(caseDocument).build());
    }

    public static class WorkingHearingBuilder {
        private DynamicList hearingTypeDynamicList;
        private DynamicMultiSelectList partiesOnCaseMultiSelectList;

        private DynamicListElement hearingTypeItem(HearingType hearingType) {
            return DynamicListElement.builder()
                .code(hearingType.name())
                .label(hearingType.getId())
                .build();
        }

        public WorkingHearingBuilder withHearingTypes(HearingType... hearingTypes) {
            List<DynamicListElement> listElements = Arrays.stream(hearingTypes)
                .map(this::hearingTypeItem)
                .toList();

            DynamicList list = ofNullable(this.hearingTypeDynamicList)
                .orElse(DynamicList.builder().build());
            list.setListItems(listElements);
            this.hearingTypeDynamicList = list;

            return this;
        }

        public WorkingHearingBuilder withHearingTypeSelected(HearingType hearingType) {
            DynamicList list = ofNullable(this.hearingTypeDynamicList)
                .orElse(DynamicList.builder().build());
            list.setValue(hearingTypeItem(hearingType));
            this.hearingTypeDynamicList = list;

            return this;
        }

        public WorkingHearingBuilder withPartiesOnCaseSelected(List<PartyOnCaseCollectionItem> partiesOnCaseCollectionItems) {
            List<DynamicMultiSelectListElement> listElements = partiesOnCaseCollectionItems.stream()
                .map(PartyOnCaseCollectionItem::getValue)
                .map(partyOnCase -> DynamicMultiSelectListElement.builder()
                    .code(partyOnCase.getRole())
                    .label(partyOnCase.getLabel())
                    .build())
                .toList();

            DynamicMultiSelectList list = ofNullable(this.partiesOnCaseMultiSelectList)
                .orElse(DynamicMultiSelectList.builder().build());
            list.setListItems(listElements);
            this.partiesOnCaseMultiSelectList = list;

            return this;
        }
    }
}
