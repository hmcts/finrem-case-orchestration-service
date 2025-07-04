package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimHearingCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintDocumentService;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class InterimHearingContestedMidHandler extends FinremCallbackHandler {

    private final BulkPrintDocumentService service;

    public InterimHearingContestedMidHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                             BulkPrintDocumentService service) {
        super(finremCaseDetailsMapper);
        this.service = service;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.MID_EVENT.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.INTERIM_HEARING.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        String caseId = String.valueOf(caseDetails.getId());
        log.info("Invoking contested event {} mid callback for Case ID: {}", EventType.INTERIM_HEARING, caseId);
        FinremCaseData finremCaseData = caseDetails.getData();
        List<String> errors = new ArrayList<>();

        List<InterimHearingCollection> newInterimHearings =
            finremCaseData.getInterimWrapper().getInterimHearingsScreenField();

        if (newInterimHearings != null && !newInterimHearings.isEmpty()) {

            newInterimHearings.forEach(hearing -> {
                if (hearing.getValue().getInterimPromptForAnyDocument().isYes()) {
                    CaseDocument document = hearing.getValue().getInterimUploadAdditionalDocument();
                    service.validateEncryptionOnUploadedDocument(document,
                        caseId, errors, userAuthorisation);
                }
            });
        }

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(finremCaseData).errors(errors).build();
    }
}
