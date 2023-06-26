package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;

@Slf4j
@Service
public class CreateGeneralLetterAboutToStartHandler extends FinremCallbackHandler {

    private final IdamService idamService;

    @Autowired
    public CreateGeneralLetterAboutToStartHandler(FinremCaseDetailsMapper mapper,
                                                  IdamService idamService) {
        super(mapper);
        this.idamService = idamService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_START.equals(callbackType)
            && (EventType.CREATE_GENERAL_LETTER.equals(eventType)
            || EventType.CREATE_GENERAL_LETTER_JUDGE.equals(eventType));
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        log.info("Received request to clear general letter fields for Case ID: {}", caseDetails.getId());

        validateCaseData(callbackRequest);

        FinremCaseData caseData = caseDetails.getData();
        if (caseData.getGeneralLetterWrapper() != null) {
            caseData.getGeneralLetterWrapper().setGeneralLetterAddressTo(null);
            caseData.getGeneralLetterWrapper().setGeneralLetterRecipient(null);
            caseData.getGeneralLetterWrapper().setGeneralLetterRecipientAddress(null);
            caseData.getGeneralLetterWrapper().setGeneralLetterCreatedBy(idamService.getIdamFullName(userAuthorisation));
            caseData.getGeneralLetterWrapper().setGeneralLetterBody(null);
            caseData.getGeneralLetterWrapper().setGeneralLetterPreview(null);
            caseData.getGeneralLetterWrapper().setGeneralLetterUploadedDocument(null);
        }

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().data(caseDetails.getData()).build();
    }


}
