package uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.letters;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RepresentationUpdate;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RepresentationUpdateHolder;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.letters.processors.NocSolicitorAddedLettersProcessor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.REPRESENTATION_UPDATE_HISTORY;

@Service
@Slf4j
@RequiredArgsConstructor
public class NocLetterNotificationService {

    private final NocSolicitorAddedLettersProcessor nocSolicitorAddedLettersProcessor;
    private final NocSolicitorAddedLettersProcessor nocSolicitorRemovedLettersProcessor;

    public void sendNoticeOfChangeLetters(CaseDetails caseDetails, String authToken) {

        log.info("Send noc letters for case id {}", caseDetails.getId());
        RepresentationUpdate representationUpdate = getRepresentationUpdate(caseDetails);
        log.info("Got the representationUpdate");
        if (representationUpdate.getAdded() != null) {
            log.info("The representationUpdate is for an Added solicitor");
            nocSolicitorAddedLettersProcessor.processSolicitorAndLitigantLetters(caseDetails, authToken, representationUpdate);
        }
        if (representationUpdate.getRemoved() != null) {
            log.info("The representationUpdate is for an removed solicitor");
            nocSolicitorRemovedLettersProcessor.processSolicitorAndLitigantLetters(caseDetails, authToken, representationUpdate);
        }
    }

    private RepresentationUpdate getRepresentationUpdate(CaseDetails caseDetails) {
        RepresentationUpdate representationUpdate = Optional.ofNullable(caseDetails.getData().get(REPRESENTATION_UPDATE_HISTORY))
            .map(this::convertTofRepresentationUpdateHolder)
            .orElse(new ArrayList<>())
            .stream()
            .map(RepresentationUpdateHolder::getRepresentationUpdate)
            .max(Comparator.comparing(RepresentationUpdate::getDate))
            .orElse(null);
        return representationUpdate;
    }

    private List<RepresentationUpdateHolder> convertTofRepresentationUpdateHolder(Object object) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper.convertValue(object, new TypeReference<List<RepresentationUpdateHolder>>() {
        });
    }

}
