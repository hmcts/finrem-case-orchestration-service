package uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.letters.handler.representative;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.NoticeType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.documents.SolicitorNocDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.documents.generators.NocLetterDetailsGenerator;

@Slf4j
@Component
public class SolicitorRemovedRepresentativeLetterHandler extends RepresentativeLetterHandler {
    private final CaseDataService caseDataService;

    @Autowired
    public SolicitorRemovedRepresentativeLetterHandler(
        NocLetterDetailsGenerator noticeOfChangeLetterDetailsGenerator,
        SolicitorNocDocumentService solicitorNocDocumentService,
        BulkPrintService bulkPrintService, CaseDataService caseDataService) {
        super(noticeOfChangeLetterDetailsGenerator, solicitorNocDocumentService,
            bulkPrintService, caseDataService, NoticeType.REMOVE);
        this.caseDataService = caseDataService;
    }


}
