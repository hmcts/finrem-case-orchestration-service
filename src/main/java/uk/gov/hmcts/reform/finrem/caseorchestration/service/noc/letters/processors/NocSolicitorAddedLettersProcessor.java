package uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.letters.processors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.letters.generators.LitigantSolicitorAddedNocLetterGenerator;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.letters.generators.NocLetterDetailsGenerator;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.letters.generators.SolicitorNocLetterGenerator;

@Component
public class NocSolicitorAddedLettersProcessor extends NocSolicitorLettersProcessor {

    @Autowired
    public NocSolicitorAddedLettersProcessor(
        LitigantSolicitorAddedNocLetterGenerator litigantNocLetterGenerator,
        SolicitorNocLetterGenerator solicitorNocLetterGenerator,
        NocLetterDetailsGenerator noticeOfChangeLetterDetailsGenerator,
        CaseDataService caseDataService) {
        super(litigantNocLetterGenerator, solicitorNocLetterGenerator, noticeOfChangeLetterDetailsGenerator, caseDataService,
            NocLetterDetailsGenerator.NoticeType.ADD);
    }
}
