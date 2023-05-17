package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralOrderService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.StampType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Slf4j
@Service
public class SendOrderContestedAboutToSubmitHandler extends FinremCallbackHandler {

    private final GeneralOrderService generalOrderService;
    private final GenericDocumentService genericDocumentService;
    private final DocumentHelper documentHelper;

    public SendOrderContestedAboutToSubmitHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                  GeneralOrderService generalOrderService,
                                                  GenericDocumentService genericDocumentService,
                                                  DocumentHelper documentHelper) {
        super(finremCaseDetailsMapper);
        this.generalOrderService =  generalOrderService;
        this.genericDocumentService =  genericDocumentService;
        this.documentHelper =  documentHelper;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.SEND_ORDER.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        Long caseId = caseDetails.getId();
        log.info("Invoking contested event {}, callback {} callback for case id: {}",
            EventType.SEND_ORDER, CallbackType.ABOUT_TO_SUBMIT, caseId);

        try {
            List<String> parties = generalOrderService.getParties(caseDetails);
            log.info("selected parties {} on case {}", parties, caseDetails.getId());

            DynamicMultiSelectList selectedOrders = caseDetails.getData().getOrdersToShare();
            log.info("selected orders {} on case {}", selectedOrders, caseDetails.getId());

            log.info("sending for stamp final order on case {}", caseDetails.getId());
            List<CaseDocument> hearingOrders = generalOrderService.hearingOrderToProcess(caseDetails, selectedOrders);
            hearingOrders.forEach(orderToStamp -> {
                log.info("StampFinalOrder {} for Case ID {}, ", orderToStamp, caseId);
                stampAndAddToCollection(caseDetails, orderToStamp, parties, userAuthorisation);
            });

        } catch (RuntimeException e) {
            return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
                .data(caseDetails.getData()).errors(List.of(e.getMessage())).build();
        }

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(caseDetails.getData()).build();
    }

    private void stampAndAddToCollection(FinremCaseDetails caseDetails, CaseDocument latestHearingOrder,
                                         List<String> partyList, String authToken) {
        Long caseId =  caseDetails.getId();
        FinremCaseData caseData = caseDetails.getData();

        StampType stampType = documentHelper.getStampType(caseData);
        CaseDocument stampedDocs = genericDocumentService.stampDocument(latestHearingOrder, authToken, stampType);
        log.info("Stamped Documents = {} for caseId {}", stampedDocs, caseId);

        List<DirectionOrderCollection> finalOrderCollection = Optional.ofNullable(caseData.getFinalOrderCollection())
            .orElse(new ArrayList<>());

        finalOrderCollection.add(prepareFinalOrderList(stampedDocs));
        log.info("Existing final order collection = {}", finalOrderCollection);

        caseData.setFinalOrderCollection(finalOrderCollection);
        log.info("Finished stamping final order for caseId {}", caseId);

        CaseDocument obj = caseData.getAdditionalDocument();
        if (obj != null) {
            List<DirectionOrderCollection> additionalOrderDocsCollection
                = Optional.ofNullable(caseData.getAdditionalOrderDocsCollection()).orElse(new ArrayList<>());

            additionalOrderDocsCollection.add(prepareFinalOrderList(obj));
            caseData.setAdditionalOrderDocsCollection(additionalOrderDocsCollection);
        }


        if (partyList.contains(CaseRole.INTVR_SOLICITOR_1.getValue())) {
            List<IntervenerOrderCollection> finalOrder =  Optional.ofNullable(caseData.getIntv1OrderCollection())
                .orElse(new ArrayList<>());

            finalOrder.add(getIntervenerFinalOrderList(stampedDocs));
            caseData.setIntv1OrderCollection(finalOrder);
            addIntervener1AdditionalSupportingDocWithOrder(caseData, obj);
        }

        if (partyList.contains(CaseRole.INTVR_SOLICITOR_2.getValue())) {
            List<IntervenerOrderCollection> finalOrder =  Optional.ofNullable(caseData.getIntv2OrderCollection())
                .orElse(new ArrayList<>());

            finalOrder.add(getIntervenerFinalOrderList(stampedDocs));
            caseData.setIntv2OrderCollection(finalOrder);
            addIntervener2AdditionalSupportingDocWithOrder(caseData, obj);
        }

        if (partyList.contains(CaseRole.INTVR_SOLICITOR_3.getValue())) {
            List<IntervenerOrderCollection> finalOrder =  Optional.ofNullable(caseData.getIntv3OrderCollection())
                .orElse(new ArrayList<>());

            finalOrder.add(getIntervenerFinalOrderList(stampedDocs));
            caseData.setIntv3OrderCollection(finalOrder);
            addIntervener3AdditionalSupportingDocWithOrder(caseData, obj);
        }

        if (partyList.contains(CaseRole.INTVR_SOLICITOR_4.getValue())) {
            List<IntervenerOrderCollection> finalOrder =  Optional.ofNullable(caseData.getIntv4OrderCollection())
                .orElse(new ArrayList<>());

            finalOrder.add(getIntervenerFinalOrderList(stampedDocs));
            caseData.setIntv4OrderCollection(finalOrder);
            addIntervener4AdditionalSupportingDocWithOrder(caseData, obj);
        }
    }

    private void addIntervener4AdditionalSupportingDocWithOrder(FinremCaseData caseData, CaseDocument obj) {
        if (obj != null) {
            List<IntervenerOrderCollection> additionalDoc =  Optional.ofNullable(caseData.getIntv4AdditionalOrderDocsColl())
                .orElse(new ArrayList<>());

            additionalDoc.add(getIntervenerFinalOrderList(obj));
            caseData.setIntv4AdditionalOrderDocsColl(additionalDoc);
        }
    }

    private void addIntervener3AdditionalSupportingDocWithOrder(FinremCaseData caseData, CaseDocument obj) {
        if (obj != null) {
            List<IntervenerOrderCollection> additionalDoc =  Optional.ofNullable(caseData.getIntv3AdditionalOrderDocsColl())
                .orElse(new ArrayList<>());

            additionalDoc.add(getIntervenerFinalOrderList(obj));
            caseData.setIntv3AdditionalOrderDocsColl(additionalDoc);
        }
    }

    private void addIntervener2AdditionalSupportingDocWithOrder(FinremCaseData caseData, CaseDocument obj) {
        if (obj != null) {
            List<IntervenerOrderCollection> additionalDoc =  Optional.ofNullable(caseData.getIntv2AdditionalOrderDocsColl())
                .orElse(new ArrayList<>());

            additionalDoc.add(getIntervenerFinalOrderList(obj));
            caseData.setIntv2AdditionalOrderDocsColl(additionalDoc);
        }
    }

    private void addIntervener1AdditionalSupportingDocWithOrder(FinremCaseData caseData, CaseDocument obj) {
        if (obj != null) {
            List<IntervenerOrderCollection> additionalDoc =  Optional.ofNullable(caseData.getIntv1AdditionalOrderDocsColl())
                .orElse(new ArrayList<>());
            additionalDoc.add(getIntervenerFinalOrderList(obj));
            caseData.setIntv1AdditionalOrderDocsColl(additionalDoc);
        }
    }

    private DirectionOrderCollection prepareFinalOrderList(CaseDocument document) {
        return DirectionOrderCollection.builder()
            .value(DirectionOrder.builder().uploadDraftDocument(document).build())
            .build();
    }

    private IntervenerOrderCollection getIntervenerFinalOrderList(CaseDocument stampedDocs) {
        return IntervenerOrderCollection.builder().value(IntervenerOrder.builder().approveOrder(stampedDocs).build())
            .build();
    }

}
