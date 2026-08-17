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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.PartyOnCaseCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.VacateOrAdjournAction;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.VacateOrAdjournReason;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.WorkingVacatedHearing;

import java.time.LocalDate;
import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "FR_vacateOrAdjournHearing", generate = true)
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VacateOrAdjournedHearing implements HearingLike {

    @CCD(
            label = "Hearing Date",
            hint = "Fast Track: Date of the Fast Track hearing must be between 6 and 10 weeks.\r\nExpress pilot: Date of the express pilot hearing should be between 16 and 20 weeks.\r\nStandard Track: Date of the hearing must be between 12 and 16 weeks\r\n",
            searchable = false
    )
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate hearingDate;
    @CCD(label = "Type of Hearing", searchable = false, typeOverride = FieldType.Text)
    private HearingType hearingType;
    @CCD(label = "Hearing Time Estimate", searchable = false)
    private String hearingTimeEstimate;
    @CCD(label = "Hearing Time", searchable = false)
    private String hearingTime;
    @CCD(label = " ", searchable = false)
    private Court hearingCourtSelection;
    @CCD(
            label = "Hearing Attendance",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_ManageHearingMode"
    )
    private HearingMode hearingMode;
    @CCD(label = "Additional information about the hearing", searchable = false)
    private String additionalHearingInformation;
    @CCD(
            label = "Do you want to send a notice of hearing?",
            hint = "If you select 'yes' then the selected parties for this hearing will receive a hearing notice. If you select 'no', no notices will be sent to any of the selected parties. Only the selected parties will be able to view information relating to this hearing on the portal.",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private YesOrNo hearingNoticePrompt;
    @CCD(label = "Do you want to upload any other documents?", searchable = false, typeOverride = FieldType.YesOrNo)
    private YesOrNo additionalHearingDocPrompt;
    @CCD(
            label = "Please upload any additional documents related to your application.",
            showCondition = "additionalHearingDocPrompt=\"Yes\"",
            categoryID = "hearingNotices",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "Document"
    )
    private List<DocumentCollectionItem> additionalHearingDocs;
    @CCD(
            label = "Who should see this order?",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_partyOnCase"
    )
    private List<PartyOnCaseCollectionItem> partiesOnCase;
    @CCD(label = "Was Migrated?", typeOverride = FieldType.YesOrNo)
    private YesOrNo wasMigrated;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.YesOrNo)
    private YesOrNo wasVacOrAdjNoticeSent;

    @CCD(label = " ", searchable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate vacatedOrAdjournedDate;
    @CCD(
            label = "Why is the hearing being adjourned or vacated?",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_vacateOrAdjournHearingReason"
    )
    private VacateOrAdjournReason vacateOrAdjournReason;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.TextArea)
    private String specifyOtherReason;
    @CCD(label = "Hearing Status", searchable = false, typeOverride = FieldType.Text)
    private VacateOrAdjournAction hearingStatus;

    public boolean shouldSendNotifications() {
        return YesOrNo.YES.equals(this.getWasVacOrAdjNoticeSent());
    }

    public static VacateOrAdjournedHearing fromHearingToVacatedOrAdjourned(ManageHearingsCollectionItem hearingToVacate,
                                                                           WorkingVacatedHearing vacateHearingInput,
                                                                           YesOrNo wasVacateOrAdjournNoticeSent,
                                                                           VacateOrAdjournAction hearingStatus) {
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
            .hearingStatus(hearingStatus)
            .specifyOtherReason(vacateHearingInput.getSpecifyOtherReason())
            .wasVacOrAdjNoticeSent(wasVacateOrAdjournNoticeSent)
            .build();
    }
}
