package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.DocumentClient;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.bulk.print.BulkPrintMetadata;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BULK_PRINT_COVER_SHEET_APP;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BULK_PRINT_COVER_SHEET_RES;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BULK_PRINT_LETTER_ID_APP;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BULK_PRINT_LETTER_ID_RES;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_AGREE_TO_RECEIVE_EMAILS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintDocumentTranslator.approvedOrderCollection;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintDocumentTranslator.uploadOrder;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.nullToEmpty;

@Service
@Slf4j
public class BulkPrintService extends AbstractDocumentService {

    private GenerateCoverSheetService generateCoverSheetService;

    @Autowired
    public BulkPrintService(DocumentClient documentClient,
                            DocumentConfiguration config,
                            ObjectMapper objectMapper,
                            GenerateCoverSheetService generateCoverSheetService) {
        super(documentClient, config, objectMapper);
        this.generateCoverSheetService = generateCoverSheetService;
    }

    public void sendLetterToApplicantSolicitor(String authToken, CaseDetails caseDetails) {
        logInfo("Applicant Solicitor", new CaseDocument(), UUID.randomUUID());
    }

    public BulkPrintMetadata sendLetterToApplicant(String authToken, CaseDetails caseDetails) {
        CaseDocument applicantCoverSheet = generateCoverSheetService.generateApplicantCoverSheet(caseDetails, authToken);
        UUID applicantLetterId = sendForBulkPrint(applicantCoverSheet, caseDetails);

        logInfo("Applicant", applicantCoverSheet, applicantLetterId);

        return BulkPrintMetadata.builder()
                .letterId(applicantLetterId)
                .coverSheet(applicantCoverSheet)
                .build();
    }

    public BulkPrintMetadata sendLetterToRespondent(String authToken, CaseDetails caseDetails) {
        CaseDocument respondentCoverSheet = generateCoverSheetService.generateRespondentCoverSheet(caseDetails, authToken);
        UUID respondentLetterId = sendForBulkPrint(respondentCoverSheet, caseDetails);

        logInfo("Respondent", respondentCoverSheet, respondentLetterId);

        return BulkPrintMetadata.builder()
            .letterId(respondentLetterId)
            .coverSheet(respondentCoverSheet)
            .build();
    }

    private UUID sendForBulkPrint(CaseDocument coverSheet, CaseDetails caseDetails) {
        List<BulkPrintDocument> bulkPrintDocuments = new ArrayList<>();

        bulkPrintDocuments.add(BulkPrintDocument.builder().binaryFileUrl(coverSheet.getDocumentBinaryUrl()).build());

        List<BulkPrintDocument> approvedOrderCollection = approvedOrderCollection(caseDetails.getData());
        List<BulkPrintDocument> uploadOrder = uploadOrder(caseDetails.getData());

        if (approvedOrderCollection.size() > 0) {
            log.info("Sending Approved Order Collections for Bulk Print.");
            bulkPrintDocuments.addAll(approvedOrderCollection);
        } else if (uploadOrder.size() > 0) {
            log.info("Sending Upload Order Collections for Bulk Print.");
            bulkPrintDocuments.addAll(uploadOrder);
        }

        log.info("{} Order documents including cover sheet have been sent bulk print.", bulkPrintDocuments.size());

        return bulkPrint(
            BulkPrintRequest.builder()
                .caseId(caseDetails.getId().toString())
                .letterType("FINANCIAL_REMEDY_PACK")
                .bulkPrintDocuments(bulkPrintDocuments)
                .build()
        );
    }

    public static boolean shouldBeSentToApplicant(ImmutableMap<String, Object> caseData) {
        return applicantIsNotRepresentedBySolicitor(caseData) || solicitorDidNotAgreeToReceiveEmails(caseData);
    }

    private static boolean applicantIsNotRepresentedBySolicitor(Map<String, Object> caseData) {
        return NO_VALUE.equalsIgnoreCase(nullToEmpty(caseData.get(APPLICANT_REPRESENTED)));
    }

    private static boolean solicitorDidNotAgreeToReceiveEmails(Map<String, Object> caseData) {
        return NO_VALUE.equalsIgnoreCase(nullToEmpty(caseData.get(SOLICITOR_AGREE_TO_RECEIVE_EMAILS)));
    }

    private static void logInfo(String party, CaseDocument caseDocument, UUID letterId) {
        log.info(
                "Generated {} CoverSheet for bulk print. coversheet: {}, letterId : {}",
                party,
                caseDocument,
                letterId
        );
    }
}
