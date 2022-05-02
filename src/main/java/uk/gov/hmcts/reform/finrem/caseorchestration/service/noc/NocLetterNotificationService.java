package uk.gov.hmcts.reform.finrem.caseorchestration.service.noc;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Element;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RepresentationUpdate;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.letters.NocSolicitorAddedLettersProcessor;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.REPRESENTATION_UPDATE_HISTORY;

@Service
@Slf4j
@RequiredArgsConstructor
public class NocLetterNotificationService {

    private final NocSolicitorAddedLettersProcessor nocSolicitorAddedLettersProcessor;
    private final NocSolicitorAddedLettersProcessor nocSolicitorRemovedLettersProcessor;

    public void sendNoticeOfChangeLetters(CaseDetails caseDetails, String authToken) {

        log.info("Send noc letters for case id {}", caseDetails.getId());
        RepresentationUpdate representationUpdate = getLatestRepresentationUpdate(caseDetails);
        if (representationUpdate != null) {
            log.info("Got the representationUpdate");
            if (representationUpdate.getAdded() != null) {
                log.info("The representationUpdate is for an Added solicitor");
                nocSolicitorAddedLettersProcessor.processSolicitorAndLitigantLetters(caseDetails, authToken, representationUpdate);
            }
            if (representationUpdate.getRemoved() != null) {
                log.info("The representationUpdate is for a Removed solicitor");
                nocSolicitorRemovedLettersProcessor.processSolicitorAndLitigantLetters(caseDetails, authToken, representationUpdate);
            }
        }
    }

    private RepresentationUpdate getLatestRepresentationUpdate(CaseDetails caseDetails) {
        log.info("Get the latest Representation Update");
        List<Element<RepresentationUpdate>> representationUpdates = new ObjectMapper().registerModule(new JavaTimeModule())
            .convertValue(caseDetails.getData().get(REPRESENTATION_UPDATE_HISTORY), new TypeReference<>() {
            });
        return Collections.max(representationUpdates, Comparator.comparing(representationUpdate -> representationUpdate.getValue().getDate()))
            .getValue();
    }

}
