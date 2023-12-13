package uk.gov.hmcts.reform.finrem.caseorchestration.handler.uploadapprovedorder;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;

import java.time.LocalDate;

@Slf4j
@Service
public class UploadApprovedOrderConsentedAboutToStartHandler extends FinremCallbackHandler {

    private final IdamService idamService;

    public UploadApprovedOrderConsentedAboutToStartHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                           IdamService idamService) {
        super(finremCaseDetailsMapper);
        this.idamService = idamService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_START.equals(callbackType)
            && CaseType.CONSENTED.equals(caseType)
            && EventType.UPLOAD_APPROVED_ORDER.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        log.info("Handling Upload Approved Order Consented application about to start callback for case id: {}",
            callbackRequest.getCaseDetails().getId());
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        FinremCaseData caseData = caseDetails.getData();

        caseData.setOrderDirectionAbsolute(null);
        caseData.setServePensionProvider(null);
        caseData.setServePensionProviderResponsibility(null);
        caseData.setServePensionProviderOther(null);
        caseData.setOrderDirectionJudge(null);
        caseData.setOrderDirectionAddComments(null);
        caseData.getConsentOrderWrapper().setUploadApprovedConsentOrder(null);

        caseData.setOrderDirectionJudgeName(idamService.getIdamSurname(userAuthorisation));
        caseData.setOrderDirectionDate(LocalDate.now());

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().data(caseData).build();
    }
}
