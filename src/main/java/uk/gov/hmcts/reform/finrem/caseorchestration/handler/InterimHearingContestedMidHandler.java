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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDataContested;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimHearingCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintDocumentService;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class InterimHearingContestedMidHandler extends FinremCallbackHandler<FinremCaseDataContested> {

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
            && (EventType.INTERIM_HEARING.equals(eventType));
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseDataContested> handle(
        FinremCallbackRequest<FinremCaseDataContested> callbackRequest, String userAuthorisation) {

        FinremCaseDetails<FinremCaseDataContested> caseDetails = callbackRequest.getCaseDetails();
        String caseId = String.valueOf(caseDetails.getId());
        log.info("Invoking contested event {} mid callback for case id: {}",
            EventType.INTERIM_HEARING, caseId);
        FinremCaseDataContested finremCaseData = caseDetails.getData();
        List<String> errors = new ArrayList<>();

        List<InterimHearingCollection> interimHearings = finremCaseData.getInterimWrapper().getInterimHearings();
        if (interimHearings != null && !interimHearings.isEmpty()) {
            interimHearings.forEach(hearing -> {
                if (hearing.getValue().getInterimPromptForAnyDocument().equalsIgnoreCase("Yes")) {
                    CaseDocument document = hearing.getValue().getInterimUploadAdditionalDocument();
                    service.validateEncryptionOnUploadedDocument(document,
                        caseId, errors, userAuthorisation);
                }
            });
        }

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseDataContested>builder()
            .data(finremCaseData).errors(errors).build();
    }
}