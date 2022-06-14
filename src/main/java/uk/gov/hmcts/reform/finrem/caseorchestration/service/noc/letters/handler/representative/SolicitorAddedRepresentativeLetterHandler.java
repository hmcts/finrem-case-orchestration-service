package uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.letters.handler.representative;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.NoticeType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.documents.SolicitorNocDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.documents.generators.SolicitorAddedLetterDetailsGenerator;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.solicitors.CheckApplicantSolicitorIsDigitalService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.solicitors.CheckRespondentSolicitorIsDigitalService;

@Slf4j
@Component
public class SolicitorAddedRepresentativeLetterHandler extends RepresentativeLetterHandler {

    @Autowired
    public SolicitorAddedRepresentativeLetterHandler(
        SolicitorAddedLetterDetailsGenerator noticeOfChangeLetterDetailsGenerator,
        SolicitorNocDocumentService solicitorNocDocumentService,
        BulkPrintService bulkPrintService,
        CaseDataService caseDataService,
        CheckApplicantSolicitorIsDigitalService checkApplicantSolicitorIsDigitalService,
        CheckRespondentSolicitorIsDigitalService checkRespondentSolicitorIsDigitalService) {
        super(noticeOfChangeLetterDetailsGenerator, solicitorNocDocumentService,
            bulkPrintService, caseDataService, NoticeType.ADD, checkApplicantSolicitorIsDigitalService,
            checkRespondentSolicitorIsDigitalService);
    }

}
