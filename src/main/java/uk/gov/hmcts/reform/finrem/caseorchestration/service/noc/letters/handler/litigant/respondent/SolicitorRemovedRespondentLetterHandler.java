package uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.letters.handler.litigant.respondent;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.NoticeType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.documents.LitigantSolicitorRemovedNocDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.documents.generators.SolicitorRemovedLetterDetailsGenerator;

@Slf4j
@Component
public class SolicitorRemovedRespondentLetterHandler extends SolicitorChangedRespondentLetterHandler {

    @Autowired
    public SolicitorRemovedRespondentLetterHandler(
        SolicitorRemovedLetterDetailsGenerator letterDetailsGenerator,
        LitigantSolicitorRemovedNocDocumentService litigantSolicitorRemovedNocDocumentService,
        BulkPrintService bulkPrintService) {
        super(letterDetailsGenerator, litigantSolicitorRemovedNocDocumentService, bulkPrintService, NoticeType.REMOVE);
    }
}
