package uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.letters.handler.litigant.respondent;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.NoticeType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.documents.NocDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.documents.generators.AbstractLetterDetailsGenerator;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.letters.handler.AbstractLetterHandler;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.RepresentationUpdate;

import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.RESPONDENT;

@Slf4j
public class SolicitorChangedRespondentLetterHandler extends AbstractLetterHandler {

    public SolicitorChangedRespondentLetterHandler(
        AbstractLetterDetailsGenerator letterDetailsGenerator,
        NocDocumentService nocDocumentService, BulkPrintService bulkPrintService,
        NoticeType noticeType) {
        super(letterDetailsGenerator, nocDocumentService, bulkPrintService, noticeType, RESPONDENT);
    }

    @Override
    protected boolean shouldALetterBeSent(RepresentationUpdate representationUpdate,
                                          FinremCaseDetails caseDetailsToUse,
                                          FinremCaseDetails otherCaseDetails) {
        log.info("Now check if notification letter is required for respondent");
        return !isApplicant(representationUpdate) && isAddressFieldPopulated(caseDetailsToUse.getCaseData()
            .getContactDetailsWrapper().getRespondentAddress());
    }
}
