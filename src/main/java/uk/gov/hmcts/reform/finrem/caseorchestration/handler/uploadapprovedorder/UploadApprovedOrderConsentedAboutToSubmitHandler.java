package uk.gov.hmcts.reform.finrem.caseorchestration.handler.uploadapprovedorder;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.CallbackHandlerLogger;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.helper.DocumentWarningsHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderApprovedDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;

import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Service
public class UploadApprovedOrderConsentedAboutToSubmitHandler extends FinremCallbackHandler {

    private final ConsentOrderApprovedDocumentService consentOrderApprovedDocumentService;
    private final GenericDocumentService genericDocumentService;
    private final DocumentWarningsHelper documentWarningsHelper;

    public UploadApprovedOrderConsentedAboutToSubmitHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                            ConsentOrderApprovedDocumentService consentOrderApprovedDocumentService,
                                                            GenericDocumentService genericDocumentService,
                                                            DocumentWarningsHelper documentWarningsHelper) {
        super(finremCaseDetailsMapper);
        this.consentOrderApprovedDocumentService = consentOrderApprovedDocumentService;
        this.genericDocumentService =  genericDocumentService;
        this.documentWarningsHelper = documentWarningsHelper;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && CaseType.CONSENTED.equals(caseType)
            && EventType.UPLOAD_APPROVED_ORDER.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        log.info(CallbackHandlerLogger.aboutToSubmit(callbackRequest));

        String caseId = String.valueOf(callbackRequest.getCaseDetails().getId());
        FinremCaseDetails finremCaseDetails = callbackRequest.getCaseDetails();
        FinremCaseData caseData = finremCaseDetails.getData();

        CaseDocument approvedConsentOrder = caseData.getConsentOrderWrapper().getUploadApprovedConsentOrder();
        CaseDocument approvedConsentOrderPdf = genericDocumentService.convertDocumentIfNotPdfAlready(approvedConsentOrder, userAuthorisation, caseId);
        caseData.getConsentOrderWrapper().setUploadApprovedConsentOrder(approvedConsentOrderPdf);

        caseData.setLatestConsentOrder(caseData.getConsentOrderWrapper().getUploadApprovedConsentOrder());
        consentOrderApprovedDocumentService
            .addGeneratedApprovedConsentOrderDocumentsToCase(userAuthorisation, finremCaseDetails);
        
        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(caseData)
            .warnings(documentWarningsHelper.getDocumentWarnings(callbackRequest, data ->
                    List.of(() -> Stream.ofNullable(data.getConsentOrderWrapper().getUploadApprovedConsentOrder()).toList()),
                userAuthorisation))
            .build();
    }
}
