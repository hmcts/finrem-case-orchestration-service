package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintDocumentService;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class CreateGeneralEmailConsentMidHandler extends FinremCallbackHandler {

    private final BulkPrintDocumentService service;

    @Autowired
    public CreateGeneralEmailConsentMidHandler(FinremCaseDetailsMapper mapper,
                                               BulkPrintDocumentService service) {
        super(mapper);
        this.service = service;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.MID_EVENT.equals(callbackType)
            && CaseType.CONSENTED.equals(caseType)
            && EventType.CREATE_GENERAL_EMAIL.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {

        // JCDEBUG
        final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        try {
            log.info("JCDEBUG #4: " + objectMapper.writeValueAsString(callbackRequest.getCaseDetails()));
            log.info("JCDEBUG #5: " + objectMapper.writeValueAsString(callbackRequest.getCaseDetailsBefore()));
        } catch (JsonProcessingException e) {
            log.info("JCDEBUG #6: " + e.getMessage());
        }

        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        String caseId = String.valueOf(caseDetails.getId());
        log.info("Received request to consent general email for Case ID: {}", caseDetails.getId());
        validateCaseData(callbackRequest);
        FinremCaseData finremCaseData = caseDetails.getData();

        CaseDocument caseDocument = finremCaseData.getGeneralEmailWrapper().getGeneralEmailUploadedDocument();
        List<String> errors = new ArrayList<>();
        if (caseDocument != null) {
            service.validateEncryptionOnUploadedDocument(caseDocument,
                caseId, errors, userAuthorisation);
        }
        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().data(finremCaseData).errors(errors).build();

    }
}
