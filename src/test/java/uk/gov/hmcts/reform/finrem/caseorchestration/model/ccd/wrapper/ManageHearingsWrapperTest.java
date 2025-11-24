package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.hearings.Hearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.hearings.ManageHearingsCollectionItem;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class ManageHearingsWrapperTest {

    @Test
    void shouldReturnMatchingItemById() {
        UUID matchingId = UUID.randomUUID();
        ManageHearingsCollectionItem itemToMatch = ManageHearingsCollectionItem.builder()
            .id(matchingId)
            .value(new Hearing())
            .build();

        ManageHearingsWrapper wrapper = ManageHearingsWrapper.builder()
            .hearings(List.of(
                ManageHearingsCollectionItem.builder().id(UUID.randomUUID()).build(),
                itemToMatch,
                ManageHearingsCollectionItem.builder().id(UUID.randomUUID()).build()
            ))
            .build();

        ManageHearingsCollectionItem result = wrapper.getManageHearingsCollectionItemById(matchingId);

        assertNotNull(result);
        assertEquals(matchingId, result.getId());
    }

    @Test
    void shouldReturnNullWhenNoMatchFound() {
        ManageHearingsWrapper wrapper = ManageHearingsWrapper.builder()
            .hearings(List.of(
                ManageHearingsCollectionItem.builder().id(UUID.randomUUID()).build()
            ))
            .build();

        ManageHearingsCollectionItem result = wrapper.getManageHearingsCollectionItemById(UUID.randomUUID());

        assertNull(result);
    }

    @Test
    void shouldReturnNullWhenHearingsListIsNull() {
        ManageHearingsWrapper wrapper = ManageHearingsWrapper.builder()
            .hearings(null)
            .build();

        ManageHearingsCollectionItem result = wrapper.getManageHearingsCollectionItemById(UUID.randomUUID());

        assertNull(result);
    }
}
