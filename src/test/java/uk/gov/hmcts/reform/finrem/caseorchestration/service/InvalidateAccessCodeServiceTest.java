package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AccessCodeCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AccessCodeEntry;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
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
            entry("ABC123", YesOrNo.YES)
        );

        AccessCodeCollection current = collection(
            id,
            entry("DIFFERENT", YesOrNo.NO, "user-id")
        );

        LocalDateTime beforeCall = LocalDateTime.now(ZoneOffset.UTC);

        List<AccessCodeCollection> result =
            service.mergeForInvalidation(List.of(before), List.of(current));

        LocalDateTime afterCall = LocalDateTime.now(ZoneOffset.UTC);

        AccessCodeEntry merged = result.getFirst().getValue();

        assertThat(merged.getAccessCode()).isEqualTo("ABC123");
        assertThat(merged.getIsValid()).isEqualTo(YesOrNo.NO);
        assertThat(merged.getUserIdamID()).isEqualTo("user-id");
        assertThat(merged.getUsedAt()).isAfterOrEqualTo(beforeCall);
        assertThat(merged.getUsedAt()).isBeforeOrEqualTo(afterCall);
    }

    @Test
    void shouldSetUsedAtDateTime() {
        UUID id = UUID.randomUUID();

        LocalDateTime usedAtBefore = LocalDateTime.now().minusDays(1);
        AccessCodeCollection before = collection(
            id,
            entry("ABC123", YesOrNo.NO)
        );

        AccessCodeCollection current = collection(
            id,
            entry("DIFFERENT", YesOrNo.NO, "current-user-id", LocalDateTime.now())
        );

        LocalDateTime beforeCall = LocalDateTime.now(ZoneOffset.UTC);

        List<AccessCodeCollection> result =
            service.mergeForInvalidation(List.of(before), List.of(current));

        LocalDateTime afterCall = LocalDateTime.now(ZoneOffset.UTC);

        AccessCodeEntry merged = result.getFirst().getValue();

        assertThat(merged.getIsValid()).isEqualTo(YesOrNo.NO);
        assertThat(merged.getUsedAt()).isAfterOrEqualTo(beforeCall);
        assertThat(merged.getUsedAt()).isBeforeOrEqualTo(afterCall);
        assertThat(merged.getUsedAt()).isNotEqualTo(usedAtBefore);
    }

    @Test
    void shouldRetainBeforeItemWhenNoMatchingCurrentExists() {
        UUID id = UUID.randomUUID();

        AccessCodeCollection before = collection(
            id,
            entry("XYZ999", YesOrNo.YES, "before-user-id")
        );

        List<AccessCodeCollection> result =
            service.mergeForInvalidation(List.of(before), List.of());

        assertThat(result)
            .hasSize(1)
            .containsExactly(before);
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
        YesOrNo isValid) {

        return entry(accessCode, isValid, null);
    }

    private AccessCodeEntry entry(
        String accessCode,
        YesOrNo isValid,
        String userIdamId) {

        return entry(accessCode, isValid, userIdamId, null);
    }

    private AccessCodeEntry entry(
        String accessCode,
        YesOrNo isValid,
        String userIdamId,
        LocalDateTime usedAt) {

        return AccessCodeEntry.builder()
            .accessCode(accessCode)
            .isValid(isValid)
            .userIdamID(userIdamId)
            .usedAt(usedAt)
            .build();
    }
}
