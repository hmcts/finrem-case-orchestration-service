package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AccessCodeCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AccessCodeEntry;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;

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
            entry("DIFFERENT", YesOrNo.NO)
        );

        List<AccessCodeCollection> result =
            service.mergeForInvalidation(List.of(before), List.of(current));

        AccessCodeEntry merged = result.getFirst().getValue();

        assertThat(merged.getAccessCode()).isEqualTo("ABC123");
        assertThat(merged.getIsValid()).isEqualTo(YesOrNo.NO);
    }

    @Test
    void shouldRetainBeforeItemWhenNoMatchingCurrentExists() {
        UUID id = UUID.randomUUID();

        AccessCodeCollection before = collection(
            id,
            entry("XYZ999", YesOrNo.YES)
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

        return AccessCodeEntry.builder()
            .accessCode(accessCode)
            .isValid(isValid)
            .build();
    }
}
