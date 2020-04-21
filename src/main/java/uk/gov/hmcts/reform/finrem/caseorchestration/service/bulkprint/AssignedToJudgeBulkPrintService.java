package uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkprint;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.DocumentClient;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.Addressee;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.AssignedToJudgeLetter;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CtscContactDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AbstractDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;

import java.time.LocalDate;
import java.util.Map;

import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.LetterAddressHelper.formatAddressForLetterPrinting;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_FIRST_MIDDLE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_LAST_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_RESPONDENT_FIRST_MIDDLE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_SOLICITOR_ADDRESS_CCD_FIELD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ASSIGNED_TO_JUDGE_NOTIFICATION_LETTER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.buildFullName;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.isApplicantRepresented;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.nullToEmpty;

@Service
@Slf4j
public class AssignedToJudgeBulkPrintService extends AbstractDocumentService {

    @Autowired
    private BulkPrintService bulkPrintService;

    @Autowired
    public AssignedToJudgeBulkPrintService(DocumentClient documentClient, DocumentConfiguration config, ObjectMapper objectMapper) {
        super(documentClient, config, objectMapper);
    }

    public CaseDetails sendLetter(String authToken, CaseDetails caseDetails) {
        Map<String, Object> caseData = caseDetails.getData();

        CaseDocument assignedToJudgeLetter = generateAssignedToJudgeBulkPrintLetter(caseDetails, authToken);
        // take generated letter and send for bulk print
        bulkPrintService.sendForBulkPrint(assignedToJudgeLetter, caseDetails);
        // also update to store letter in CCD

        /* need to properly send to bulk print using:

        return bulkPrint(
            BulkPrintRequest.builder()
                .caseId(caseDetails.getId().toString())
                .letterType("FINANCIAL_REMEDY_PACK")
                .bulkPrintDocuments(bulkPrintDocuments)
                .build());
         */

        log.info("Application assigned to judge bulk print letter - generated {}", assignedToJudgeLetter);
        // Need to remove the document before saving to CCD
        caseData.remove(ASSIGNED_TO_JUDGE_NOTIFICATION_LETTER);

        return caseDetails;
    }

    /*
    create an interface that defines the methods we’re implementing if they’re going to
    have similar methods i.e. printLetter and sendLetterToBulkPrintService etc
     */

    private CaseDocument generateAssignedToJudgeBulkPrintLetter(CaseDetails caseDetails, String authToken) {
        String template = config.getApplicationAssignedToJudgeTemplate();
        String filename = config.getApplicationAssignedToJudgeFileName();

        log.info("Generating 'application assigned to judge' Letter {} from {} for bulk print", template, filename);

        try {
            AssignedToJudgeLetter letterToPrint = prepareAssignedToJudgeLetter(caseDetails);
            // Set the built letter in the case data for sending to Docmosis
            caseDetails.getData().put(ASSIGNED_TO_JUDGE_NOTIFICATION_LETTER, letterToPrint);

            return generateDocument(
                authToken,
                caseDetails,
                config.getApplicationAssignedToJudgeTemplate(),
                config.getApplicationAssignedToJudgeFileName());

        } catch (IllegalArgumentException exception) {
            log.warn(
                "Failed to generate 'application assigned to judge' Letter as not all required address details were present");
            throw exception;
        }
    }

    private AssignedToJudgeLetter prepareAssignedToJudgeLetter(CaseDetails caseDetails) {
        Map<String, Object> caseData = caseDetails.getData();

        AssignedToJudgeLetter.AssignedToJudgeLetterBuilder assignedToJudgeLetterBuilder = AssignedToJudgeLetter.builder();
        Addressee.AddresseeBuilder addresseeBuilder = Addressee.builder();
        CtscContactDetails.CtscContactDetailsBuilder ctscContactDetailsBuilder = CtscContactDetails.builder();

        String applicantName = buildFullName(caseData, APPLICANT_FIRST_MIDDLE_NAME, APPLICANT_LAST_NAME);

        assignedToJudgeLetterBuilder
            .caseNumber(nullToEmpty((caseDetails.getId())))
            .reference("");

        if (isApplicantRepresented(caseData)) {
            log.info("Applicant is represented by a solicitor");
            addresseeBuilder
                .name(nullToEmpty(caseData.get(SOLICITOR_NAME)))
                .formattedAddress(formatAddressForLetterPrinting((Map) caseData.get(APP_SOLICITOR_ADDRESS_CCD_FIELD)));
            assignedToJudgeLetterBuilder
                .applicantName(applicantName)
                .reference(nullToEmpty(caseData.get(SOLICITOR_REFERENCE)));
        } else {
            log.info("Applicant is not represented by a solicitor");
            addresseeBuilder
                .name(applicantName)
                .formattedAddress(formatAddressForLetterPrinting((Map) caseData.get(APPLICANT_ADDRESS)));
            assignedToJudgeLetterBuilder
                .applicantName(applicantName);
        }

        ctscContactDetailsBuilder
            .serviceCentre("Courts and Tribunals Service Centre")
            .careOf("c/o HMCTS Digital Financial Remedy")
            .poBox("12746")
            .town("HARLOW")
            .postcode("CM20 9QZ")
            .emailAddress("HMCTSFinancialRemedy@justice.gov.uk")
            .phoneNumber("0300 303 0642")
            .openingHours("from 8.30am to 5pm");

        return assignedToJudgeLetterBuilder
            .addressee(addresseeBuilder.build())
            .respondentName(buildFullName(caseData, APP_RESPONDENT_FIRST_MIDDLE_NAME, APP_RESPONDENT_LAST_NAME))
            .letterDate(String.valueOf(LocalDate.now()))
            .ctscContactDetails(ctscContactDetailsBuilder.build())
            .build();
    }
}