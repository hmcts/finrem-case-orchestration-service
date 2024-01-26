package uk.gov.hmcts.reform.finrem.caseorchestration.handler.creategeneralorder;

import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralOrderWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;

import java.time.Clock;
import java.time.LocalDate;

abstract class CreateGeneralOrderAboutToStartHandler extends FinremCallbackHandler {

    private final IdamService idamService;
    private final Clock clock;

    public CreateGeneralOrderAboutToStartHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                 IdamService idamService, Clock clock) {
        super(finremCaseDetailsMapper);
        this.idamService = idamService;
        this.clock = clock;
    }

    protected void initGeneralOrder(GeneralOrderWrapper generalOrderWrapper, String userAuthorisation) {
        generalOrderWrapper.setGeneralOrderCreatedBy(idamService.getIdamFullName(userAuthorisation));
        generalOrderWrapper.setGeneralOrderRecitals(null);
        generalOrderWrapper.setGeneralOrderJudgeType(null);
        generalOrderWrapper.setGeneralOrderJudgeName(idamService.getIdamSurname(userAuthorisation));
        generalOrderWrapper.setGeneralOrderDate(LocalDate.now(clock));
        generalOrderWrapper.setGeneralOrderBodyText(null);
        generalOrderWrapper.setGeneralOrderPreviewDocument(null);
        generalOrderWrapper.setGeneralOrderAddressTo(null);
    }
}
