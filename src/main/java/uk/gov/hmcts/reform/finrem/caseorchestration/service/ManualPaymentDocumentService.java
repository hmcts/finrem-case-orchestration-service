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

    public CaseDocument generateManualPaymentLetter(CaseDetails caseDetails, String authToken) {
        log.info("Generating Manual Payment Letter {} from {} for bulk print",
            documentConfiguration.getManualPaymentFileName(),
            documentConfiguration.getManualPaymentTemplate());

        CaseDetails caseDetailsForBulkPrint = documentHelper.prepareLetterToApplicantTemplateData(caseDetails);

        /* TODO:
        Build a mapper in order to populate values in template with:
        <<courtName>>
        <<courtAddress>>
        <<courtEmail>>
        <<courtphoneNumber>>
        */

        CaseDocument generatedManualPaymentLetter = genericDocumentService.generateDocument(authToken,
            caseDetailsForBulkPrint,
            documentConfiguration.getManualPaymentFileName(),
            documentConfiguration.getManualPaymentFileName());

        log.info("Generated Manual Payment Letter: {}", generatedManualPaymentLetter);

        return generatedManualPaymentLetter;
    }
}
