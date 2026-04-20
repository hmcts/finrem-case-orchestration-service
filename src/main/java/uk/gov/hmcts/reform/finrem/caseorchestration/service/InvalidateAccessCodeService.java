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

@Service
@RequiredArgsConstructor
@Slf4j
public class InvalidateAccessCodeService {

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
