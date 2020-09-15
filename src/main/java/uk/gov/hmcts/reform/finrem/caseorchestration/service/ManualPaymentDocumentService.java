package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;

@Service
@Slf4j
@RequiredArgsConstructor
public class ManualPaymentDocumentService {

    private final GenericDocumentService genericDocumentService;
    private final DocumentConfiguration documentConfiguration;
    private final DocumentHelper documentHelper;
    private final HearingDocumentService hearingDocumentService;

    public CaseDocument generateApplicantManualPaymentLetter(CaseDetails caseDetails, String authToken) {
        log.info("Generating Applicant Manual Payment Letter {} from {} for bulk print",
            documentConfiguration.getManualPaymentFileName(),
            documentConfiguration.getManualPaymentTemplate());

        CaseDetails caseDetailsForBulkPrint = documentHelper.prepareLetterToApplicantTemplateData(caseDetails);

        return generateManualPaymentLetter(caseDetailsForBulkPrint, authToken);
    }

    public CaseDocument generateManualPaymentLetter(CaseDetails caseDetailsForBulkPrint, String authToken) {

        hearingDocumentService.addCourtFields(caseDetailsForBulkPrint);

        CaseDocument manualPaymentLetter = genericDocumentService.generateDocument(authToken,
            caseDetailsForBulkPrint,
            documentConfiguration.getManualPaymentTemplate(),
            documentConfiguration.getManualPaymentFileName());
        log.info("Generated Manual Payment Letter: {}", manualPaymentLetter);

        return manualPaymentLetter;
    }
}