package uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.letters.handler.litigant.applicant;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.adapters.BulkPrintServiceAdapter;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.NoticeType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.documents.LitigantSolicitorAddedNocDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.documents.generators.SolicitorAddedLetterDetailsGenerator;

@Slf4j
@Component
public class SolicitorAddedApplicantLetterHandler extends SolicitorChangedApplicantLetterHandler {

    @Autowired
    public SolicitorAddedApplicantLetterHandler(
        SolicitorAddedLetterDetailsGenerator noticeOfChangeLetterDetailsGenerator,
        LitigantSolicitorAddedNocDocumentService litigantSolicitorAddedNocDocumentService,
        BulkPrintServiceAdapter bulkPrintServiceAdapter) {
        super(noticeOfChangeLetterDetailsGenerator, litigantSolicitorAddedNocDocumentService, bulkPrintServiceAdapter, NoticeType.ADD);
    }

}
