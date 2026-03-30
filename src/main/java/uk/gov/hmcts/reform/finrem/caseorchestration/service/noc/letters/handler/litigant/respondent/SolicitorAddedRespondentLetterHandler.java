package uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.letters.handler.litigant.respondent;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.adapters.BulkPrintServiceAdapter;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.NoticeType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.documents.LitigantSolicitorAddedNocDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.documents.generators.SolicitorAddedLetterDetailsGenerator;

@Slf4j
@Component
public class SolicitorAddedRespondentLetterHandler extends SolicitorChangedRespondentLetterHandler {

    @Autowired
    public SolicitorAddedRespondentLetterHandler(
        SolicitorAddedLetterDetailsGenerator letterDetailsGenerator,
        LitigantSolicitorAddedNocDocumentService litigantSolicitorAddedNocDocumentService,
        BulkPrintServiceAdapter bulkPrintServiceAdapter) {
        super(letterDetailsGenerator, litigantSolicitorAddedNocDocumentService, bulkPrintServiceAdapter, NoticeType.ADD);
    }

}
