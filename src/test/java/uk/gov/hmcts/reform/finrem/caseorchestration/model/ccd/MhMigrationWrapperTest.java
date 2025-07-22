package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.MhMigrationWrapper;

import static org.assertj.core.api.Assertions.assertThat;

class MhMigrationWrapperTest {

    @Test
    void testClearAll() {
        MhMigrationWrapper underTest = MhMigrationWrapper.builder()
            .mhMigrationVersion("v1")
            .isListForHearingsMigrated(YesOrNo.YES)
            .isListForInterimHearingsMigrated(YesOrNo.YES)
            .isGeneralApplicationMigrated(YesOrNo.YES)
            .isDirectionDetailsCollectionMigrated(YesOrNo.YES)
            .build();

        underTest.clearAll();

        assertThat(underTest).extracting(MhMigrationWrapper::getMhMigrationVersion,
            MhMigrationWrapper::getIsListForHearingsMigrated,
            MhMigrationWrapper::getIsListForInterimHearingsMigrated,
            MhMigrationWrapper::getIsGeneralApplicationMigrated,
            MhMigrationWrapper::getIsDirectionDetailsCollectionMigrated)
            .containsOnlyNulls();
    }
}
