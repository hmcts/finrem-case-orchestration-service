package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AccessCodeCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AccessCodeEntry;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

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

        return merged;
    }

    /**
     * Merges an updated access code entry with its original details.
     * Preserves the original access code and creation date,
     * updates its validity and IDAM user, and records when
     * the code is first activated.
     *
     * @param beforeItem the original access code entry
     * @param updated    the updated access code entry containing the new validity value
     * @return a merged access code entry with updated validity
     */
    private AccessCodeCollection mergeSingle(AccessCodeCollection beforeItem, AccessCodeCollection updated) {

        AccessCodeEntry beforeValue = beforeItem.getValue();
        AccessCodeEntry updatedValue = updated.getValue();

        String userIdamId = StringUtils.defaultIfBlank(
            updatedValue.getUserIdamID(),
            beforeValue.getUserIdamID()
        );

        boolean newlyActivated =
            YesOrNo.YES.equals(beforeValue.getIsValid())
                && YesOrNo.NO.equals(updatedValue.getIsValid())
                && StringUtils.isNotBlank(updatedValue.getUserIdamID());

        LocalDateTime usedAt = beforeValue.getUsedAt();

        if (usedAt == null && newlyActivated) {
            usedAt = LocalDateTime.now(ZoneOffset.UTC);
        }

        return AccessCodeCollection.builder()
            .id(beforeItem.getId())
            .value(AccessCodeEntry.builder()
                .accessCode(beforeValue.getAccessCode())
                .isValid(updatedValue.getIsValid())
                .userIdamID(userIdamId)
                .usedAt(usedAt)
                .createdAt(beforeValue.getCreatedAt())
                .build())
            .build();
    }
}
