package uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.letters;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangeOfRepresentation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangeOfRepresentationDataHolder;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.letters.processors.NocSolicitorAddedLettersProcessor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CHANGE_OF_REPRESENTATIVES;

@Service
@Slf4j
@RequiredArgsConstructor
public class NocLetterNotificationService {

    private final NocSolicitorAddedLettersProcessor nocSolicitorAddedLettersProcessor;
    private final NocSolicitorAddedLettersProcessor nocSolicitorRemovedLettersProcessor;

    public void sendNoticeOfChangeLetters(CaseDetails caseDetails, String authToken) {

        log.info("Send noc letters for case id {}", caseDetails.getId() );
        ChangeOfRepresentation changeOfRepresentation = getChangeOfRepresentation(caseDetails);
        log.info("Got the changeOfRepresentation");
        if (changeOfRepresentation.getAdded() != null) {
            log.info("The changeOfRepresentation is for an Added solicitor");
            nocSolicitorAddedLettersProcessor.processSolicitorAndLitigantLetters(caseDetails, authToken, changeOfRepresentation);
        }
        if (changeOfRepresentation.getRemoved() != null) {
            log.info("The changeOfRepresentation is for an removed solicitor");
            nocSolicitorRemovedLettersProcessor.processSolicitorAndLitigantLetters(caseDetails, authToken, changeOfRepresentation);
        }
    }

    private ChangeOfRepresentation getChangeOfRepresentation(CaseDetails caseDetails) {
        ChangeOfRepresentation changeOfRepresentation = Optional.ofNullable(caseDetails.getData().get(CHANGE_OF_REPRESENTATIVES))
            .map(this::convertToChangeOfRepresentation)
            .orElse(new ArrayList<>())
            .stream()
            .map(ChangeOfRepresentationDataHolder::getChangeOfRepresentation)
            .max(Comparator.comparing(ChangeOfRepresentation::getDate))
            .orElse(null);
        return changeOfRepresentation;
    }

    private List<ChangeOfRepresentationDataHolder> convertToChangeOfRepresentation(Object object) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper.convertValue(object, new TypeReference<List<ChangeOfRepresentationDataHolder>>() {
        });
    }

}
