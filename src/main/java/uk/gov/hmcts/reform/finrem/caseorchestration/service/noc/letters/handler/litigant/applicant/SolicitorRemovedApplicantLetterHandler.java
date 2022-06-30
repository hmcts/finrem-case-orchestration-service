package uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.letters.handler.litigant.applicant;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.NoticeType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.documents.LitigantSolicitorRemovedNocDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.documents.generators.SolicitorRemovedLetterDetailsGenerator;

@Slf4j
@Component
public class SolicitorRemovedApplicantLetterHandler extends SolicitorChangedApplicantLetterHandler {

    @Autowired
    public SolicitorRemovedApplicantLetterHandler(
        SolicitorRemovedLetterDetailsGenerator noticeOfChangeLetterDetailsGenerator,
        LitigantSolicitorRemovedNocDocumentService litigantSolicitorRemovedNocDocumentService,
        BulkPrintService bulkPrintService) {
        super(noticeOfChangeLetterDetailsGenerator, litigantSolicitorRemovedNocDocumentService, bulkPrintService, NoticeType.REMOVE);
    }
}