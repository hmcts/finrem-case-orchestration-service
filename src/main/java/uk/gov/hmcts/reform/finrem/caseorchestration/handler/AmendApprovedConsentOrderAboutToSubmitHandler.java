package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ApprovedOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ConsentOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.PensionType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.StampType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory.ApprovedConsentOrderDocumentCategoriser;

import java.util.List;

@Slf4j
@Service
public class AmendApprovedConsentOrderAboutToSubmitHandler extends FinremCallbackHandler {
    private final ApprovedConsentOrderDocumentCategoriser approvedConsentOrderCategoriser;
    private final GenericDocumentService genericDocumentService;
    private final DocumentHelper documentHelper;

    public AmendApprovedConsentOrderAboutToSubmitHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                         ApprovedConsentOrderDocumentCategoriser approvedConsentOrderCategoriser,
                                                         GenericDocumentService genericDocumentService,
                                                         DocumentHelper documentHelper) {
        super(finremCaseDetailsMapper);
        this.approvedConsentOrderCategoriser = approvedConsentOrderCategoriser;
        this.genericDocumentService = genericDocumentService;
        this.documentHelper = documentHelper;
    }

    @Override
    public boolean canHandle(final CallbackType callbackType, final CaseType caseType,
                             final EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.AMEND_CONTESTED_APPROVED_CONSENT_ORDER.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        log.info(CallbackHandlerLogger.aboutToSubmit(callbackRequest));
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        FinremCaseData caseData = caseDetails.getData();
        FinremCaseData caseDataBefore = callbackRequest.getCaseDetailsBefore().getData();

        List<ConsentOrderCollection> collection = caseData.getConsentOrderWrapper().getContestedConsentedApprovedOrders();
        List<ConsentOrderCollection> collectionBefore = caseDataBefore.getConsentOrderWrapper().getContestedConsentedApprovedOrders();
        StampType stampType = documentHelper.getStampType(caseData);

        //Compare existing orders for updates
        for (int i = 0; i < collection.size(); i++) {
            ConsentOrderCollection currentOrder = collection.get(i);
            ConsentOrderCollection previousOrder = i < collectionBefore.size() ? collectionBefore.get(i) : null;

            ApprovedOrder currentApprovedOrder = currentOrder.getApprovedOrder();
            String caseId = caseDetails.getId().toString();

            //If the user has added a new order
            if (previousOrder == null) {
                currentApprovedOrder.setOrderLetter(
                    genericDocumentService.stampDocument(currentApprovedOrder.getOrderLetter(), userAuthorisation,
                        stampType, caseId)
                );
                currentApprovedOrder.setConsentOrder(
                    genericDocumentService.stampDocument(currentApprovedOrder.getConsentOrder(), userAuthorisation,
                        stampType, caseId)
                );
                continue;
            }

            ApprovedOrder previousApprovedOrder = previousOrder.getApprovedOrder();

            if (currentApprovedOrder.getOrderLetter() != null) {
                currentApprovedOrder.setOrderLetter(
                    stampIfUpdated(currentApprovedOrder.getOrderLetter(),
                        previousApprovedOrder.getOrderLetter(),
                        userAuthorisation, stampType, caseId
                    )
                );
            }
            if (currentApprovedOrder.getConsentOrder() != null) {
                currentApprovedOrder.setConsentOrder(
                    stampIfUpdated(
                        currentApprovedOrder.getConsentOrder(),
                        previousApprovedOrder.getConsentOrder(),
                        userAuthorisation, stampType, caseId
                    )
                );
            }
            if (CollectionUtils.isNotEmpty(currentApprovedOrder.getPensionDocuments())) {
                currentApprovedOrder.setPensionDocuments(
                    currentApprovedOrder.getPensionDocuments().stream()
                        .map(pensionDoc -> {
                            PensionType pensionTypeDoc = pensionDoc.getTypedCaseDocument();
                            pensionTypeDoc.setPensionDocument(genericDocumentService.stampDocument(
                                pensionTypeDoc.getPensionDocument(), userAuthorisation, stampType, caseDetails.getId().toString()));
                            return pensionDoc;
                        }).toList()
                );
            }
        }

        approvedConsentOrderCategoriser.categorise(caseDetails.getData());
        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().data(caseDetails.getData()).build();
    }

    private boolean isDocumentUpdated(CaseDocument current, CaseDocument previous) {
        return !current.getDocumentUrl().equals(previous.getDocumentUrl());
    }

    private CaseDocument stampIfUpdated(CaseDocument current, CaseDocument previous, String userAuth, StampType stampType, String caseId) {
        return isDocumentUpdated(current, previous)
            ? genericDocumentService.stampDocument(current, userAuth, stampType, caseId)
            : current;
    }
}
