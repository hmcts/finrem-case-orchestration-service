package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangeOfRepresentationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Element;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RepresentationUpdate;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RepresentationUpdateHistory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Element.element;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ChangeOfRepresentationService {

    public static final String NOTICE_OF_CHANGE = "Notice of Change";

    public RepresentationUpdateHistory generateRepresentationUpdateHistory(
        ChangeOfRepresentationRequest changeOfRepresentationRequest) {
        return generateRepresentationUpdateHistory(changeOfRepresentationRequest, null);
    }

    /**
     * Generates an updated {@link RepresentationUpdateHistory} based on the provided
     * {@link ChangeOfRepresentationRequest}.
     *
     * <p>This method takes the current representation history (if any) and adds a new entry
     * reflecting the changes specified in {@code changeOfRepresentationRequest}. The new entry
     * includes the party, client name, the event source ({@code via}), the user who made the change,
     * the current date, and the representatives added or removed.</p>
     *
     * <p>The updated history is sorted by the date of each entry before being returned.</p>
     *
     * @param changeOfRepresentationRequest  the request containing details of the representation changes
     * @param viaEventType            the event type that triggered this update
     * @return                                an updated {@link RepresentationUpdateHistory} including the new change
     */
    public RepresentationUpdateHistory generateRepresentationUpdateHistory(
        ChangeOfRepresentationRequest changeOfRepresentationRequest, EventType viaEventType) {

        log.info("Updating change of representatives for case.");

        RepresentationUpdateHistory history = Optional.ofNullable(changeOfRepresentationRequest.getCurrent()).map(
                current -> buildNewHistory(current.getRepresentationUpdateHistory()))
            .orElse(RepresentationUpdateHistory.builder().representationUpdateHistory(new ArrayList<>()).build());

        history.getRepresentationUpdateHistory().add(element(UUID.randomUUID(),
            RepresentationUpdate.builder()
                .party(changeOfRepresentationRequest.getParty())
                .clientName(changeOfRepresentationRequest.getClientName())
                .via(describeViaEventType(viaEventType))
                .by(changeOfRepresentationRequest.getBy())
                .date(LocalDateTime.now())
                .added(changeOfRepresentationRequest.getAddedRepresentative())
                .removed(changeOfRepresentationRequest.getRemovedRepresentative())
                .build()
        ));

        log.info("Updated change of representatives: {}", history);

        history.getRepresentationUpdateHistory().sort(Comparator.comparing(element -> element.getValue().getDate()));

        return history;
    }

    private String describeViaEventType(EventType viaEventType) {
        if (EventType.STOP_REPRESENTING_CLIENT.equals(viaEventType)) {
            return "Stop representing a client";
        } else {
            return NOTICE_OF_CHANGE;
        }
    }

    private RepresentationUpdateHistory buildNewHistory(List<Element<RepresentationUpdate>> currentChangeList) {
        return RepresentationUpdateHistory.builder()
            .representationUpdateHistory(Optional.ofNullable(currentChangeList).orElse(new ArrayList<>()))
            .build();
    }

}
