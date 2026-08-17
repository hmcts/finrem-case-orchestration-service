package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.tabs;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "FR_vacatedOrAdjournedHearingTabItems", generate = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VacatedOrAdjournedHearingTabItem {
    @CCD(label = "Type of Hearing", searchable = false)
    private String tabHearingType;
    @CCD(label = "Court", searchable = false)
    private String tabCourtSelection;
    @CCD(label = "Hearing Attendance", searchable = false)
    private String tabAttendance;
    @CCD(label = "Hearing Date", searchable = false)
    private String tabDateTime;
    @CCD(label = "Hearing Time Estimate", searchable = false)
    private String tabTimeEstimate;
    @CCD(label = "Who has received this notice", searchable = false)
    private String tabConfidentialParties;
    @CCD(label = "Additional information about the hearing", searchable = false)
    private String tabAdditionalInformation;
    @CCD(
            label = "Hearing Documents",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "Document"
    )
    private List<DocumentCollectionItem> tabHearingDocuments;
    @CCD(label = "Was Migrated?", typeOverride = FieldType.YesOrNo)
    private YesOrNo tabWasMigrated;
    @CCD(label = "Adjourned or Vacated date")
    private String tabVacatedOrAdjournedDate;
    @CCD(label = "Reason")
    private String tabVacateOrAdjournReason;
    @CCD(label = " ")
    private String tabSpecifyOtherReason;
    @CCD(label = "Hearing status")
    private String tabHearingStatus;
}
