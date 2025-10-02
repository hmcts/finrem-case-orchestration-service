package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.helper.DocumentWarningsHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AmendedConsentOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseFlagsService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderService;

import static org.apache.commons.collections4.ListUtils.emptyIfNull;

@Slf4j
@Service
public class AmendConsentOrderAboutToSubmitHandler extends FinremCallbackHandler {

    private final ConsentOrderService consentOrderService;

    private final CaseFlagsService caseFlagsService;

    private final DocumentWarningsHelper documentWarningsHelper;

    public AmendConsentOrderAboutToSubmitHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                 ConsentOrderService consentOrderService,
                                                 CaseFlagsService caseFlagsService,
                                                 DocumentWarningsHelper documentWarningsHelper) {
        super(finremCaseDetailsMapper);
        this.consentOrderService = consentOrderService;
        this.caseFlagsService = caseFlagsService;
        this.documentWarningsHelper = documentWarningsHelper;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && CaseType.CONSENTED.equals(caseType)
            && EventType.AMEND_CONSENT_ORDER.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        log.info(CallbackHandlerLogger.aboutToSubmit(callbackRequest));
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();

        FinremCaseData caseData = caseDetails.getData();

        CaseDocument caseDocument = consentOrderService.getLatestConsentOrderData(callbackRequest);
        caseData.setLatestConsentOrder(caseDocument);

        caseFlagsService.setCaseFlagInformation(callbackRequest.getCaseDetails());
        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .warnings(documentWarningsHelper.getDocumentWarnings(callbackRequest, data ->
                emptyIfNull(data.getAmendedConsentOrderCollection()).stream()
                    .map(AmendedConsentOrderCollection::getValue).toList(), userAuthorisation))
            .data(caseData).build();
    }
}
