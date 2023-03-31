package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.intervener;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.bsp.common.model.document.Addressee;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerAddedLetterDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseHearingFunctions;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.ADDRESSEE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CASE_DATA;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CASE_DETAILS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.documents.generators.AbstractLetterDetailsGenerator.LETTER_DATE_FORMAT;

@Service
@RequiredArgsConstructor
@Slf4j
public class IntervenerDocumentService {

    private final GenericDocumentService genericDocumentService;
    private final DocumentConfiguration documentConfiguration;
    private final DocumentHelper documentHelper;
    private final ObjectMapper objectMapper;

    public CaseDocument generateIntervenerAddedNotificationLetter(FinremCaseDetails finremCaseDetails, String authToken,
                                                                  DocumentHelper.PaperNotificationRecipient recipient) {

        log.info("Generating Intervener Added Notification Letter {} from {} for bulk print for {}",
            documentConfiguration.getIntervenerAddedTemplate(),
            documentConfiguration.getIntervenerAddedFilename(),
            recipient);


        CaseDetails caseDetailsForBulkPrint = documentHelper.prepareLetterTemplateData(finremCaseDetails, recipient);
        finremCaseDetails.getData().setCurrentAddressee((Addressee) caseDetailsForBulkPrint.getData().get(ADDRESSEE));
        IntervenerAddedLetterDetails intervenerAddedLetterDetails = generateLetterDetails(finremCaseDetails);

        return getCaseDocument(authToken, intervenerAddedLetterDetails);
    }

    private CaseDocument getCaseDocument(String authToken, IntervenerAddedLetterDetails intervenerAddedLetterDetails) {

        CaseDocument generatedIntervenerAddedNotificationLetter = generateDocument(authToken,
            intervenerAddedLetterDetails,
            documentConfiguration.getIntervenerAddedTemplate(),
            documentConfiguration.getIntervenerAddedFilename());

        log.info("Generated Intervener Added Notification Letter: {}", generatedIntervenerAddedNotificationLetter);
        return generatedIntervenerAddedNotificationLetter;
    }

    private CaseDocument generateDocument(String authToken,
                                                    IntervenerAddedLetterDetails intervenerAddedLetterDetails,
                                                    String template,
                                                    String filename) {
        return genericDocumentService.generateDocumentFromPlaceholdersMap(
            authToken,
            convertLetterDetailsToMap(intervenerAddedLetterDetails),
            template,
            filename);
    }

    private IntervenerAddedLetterDetails generateLetterDetails(FinremCaseDetails caseDetails) {

        return IntervenerAddedLetterDetails.builder()
            .courtDetails(CaseHearingFunctions.buildFrcCourtDetails(caseDetails.getData()))
            .addressee(caseDetails.getData().getCurrentAddressee())
            .divorceCaseNumber(caseDetails.getData().getDivorceCaseNumber())
            .applicantName(caseDetails.getData().getFullApplicantName())
            .respondentName(caseDetails.getRespondentFullName())
            .letterDate(DateTimeFormatter.ofPattern(LETTER_DATE_FORMAT).format(LocalDate.now()))
            .caseNumber(caseDetails.getId().toString())
            .intervenerFullName(caseDetails.getData().getCurrentIntervenerChangeDetails().getIntervenerDetails().getIntervenerName())
            .build();
    }

    private Map<String, Object> convertLetterDetailsToMap(IntervenerAddedLetterDetails letterDetails) {
        Map<String, Object> caseDetailsMap = Map.of(CASE_DATA, objectMapper.convertValue(letterDetails, Map.class));
        return Map.of(CASE_DETAILS, caseDetailsMap);
    }
}
