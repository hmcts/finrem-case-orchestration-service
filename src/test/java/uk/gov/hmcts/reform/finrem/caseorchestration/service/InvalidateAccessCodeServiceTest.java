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
        LocalDateTime createdAt = LocalDateTime.now(ZoneOffset.UTC).minusDays(5);

        AccessCodeCollection before = collection(id, AccessCodeEntry.builder()
            .accessCode("ABC123")
            .isValid(YesOrNo.YES)
            .createdAt(createdAt)
            .build());

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
        assertThat(merged.getCreatedAt()).isEqualTo(createdAt);
        assertThat(merged.getUsedAt()).isBetween(beforeCall, afterCall);
    }

    @Test
    void shouldPreserveExistingUsedAt() {
        UUID id = UUID.randomUUID();
        LocalDateTime usedAt = LocalDateTime.now(ZoneOffset.UTC).minusDays(1);

        AccessCodeCollection before = collection(
            id,
            entry("ABC123", YesOrNo.NO, "original-user", usedAt)
        );
        AccessCodeCollection current = collection(
            id,
            entry("DIFFERENT", YesOrNo.NO)
        );

        List<AccessCodeCollection> result =
            service.mergeForInvalidation(List.of(before), List.of(current));

        AccessCodeEntry merged = result.getFirst().getValue();

        assertThat(merged.getUsedAt()).isEqualTo(usedAt);
        assertThat(merged.getUserIdamID()).isEqualTo("original-user");
    }

    @Test
    void shouldNotSetUsedAtWhenNoIdamUserProvided() {
        UUID id = UUID.randomUUID();

        AccessCodeCollection before = collection(
            id,
            entry("ABC123", YesOrNo.YES)
        );
        AccessCodeCollection current = collection(
            id,
            entry("ABC123", YesOrNo.NO)
        );

        List<AccessCodeCollection> result =
            service.mergeForInvalidation(List.of(before), List.of(current));

        AccessCodeEntry merged = result.getFirst().getValue();

        assertThat(merged.getIsValid()).isEqualTo(YesOrNo.NO);
        assertThat(merged.getUsedAt()).isNull();
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

        assertThat(result).containsExactly(before);
    }

    // -------- helpers --------

    private AccessCodeCollection collection(UUID id, AccessCodeEntry entry) {
        return AccessCodeCollection.builder()
            .id(id)
            .value(entry)
            .build();
    }

    private AccessCodeEntry entry(String accessCode, YesOrNo isValid) {
        return entry(accessCode, isValid, null);
    }

    private AccessCodeEntry entry(String accessCode, YesOrNo isValid, String userIdamId) {
        return entry(accessCode, isValid, userIdamId, null);
    }

    private AccessCodeEntry entry(String accessCode, YesOrNo isValid, String userIdamId,
                                  LocalDateTime usedAt) {
        return AccessCodeEntry.builder()
            .accessCode(accessCode)
            .isValid(isValid)
            .userIdamID(userIdamId)
            .usedAt(usedAt)
            .build();
    }
}
