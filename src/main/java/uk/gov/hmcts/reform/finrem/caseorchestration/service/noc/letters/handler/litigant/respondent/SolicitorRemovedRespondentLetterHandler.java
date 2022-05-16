package uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.letters.handler.litigant.respondent;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.NoticeType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.documents.LitigantSolicitorRemovedNocDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.documents.generators.NocLetterDetailsGenerator;

@Slf4j
@Component
public class SolicitorRemovedRespondentLetterHandler extends SolicitorChangedRespondentLetterHandler {

    @Autowired
    public SolicitorRemovedRespondentLetterHandler(
        NocLetterDetailsGenerator noticeOfChangeLetterDetailsGenerator,
        LitigantSolicitorRemovedNocDocumentService litigantSolicitorRemovedNocDocumentService,
        BulkPrintService bulkPrintService) {
        super(noticeOfChangeLetterDetailsGenerator, litigantSolicitorRemovedNocDocumentService, bulkPrintService, NoticeType.REMOVE);
    }
}
