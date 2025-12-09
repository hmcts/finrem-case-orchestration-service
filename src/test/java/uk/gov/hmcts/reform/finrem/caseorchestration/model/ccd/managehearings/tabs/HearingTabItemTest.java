package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.tabs;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory.HEARING_NOTICES;

@ExtendWith(MockitoExtension.class)
class HearingTabItemTest {

    @Test
    void shouldMarkHearingDocumentsAsSystemDuplicates() {

        var documentItem = DocumentCollectionItem.builder()
            .value(CaseDocument
                .builder()
                .categoryId(HEARING_NOTICES.getDocumentCategoryId())
                .build())
            .build();

        var originalHearingTabItem = HearingTabItem.builder()
            .tabHearingType("TypeA")
            .tabCourtSelection("CourtX")
            .tabAttendance("Present")
            .tabDateTime("2024-06-01T10:00")
            .tabTimeEstimate("1h")
            .tabConfidentialParties("None")
            .tabAdditionalInformation("Info")
            .tabHearingDocuments(List.of(documentItem))
            .tabWasMigrated(YesOrNo.YES)
            .build();

        var duplicatedHearingTabItem = HearingTabItem.fromHearingTabItemMarkDuplicateDocs(originalHearingTabItem);
        var duplicatedDocumentItem = duplicatedHearingTabItem.getTabHearingDocuments().getFirst();

        assertThat(duplicatedHearingTabItem.getTabHearingDocuments()).hasSize(1);
        assertThat(duplicatedDocumentItem.getValue().getCategoryId())
            .isEqualTo(DocumentCategory.SYSTEM_DUPLICATES.getDocumentCategoryId());

        assertThat(originalHearingTabItem.getTabHearingDocuments().getFirst().getValue().getCategoryId())
            .isEqualTo(HEARING_NOTICES.getDocumentCategoryId());

        assertThat(duplicatedHearingTabItem)
            .usingRecursiveComparison()
            .ignoringFields("tabHearingDocuments")
            .isEqualTo(originalHearingTabItem);
    }
}
