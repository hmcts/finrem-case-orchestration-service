package uk.gov.hmcts.reform.finrem.caseorchestration.service.interveners;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.bsp.common.model.document.Addressee;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.AddresseeGeneratorHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.barristers.BarristerLetterDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Address;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerAddedLetterDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerChangeDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseHearingFunctions;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.ADDRESSEE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CASE_DATA;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CASE_DETAILS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.DIVORCE_CASE_NUMBER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.documents.generators.AbstractLetterDetailsGenerator.LETTER_DATE_FORMAT;

@Service
@RequiredArgsConstructor
@Slf4j
public class IntervenerDocumentService {

    private final GenericDocumentService genericDocumentService;
    private final DocumentConfiguration documentConfiguration;
    private final DocumentHelper documentHelper;
    private final ObjectMapper objectMapper;

    public CaseDocument generateIntervenerAddedNotificationLetter(FinremCaseDetails caseDetails, String authToken,
                                                                  DocumentHelper.PaperNotificationRecipient recipient) {

        log.info("Generating Intervener Added Notification Letter {} from {} for bulk print for {}",
            documentConfiguration.getIntervenerAddedTemplate(),
            documentConfiguration.getIntervenerAddedFilename(),
            recipient);


        CaseDetails caseDetailsForBulkPrint = documentHelper.prepareLetterTemplateData(caseDetails, recipient);
        IntervenerAddedLetterDetails intervenerAddedLetterDetails = generateLetterDetails(caseDetailsForBulkPrint,
            recipient);

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

    private IntervenerAddedLetterDetails generateLetterDetails(CaseDetails caseDetails,
                                                               DocumentHelper.PaperNotificationRecipient recipient){

        return IntervenerAddedLetterDetails.builder()
            .courtDetails(CaseHearingFunctions.buildFrcCourtDetails(caseDetails.getData()))
            .addressee((Addressee) caseDetails.getData().get(ADDRESSEE))
            .divorceCaseNumber(Objects.toString(caseDetails.getData().get(DIVORCE_CASE_NUMBER), StringUtils.EMPTY))
            .applicantName(documentHelper.getApplicantFullName(caseDetails))
            .respondentName(documentHelper.getRespondentFullNameContested(caseDetails))
            .letterDate(DateTimeFormatter.ofPattern(LETTER_DATE_FORMAT).format(LocalDate.now()))
            .caseNumber(caseDetails.getId().toString())
            .intervenerFullName(intervenerDetails.getIntervenerName())
            .intervenerSolicitorFirm(intervenerDetails.getIntervenerOrganisation().getOrganisation().getOrganisationName())
            .build();
    }

    private Map<String, Object> convertLetterDetailsToMap(IntervenerAddedLetterDetails letterDetails) {
        Map<String, Object> caseDetailsMap = Map.of(CASE_DATA, objectMapper.convertValue(letterDetails, Map.class));
        return Map.of(CASE_DETAILS, caseDetailsMap);
    }
}
