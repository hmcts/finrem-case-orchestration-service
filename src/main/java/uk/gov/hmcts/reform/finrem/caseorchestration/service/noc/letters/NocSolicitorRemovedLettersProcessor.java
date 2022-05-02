package uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.letters;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.documents.LitigantSolicitorRemovedNocDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.NoticeType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.documents.SolicitorNocDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.documents.generators.NocLetterDetailsGenerator;

@Component
public class NocSolicitorRemovedLettersProcessor extends NocLettersProcessor {

    @Autowired
    public NocSolicitorRemovedLettersProcessor(
        LitigantSolicitorRemovedNocDocumentService litigantSolicitorRemovedNocDocumentService,
        SolicitorNocDocumentService solicitorNocDocumentService,
        NocLetterDetailsGenerator noticeOfChangeLetterDetailsGenerator,
        CaseDataService caseDataService,
        BulkPrintService bulkPrintService) {
        super(litigantSolicitorRemovedNocDocumentService, solicitorNocDocumentService, noticeOfChangeLetterDetailsGenerator, caseDataService, bulkPrintService,
            NoticeType.REMOVE);
    }
}
