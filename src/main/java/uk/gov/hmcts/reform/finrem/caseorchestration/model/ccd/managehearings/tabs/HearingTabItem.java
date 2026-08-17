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

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory.SYSTEM_DUPLICATES;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "FR_manageHearingTabItem", generate = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HearingTabItem {
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

    /**
     * Creates a new HearingTabItem with all fields copied from the provided hearingTabItem,
     * and marks all hearing documents as duplicates. The documents are deep-copied.
     *
     * @param hearingTabItem the source HearingTabItem
     * @return a new HearingTabItem with duplicated documents catagorised as SYSTEM_DUPLICATES
     */
    public static HearingTabItem  fromHearingTabItemMarkDuplicateDocs(HearingTabItem hearingTabItem) {
        List<DocumentCollectionItem> duplicatedDocuments = hearingTabItem.getTabHearingDocuments().stream()
            .map(documentCollectionItem -> {
                var documentValueCopy = documentCollectionItem.getValue().toBuilder()
                    .categoryId(SYSTEM_DUPLICATES.getDocumentCategoryId())
                    .build();
                return DocumentCollectionItem.builder()
                    .value(documentValueCopy)
                    .build();
            })
            .toList();

        return HearingTabItem.builder()
            .tabHearingType(hearingTabItem.getTabHearingType())
            .tabCourtSelection(hearingTabItem.getTabCourtSelection())
            .tabAttendance(hearingTabItem.getTabAttendance())
            .tabDateTime(hearingTabItem.getTabDateTime())
            .tabTimeEstimate(hearingTabItem.getTabTimeEstimate())
            .tabConfidentialParties(hearingTabItem.getTabConfidentialParties())
            .tabAdditionalInformation(hearingTabItem.getTabAdditionalInformation())
            .tabHearingDocuments(duplicatedDocuments)
            .tabWasMigrated(hearingTabItem.getTabWasMigrated())
            .build();
    }
}
