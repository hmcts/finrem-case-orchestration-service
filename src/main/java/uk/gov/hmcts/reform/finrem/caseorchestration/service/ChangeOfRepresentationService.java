package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangeOfRepresentationHistory;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangeOfRepresentationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Element;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RepresentationUpdate;

import java.time.LocalDate;
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

    private static final String NOTICE_OF_CHANGE = "Notice of Change";

    public ChangeOfRepresentationHistory generateChangeOfRepresentatives(ChangeOfRepresentationRequest
                                                                       changeOfRepresentationRequest) {

        log.info("Updating change of representatives for case.");

        ChangeOfRepresentationHistory change = Optional.ofNullable(changeOfRepresentationRequest.getCurrent()).map(
            current -> buildNewHistory(current.getRepresentationUpdates()))
            .orElse(ChangeOfRepresentationHistory.builder().representationUpdates(new ArrayList<>()).build());

        change.getRepresentationUpdates().add(element(UUID.randomUUID(),
            RepresentationUpdate.builder()
                .party(changeOfRepresentationRequest.getParty())
                .clientName(changeOfRepresentationRequest.getClientName())
                .via(NOTICE_OF_CHANGE)
                .by(changeOfRepresentationRequest.getBy())
                .date(LocalDate.now())
                .added(changeOfRepresentationRequest.getAddedRepresentative())
                .removed(changeOfRepresentationRequest.getRemovedRepresentative())
                .build()
        ));
        log.info("Updated change of representatives: {}", change);

        change.getRepresentationUpdates().sort(Comparator.comparing(element -> element.getValue().getDate()));

        return change;
    }

    private ChangeOfRepresentationHistory buildNewHistory(List<Element<RepresentationUpdate>> currentChangeList) {
        return  ChangeOfRepresentationHistory.builder()
            .representationUpdates(Optional.ofNullable(currentChangeList).orElse(new ArrayList<>()))
            .build();
    }

}
