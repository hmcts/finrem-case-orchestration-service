package uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.letters.handler.litigant.applicant;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RepresentationUpdate;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.NoticeType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.documents.NocDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.documents.generators.AbstractLetterDetailsGenerator;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.letters.handler.AbstractLetterHandler;

import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_ADDRESS;

@Slf4j
public class SolicitorChangedApplicantLetterHandler extends AbstractLetterHandler {

    public SolicitorChangedApplicantLetterHandler(
        AbstractLetterDetailsGenerator letterDetailsGenerator,
        NocDocumentService nocDocumentService, BulkPrintService bulkPrintService,
        NoticeType noticeType) {
        super(letterDetailsGenerator, nocDocumentService, bulkPrintService, noticeType, APPLICANT);
    }

    @Override
    protected boolean shouldALetterBeSent(RepresentationUpdate representationUpdate,
                                          CaseDetails caseDetailsToUse,
                                          CaseDetails otherCaseDetails) {
        log.info("Now check if solicitor notification letter is required for applicant");
        return isApplicant(representationUpdate) && isAddressFieldPopulated(caseDetailsToUse, APPLICANT_ADDRESS);
    }
}
