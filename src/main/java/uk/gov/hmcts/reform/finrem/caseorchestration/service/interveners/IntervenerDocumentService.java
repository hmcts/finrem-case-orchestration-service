package uk.gov.hmcts.reform.finrem.caseorchestration.service.interveners;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerChangeDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;

@Service
@RequiredArgsConstructor
@Slf4j
public class IntervenerDocumentService {

    private final GenericDocumentService genericDocumentService;
    private final DocumentConfiguration documentConfiguration;
    private final DocumentHelper documentHelper;

    public CaseDocument generateIntervenerAddedNotificationLetter(FinremCaseDetails caseDetails, String authToken,
                                                                  DocumentHelper.PaperNotificationRecipient recipient) {

        log.info("Generating Intervener Added Notification Letter {} from {} for bulk print for {}",
            documentConfiguration.getIntervenerAddedTemplate(),
            documentConfiguration.getIntervenerAddedFilename(),
            recipient);

        CaseDetails caseDetailsForBulkPrint = documentHelper.prepareLetterTemplateData(caseDetails, recipient);
        //intervenerADdedLetterDetails
        CaseDocument generateIntervenerAddedNotificationLetter =
            getCaseDocument(authToken, caseDetailsForBulkPrint);

        return generateIntervenerAddedNotificationLetter;
    }

    private CaseDocument getCaseDocument(String authToken, CaseDetails caseDetailsForBulkPrint) {

        CaseDocument generatedIntervenerAddedNotificationLetter = genericDocumentService.generateDocument(authToken,
            caseDetailsForBulkPrint,
            documentConfiguration.getIntervenerAddedTemplate(),
            documentConfiguration.getIntervenerAddedFilename());

        log.info("Generated Assigned To Judge Notification Letter: {}", generatedIntervenerAddedNotificationLetter);
        return generatedIntervenerAddedNotificationLetter;
    }

    private Pair<String, String> getTemplateFilenamePair(IntervenerChangeDetails changeDetails) {
        return changeDetails.equals(IntervenerChangeDetails.IntervenerAction.ADDED)
            ? Pair.of(documentConfiguration.getIntervenerAddedTemplate(), documentConfiguration.getIntervenerAddedFilename())
            : Pair.of(documentConfiguration.getIntervenerRemovedTemplate(), documentConfiguration.getIntervenerRemovedFilename());
    }
}
