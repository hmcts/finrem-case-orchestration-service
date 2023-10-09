package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.ConsentedApplicationHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SolicitorCreateConsentedMidHandler
    implements CallbackHandler<Map<String, Object>> {

    private final ConsentedApplicationHelper helper;
    private final BulkPrintDocumentService service;
    private final ConsentOrderService consentOrderService;

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.MID_EVENT.equals(callbackType)
            && CaseType.CONSENTED.equals(caseType)
            && (EventType.SOLICITOR_CREATE.equals(eventType)
            || EventType.AMEND_APP_DETAILS.equals(eventType));
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<Map<String, Object>> handle(CallbackRequest callbackRequest,
                                                                                   String userAuthorisation) {

        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Optional<Long> caseIdObj = Optional.ofNullable(caseDetails.getId());
        String caseId;
        if (caseIdObj.isPresent()) {
            caseId = String.valueOf(caseIdObj.get());
        } else {
            caseId = "Case not created yet.";
        }
        log.info("Received request for mid handler for Case ID: {}", caseId);
        Map<String, Object> caseData = caseDetails.getData();

        helper.setConsentVariationOrderLabelField(caseData);
        List<String> errors = new ArrayList<>();

        List<CaseDocument> caseDocuments = consentOrderService.checkIfD81DocumentContainsEncryption(caseData);
        caseDocuments.forEach(document -> service.validateEncryptionOnUploadedDocument(document, caseId, errors, userAuthorisation));

        return GenericAboutToStartOrSubmitCallbackResponse.<Map<String, Object>>builder()
            .data(caseData).errors(errors).build();
    }
}
