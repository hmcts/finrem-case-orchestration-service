package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.CourtDetailsParseException;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AdditionalHearingDirectionsCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Element;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingOrderAdditionalDocCollectionData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingOrderCollectionData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_ORDER_APPROVED_DATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_ORDER_APPROVED_JUDGE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_ORDER_APPROVED_JUDGE_TYPE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_DIRECTION_DETAILS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_NOTICE_DOCUMENT_PACK;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_ORDER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_UPLOADED_DOCUMENT;

@Service
@RequiredArgsConstructor
@Slf4j
public class UploadApprovedOrderService {

    private final HearingOrderService hearingOrderService;
    private final ContestedOrderApprovedLetterService contestedOrderApprovedLetterService;
    private final AdditionalHearingDocumentService additionalHearingDocumentService;
    private final ApprovedOrderNoticeOfHearingService approvedOrderNoticeOfHearingService;

    public Map<String, Object> prepareFieldsForOrderApprovedCoverLetter(CaseDetails caseDetails) {
        Map<String, Object> caseData = caseDetails.getData();
        caseData.remove(CONTESTED_ORDER_APPROVED_JUDGE_TYPE);
        caseData.remove(CONTESTED_ORDER_APPROVED_JUDGE_NAME);
        caseData.remove(CONTESTED_ORDER_APPROVED_DATE);
        caseData.remove(HEARING_NOTICE_DOCUMENT_PACK);
        List<HearingOrderCollectionData> hearingOrderCollection
            = additionalHearingDocumentService.getApprovedHearingOrderCollection(caseDetails);
        if (hearingOrderCollection != null && !hearingOrderCollection.isEmpty()) {
            hearingOrderCollection.clear();
        }
        caseData.put(HEARING_ORDER_COLLECTION, hearingOrderCollection);

        List<HearingOrderAdditionalDocCollectionData> hearingOrderAdditionalDocuments
            = additionalHearingDocumentService.getHearingOrderAdditionalDocuments(caseDetails.getData());
        if (hearingOrderAdditionalDocuments != null && !hearingOrderAdditionalDocuments.isEmpty()) {
            hearingOrderAdditionalDocuments.clear();
        }
        caseData.put(HEARING_UPLOADED_DOCUMENT, hearingOrderAdditionalDocuments);
        return caseData;
    }

    public GenericAboutToStartOrSubmitCallbackResponse<Map<String, Object>> handleUploadApprovedOrderAboutToSubmit(CaseDetails caseDetails,
                                                                                                                   CaseDetails caseDetailsBefore,
                                                                                                                   String authorisationToken) {
        List<String> errors = new ArrayList<>();
        contestedOrderApprovedLetterService.generateAndStoreContestedOrderApprovedLetter(caseDetails, authorisationToken);

        try {
            additionalHearingDocumentService.createAndStoreAdditionalHearingDocumentsFromApprovedOrder(authorisationToken, caseDetails);
        } catch (CourtDetailsParseException e) {
            log.error(e.getMessage());
            errors.add(e.getMessage());
            return GenericAboutToStartOrSubmitCallbackResponse.<Map<String, Object>>builder().data(caseDetails.getData()).errors(errors).build();
        }

        hearingOrderService.appendLatestDraftDirectionOrderToJudgesAmendedDirectionOrders(caseDetails);
        if (isAnotherHearingToBeListed(caseDetails)) {
            approvedOrderNoticeOfHearingService.createAndStoreHearingNoticeDocumentPack(caseDetails, authorisationToken);
        }


        List<HearingOrderCollectionData> hearingOrderCollectionBefore
            = additionalHearingDocumentService.getApprovedHearingOrderCollection(caseDetailsBefore);
        if (hearingOrderCollectionBefore != null && !hearingOrderCollectionBefore.isEmpty()) {
            hearingOrderCollectionBefore.addAll(additionalHearingDocumentService.getApprovedHearingOrderCollection(caseDetails));
            caseDetails.getData().put(HEARING_ORDER_COLLECTION, hearingOrderCollectionBefore);
        }

        List<HearingOrderAdditionalDocCollectionData> orderAdditionalDocumentsBefore
            = additionalHearingDocumentService.getHearingOrderAdditionalDocuments(caseDetailsBefore.getData());
        if (orderAdditionalDocumentsBefore != null && !orderAdditionalDocumentsBefore.isEmpty()) {
            orderAdditionalDocumentsBefore.addAll(additionalHearingDocumentService.getHearingOrderAdditionalDocuments(caseDetails.getData()));
            caseDetails.getData().put(HEARING_UPLOADED_DOCUMENT, orderAdditionalDocumentsBefore);
        }
        return GenericAboutToStartOrSubmitCallbackResponse.<Map<String, Object>>builder().data(caseDetails.getData()).build();
    }

    private boolean isAnotherHearingToBeListed(CaseDetails caseDetails) {
        Optional<AdditionalHearingDirectionsCollection> latestHearingDirections =
            getLatestAdditionalHearingDirections(caseDetails);
        return latestHearingDirections.isPresent()
            && YES_VALUE.equals(latestHearingDirections.get().getIsAnotherHearingYN());
    }

    private Optional<AdditionalHearingDirectionsCollection> getLatestAdditionalHearingDirections(CaseDetails caseDetails) {
        List<Element<AdditionalHearingDirectionsCollection>> additionalHearingDetailsCollection =
            new ObjectMapper().convertValue(caseDetails.getData().get(HEARING_DIRECTION_DETAILS_COLLECTION),
                new TypeReference<>() {
                });

        return additionalHearingDetailsCollection != null && !additionalHearingDetailsCollection.isEmpty()
            ? Optional.of(additionalHearingDetailsCollection.get(additionalHearingDetailsCollection.size() - 1).getValue())
            : Optional.empty();
    }
}
