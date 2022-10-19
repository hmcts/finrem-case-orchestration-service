package uk.gov.hmcts.reform.finrem.caseorchestration.service.barristers;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.BarristerChangeType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.BarristerLetterTuple;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.barristers.BarristerLetterDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Barrister;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;

import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.RESPONDENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CASE_DATA;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CASE_DETAILS;

@Service
@RequiredArgsConstructor
@Slf4j
public class BarristerLetterService {

    private final BarristerLetterDetailsGenerator barristerLetterDetailsGenerator;
    private final BulkPrintService bulkPrintService;
    private final CaseDataService caseDataService;
    private final DocumentConfiguration documentConfiguration;
    private final GenericDocumentService genericDocumentService;
    private final ObjectMapper objectMapper;

    public void sendBarristerLetter(CaseDetails caseDetails,
                                    Barrister barrister,
                                    BarristerLetterTuple barristerLetterTuple) {
        log.info("About to start sending barrister access letter for case {}", caseDetails.getId());
        Optional<CaseDocument> barristerLetter = getBarristerLetter(caseDetails, barrister, barristerLetterTuple);
        barristerLetter.ifPresent(letter -> bulkPrintService.sendDocumentForPrint(letter, caseDetails));
    }

    private Optional<CaseDocument> getBarristerLetter(CaseDetails caseDetails,
                                                      Barrister barrister,
                                                      BarristerLetterTuple barristerLetterTuple) {
        Map<String, Object> caseData = caseDetails.getData();

        if (letterShouldNotBeSent(barristerLetterTuple.getRecipient(), caseData)) {
            log.info("{} is represented for case {}, no letter to send",
                barristerLetterTuple.getRecipient().toString(), caseDetails.getId());
            return Optional.empty();
        }

        BarristerLetterDetails barristerLetterDetails = barristerLetterDetailsGenerator
            .generate(caseDetails, barristerLetterTuple.getRecipient(), barrister);
        Pair<String, String> documentData = getTemplateFilenamePair(barristerLetterTuple.getChangeType());

        log.info("Sending {} letter for case {}", documentData.getRight(), caseDetails.getId());

        return generateDocument(barristerLetterTuple.getAuthToken(), barristerLetterDetails,
            documentData.getLeft(), documentData.getRight());
    }

    private Optional<CaseDocument> generateDocument(String authToken,
                                                    BarristerLetterDetails barristerLetterDetails,
                                                    String template,
                                                    String filename) {
        return Optional.of(genericDocumentService.generateDocumentFromPlaceholdersMap(
            authToken,
            convertLetterDetailsToMap(barristerLetterDetails),
            template,
            filename));
    }

    private boolean letterShouldNotBeSent(DocumentHelper.PaperNotificationRecipient recipient,
                                          Map<String, Object> caseData) {
        return (recipient == RESPONDENT && caseDataService.isRespondentRepresentedByASolicitor(caseData))
            || (recipient == APPLICANT && caseDataService.isApplicantRepresentedByASolicitor(caseData));
    }

    private Pair<String, String> getTemplateFilenamePair(BarristerChangeType changeType) {
        return changeType.equals(BarristerChangeType.ADDED)
            ? Pair.of(documentConfiguration.getBarristerAddedTemplate(), documentConfiguration.getBarristerAddedFilename())
            : Pair.of(documentConfiguration.getBarristerRemovedTemplate(), documentConfiguration.getBarristerRemovedFilename());
    }

    private Map<String, Object> convertLetterDetailsToMap(BarristerLetterDetails letterDetails) {
        Map<String, Object> caseDetailsMap = Map.of(CASE_DATA, objectMapper.convertValue(letterDetails, Map.class));
        return Map.of(CASE_DETAILS, caseDetailsMap);
    }
}
