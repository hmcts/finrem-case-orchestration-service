package uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.letters.handler.litigant.applicant;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.NoticeType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.documents.NocDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.documents.generators.AbstractLetterDetailsGenerator;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.letters.handler.AbstractLetterHandler;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.RepresentationUpdate;

import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.APPLICANT;

@Slf4j
public class SolicitorChangedApplicantLetterHandler extends AbstractLetterHandler {

    public SolicitorChangedApplicantLetterHandler(
        AbstractLetterDetailsGenerator letterDetailsGenerator,
        NocDocumentService nocDocumentService,
        BulkPrintService bulkPrintService,
        NoticeType noticeType) {
        super(letterDetailsGenerator, nocDocumentService, bulkPrintService, noticeType, APPLICANT);
    }

    @Override
    protected boolean shouldALetterBeSent(RepresentationUpdate representationUpdate,
                                          FinremCaseDetails caseDetailsToUse,
                                          FinremCaseDetails otherCaseDetails) {
        log.info("Now check if solicitor notification letter is required for applicant");
        return isApplicant(representationUpdate)
            && isAddressFieldPopulated(caseDetailsToUse.getCaseData().getContactDetailsWrapper().getApplicantAddress());
    }
}
