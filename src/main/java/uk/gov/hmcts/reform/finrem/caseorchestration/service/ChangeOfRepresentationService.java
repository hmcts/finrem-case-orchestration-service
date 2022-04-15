package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangeOfRepresentation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangeOfRepresentationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangeOfRepresentatives;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ChangeOfRepresentationService {

    private static final String NOTICE_OF_CHANGE = "Notice of Change";

    public ChangeOfRepresentatives generateChangeOfRepresentatives(ChangeOfRepresentationRequest
                                                                       changeOfRepresentationRequest) {

        ChangeOfRepresentatives change = Optional.ofNullable(changeOfRepresentationRequest.getCurrent())
            .orElse(ChangeOfRepresentatives.builder()
                .changeOfRepresentation(new ArrayList<>())
                .build()
            );

        change.addChangeOfRepresentation(
            ChangeOfRepresentation.builder()
                .party(changeOfRepresentationRequest.getParty())
                .clientName(changeOfRepresentationRequest.getClientName())
                .via(NOTICE_OF_CHANGE)
                .by(changeOfRepresentationRequest.getBy())
                .date(LocalDate.now())
                .added(changeOfRepresentationRequest.getAddedRepresentative())
                .removed(changeOfRepresentationRequest.getRemovedRepresentative())
                .build()
        );

        return change;
    }

}
