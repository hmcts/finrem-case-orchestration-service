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

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HearingTabItem {
    private String tabHearingType;
    private String tabCourtSelection;
    private String tabAttendance;
    private String tabDateTime;
    private String tabTimeEstimate;
    private String tabConfidentialParties;
    private String tabAdditionalInformation;
    private List<DocumentCollectionItem> tabHearingDocuments;
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
