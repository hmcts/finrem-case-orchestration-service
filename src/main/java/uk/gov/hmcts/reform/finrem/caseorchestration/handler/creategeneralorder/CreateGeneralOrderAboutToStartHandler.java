package uk.gov.hmcts.reform.finrem.caseorchestration.handler.creategeneralorder;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralOrderWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;

import java.time.Clock;
import java.time.LocalDate;

import static uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.GENERAL_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.GENERAL_ORDER_CONSENT_IN_CONTESTED;


@Service
@Slf4j
public class CreateGeneralOrderAboutToStartHandler extends FinremCallbackHandler {

    private final IdamService idamService;
    private final Clock clock;

    @Autowired
    public CreateGeneralOrderAboutToStartHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                 IdamService idamService) {
        this(finremCaseDetailsMapper, idamService, Clock.systemDefaultZone());
    }

    public CreateGeneralOrderAboutToStartHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                 IdamService idamService, Clock clock) {
        super(finremCaseDetailsMapper);
        this.idamService = idamService;
        this.clock = clock;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        if (!ABOUT_TO_START.equals(callbackType)) {
            return false;
        }

        switch (caseType) {
            case CONTESTED -> {
                return GENERAL_ORDER.equals(eventType) || GENERAL_ORDER_CONSENT_IN_CONTESTED.equals(eventType);
            }
            case CONSENTED -> {
                return GENERAL_ORDER.equals(eventType);
            }
            default -> {
                return false;
            }
        }
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(
        FinremCallbackRequest callbackRequestWithFinremCaseDetails, String userAuthorisation) {

        log.info("Create General Order about to start callback for case id: {}",
            callbackRequestWithFinremCaseDetails.getCaseDetails().getId());

        FinremCaseData caseData = callbackRequestWithFinremCaseDetails.getCaseDetails().getData();
        GeneralOrderWrapper generalOrderWrapper = caseData.getGeneralOrderWrapper();
        generalOrderWrapper.setGeneralOrderCreatedBy(idamService.getIdamFullName(userAuthorisation));
        generalOrderWrapper.setGeneralOrderRecitals(null);
        generalOrderWrapper.setGeneralOrderJudgeType(null);
        generalOrderWrapper.setGeneralOrderJudgeName(idamService.getIdamSurname(userAuthorisation));
        generalOrderWrapper.setGeneralOrderDate(LocalDate.now(clock));
        generalOrderWrapper.setGeneralOrderBodyText(null);
        generalOrderWrapper.setGeneralOrderPreviewDocument(null);
        generalOrderWrapper.setGeneralOrderAddressTo(null);

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().data(caseData).build();
    }
}
