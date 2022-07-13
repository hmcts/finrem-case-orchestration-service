package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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

    private static final String NOTICE_OF_CHANGE = "Notice of Change";

    public RepresentationUpdateHistory generateRepresentationUpdateHistory(ChangeOfRepresentationRequest
                                                                               changeOfRepresentationRequest) {

        log.info("Updating change of representatives for case.");

        RepresentationUpdateHistory history = Optional.ofNullable(changeOfRepresentationRequest.getCurrent()).map(
                current -> buildNewHistory(current.getRepresentationUpdateHistory()))
            .orElse(RepresentationUpdateHistory.builder().representationUpdateHistory(new ArrayList<>()).build());

        history.getRepresentationUpdateHistory().add(element(UUID.randomUUID(),
            RepresentationUpdate.builder()
                .party(changeOfRepresentationRequest.getParty())
                .clientName(changeOfRepresentationRequest.getClientName())
                .via(NOTICE_OF_CHANGE)
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

    private RepresentationUpdateHistory buildNewHistory(List<Element<RepresentationUpdate>> currentChangeList) {
        return RepresentationUpdateHistory.builder()
            .representationUpdateHistory(Optional.ofNullable(currentChangeList).orElse(new ArrayList<>()))
            .build();
    }

}
