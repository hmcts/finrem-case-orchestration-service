package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AccessCodeCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AccessCodeEntry;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class InvalidateAccessCodeServiceTest {

    private InvalidateAccessCodeService service;

    @BeforeEach
    void setUp() {
        service = new InvalidateAccessCodeService();
    }

    @Test
    void shouldMergeIsValidFromCurrentWhenIdsMatch() {
        UUID id = UUID.randomUUID();

        AccessCodeCollection before = collection(
            id,
            entry("ABC123", nowMinusDays(2), nowPlusDays(5), YesOrNo.YES)
        );

        AccessCodeCollection current = collection(
            id,
            entry("DIFFERENT", nowMinusDays(1), nowPlusDays(1), YesOrNo.NO)
        );

        List<AccessCodeCollection> result =
            service.mergeForInvalidation(List.of(before), List.of(current));

        AccessCodeEntry merged = result.getFirst().getValue();

        assertThat(merged.getAccessCode()).isEqualTo("ABC123");
        assertThat(merged.getCreatedAt()).isEqualTo(before.getValue().getCreatedAt());
        assertThat(merged.getValidUntil()).isEqualTo(before.getValue().getValidUntil());
        assertThat(merged.getIsValid()).isEqualTo(YesOrNo.NO);
    }

    @Test
    void shouldRetainBeforeItemWhenNoMatchingCurrentExists() {
        UUID id = UUID.randomUUID();

        AccessCodeCollection before = collection(
            id,
            entry("XYZ999", nowMinusDays(1), nowPlusDays(3), YesOrNo.YES)
        );

        List<AccessCodeCollection> result =
            service.mergeForInvalidation(List.of(before), List.of());

        assertThat(result)
            .hasSize(1)
            .containsExactly(before);
    }

    @Test
    void shouldMergeMultipleItemsAndPreserveOrderByCreatedAtDescending() {
        UUID firstId = UUID.randomUUID();
        UUID secondId = UUID.randomUUID();

        AccessCodeCollection older = collection(
            firstId,
            entry("OLD", nowMinusDays(5), nowPlusDays(1), YesOrNo.YES)
        );

        AccessCodeCollection newer = collection(
            secondId,
            entry("NEW", nowMinusDays(1), nowPlusDays(1), YesOrNo.YES)
        );

        AccessCodeCollection updatedNewer = collection(
            secondId,
            entry("IGNORED", now(), nowPlusDays(2), YesOrNo.NO)
        );

        List<AccessCodeCollection> result =
            service.mergeForInvalidation(
                List.of(older, newer),
                List.of(updatedNewer)
            );

        assertThat(result)
            .extracting(c -> c.getValue().getAccessCode())
            .containsExactly("NEW", "OLD");

        assertThat(result.getFirst().getValue().getIsValid()).isEqualTo(YesOrNo.NO);
    }

    @Test
    void shouldSortNullCreatedAtLast() {
        UUID firstId = UUID.randomUUID();
        UUID secondId = UUID.randomUUID();

        AccessCodeCollection withDate = collection(
            firstId,
            entry("DATED", nowMinusDays(1), null, YesOrNo.YES)
        );

        AccessCodeCollection withoutDate = collection(
            secondId,
            entry("NULL", null, null, YesOrNo.YES)
        );

        List<AccessCodeCollection> result =
            service.mergeForInvalidation(
                List.of(withoutDate, withDate),
                List.of()
            );

        assertThat(result)
            .extracting(c -> c.getValue().getAccessCode())
            .containsExactly("DATED", "NULL");
    }

    // -------- helpers --------

    private AccessCodeCollection collection(UUID id, AccessCodeEntry entry) {
        return AccessCodeCollection.builder()
            .id(id)
            .value(entry)
            .build();
    }

    private AccessCodeEntry entry(
        String accessCode,
        LocalDateTime createdAt,
        LocalDateTime validUntil,
        YesOrNo isValid) {

        return AccessCodeEntry.builder()
            .accessCode(accessCode)
            .createdAt(createdAt)
            .validUntil(validUntil)
            .isValid(isValid)
            .build();
    }

    private LocalDateTime now() {
        return LocalDateTime.now();
    }

    private LocalDateTime nowMinusDays(int days) {
        return LocalDateTime.now().minusDays(days);
    }

    private LocalDateTime nowPlusDays(int days) {
        return LocalDateTime.now().plusDays(days);
    }
}
