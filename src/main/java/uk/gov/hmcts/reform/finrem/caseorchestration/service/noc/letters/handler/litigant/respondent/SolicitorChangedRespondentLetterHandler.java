package uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.letters.handler.litigant.respondent;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RepresentationUpdate;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.NoticeType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.documents.NocDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.documents.generators.NocLetterDetailsGenerator;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.letters.handler.AbstractLetterHandler;

import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.RESPONDENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_ADDRESS;

@Slf4j
public class SolicitorChangedRespondentLetterHandler extends AbstractLetterHandler {

    public SolicitorChangedRespondentLetterHandler(
        NocLetterDetailsGenerator noticeOfChangeLetterDetailsGenerator,
        NocDocumentService nocDocumentService, BulkPrintService bulkPrintService, NoticeType noticeType) {
        super(noticeOfChangeLetterDetailsGenerator, nocDocumentService, bulkPrintService, noticeType, RESPONDENT);
    }

    @Override
    protected boolean shouldALetterBeSent(RepresentationUpdate representationUpdate, CaseDetails caseDetailsToUse) {
        log.info("Now check if solicitor notification letter is required for applicant");
        return !isApplicant(representationUpdate) && isCaseFieldPopulated(caseDetailsToUse, RESPONDENT_ADDRESS);
    }
}
