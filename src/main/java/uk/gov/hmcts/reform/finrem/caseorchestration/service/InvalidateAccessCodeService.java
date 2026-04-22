package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AccessCodeCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AccessCodeEntry;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;
import static java.util.Comparator.nullsLast;

/**
 * Service responsible for merging access code collections during invalidation.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InvalidateAccessCodeService {

    /**
     * Merges an existing CCD access code collection with an updated collection
     * where an access code has been invalidated.
     *
     * <p>
     * Invalidation is triggered from the Citizen UI rather than Manage Cases.
     * The Citizen UI submits only the updated access code, not the full collection.
     * Since CCD replaces the entire collection on update, merging is required to
     * prevent other access codes from being unintentionally deleted.
     *
     * <p>
     * All existing access codes are preserved and only the {@code isValid} flag
     * is updated for matching entries, identified by access code ID.
     *
     * <p>
     * The resulting collection is sorted by creation date in descending order.
     *
     * @param before  the original access code collection stored in CCD
     * @param current the updated access code collection received from the Citizen UI
     * @return a merged collection safe to persist back to CCD
     */
    public List<AccessCodeCollection> mergeForInvalidation(List<AccessCodeCollection> before, List<AccessCodeCollection> current) {

        Map<UUID, AccessCodeCollection> currentById =
            current.stream()
                .filter(c -> c.getId() != null)
                .collect(Collectors.toMap(
                    AccessCodeCollection::getId,
                    Function.identity()
                ));

        List<AccessCodeCollection> merged = new ArrayList<>();

        for (AccessCodeCollection beforeItem : before) {
            AccessCodeCollection updated = currentById.get(beforeItem.getId());

            if (updated != null) {
                merged.add(mergeSingle(beforeItem, updated));
            } else {
                merged.add(beforeItem);
            }
        }

        merged.sort(comparing(
            AccessCodeCollection::getValue,
            comparing(AccessCodeEntry::getCreatedAt, nullsLast(Comparator.reverseOrder()))
        ));

        return merged;
    }

    /**
     * Merges a single access code entry.
     *
     * <p>
     * All access code details are preserved from the original entry.
     * Only the {@code isValid} flag is applied from the updated entry.
     *
     * @param beforeItem the original access code entry
     * @param updated    the updated access code entry containing the new validity value
     * @return a merged access code entry with updated validity
     */
    private AccessCodeCollection mergeSingle(AccessCodeCollection beforeItem, AccessCodeCollection updated) {

        AccessCodeEntry beforeValue = beforeItem.getValue();
        AccessCodeEntry updatedValue = updated.getValue();

        return AccessCodeCollection.builder()
            .id(beforeItem.getId())
            .value(AccessCodeEntry.builder()
                .accessCode(beforeValue.getAccessCode())
                .createdAt(beforeValue.getCreatedAt())
                .validUntil(beforeValue.getValidUntil())
                .isValid(updatedValue.getIsValid())
                .build())
            .build();
    }
}
