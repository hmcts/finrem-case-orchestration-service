package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.google.common.collect.Iterables;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.CourtDetailsParseException;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.InvalidCaseDataException;
import uk.gov.hmcts.reform.finrem.ccd.callback.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.ccd.domain.DirectionOrderCollection;
import uk.gov.hmcts.reform.finrem.ccd.domain.Document;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.HearingDirectionDetail;
import uk.gov.hmcts.reform.finrem.ccd.domain.HearingDirectionDetailsCollection;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static uk.gov.hmcts.reform.finrem.ccd.domain.YesOrNo.isYes;

@Service
@RequiredArgsConstructor
@Slf4j
public class UploadApprovedOrderService {

    private final HearingOrderService hearingOrderService;
    private final ContestedOrderApprovedLetterService contestedOrderApprovedLetterService;
    private final AdditionalHearingDocumentService additionalHearingDocumentService;
    private final GenericDocumentService genericDocumentService;
    private final ApprovedOrderNoticeOfHearingService approvedOrderNoticeOfHearingService;

    public FinremCaseData prepareFieldsForOrderApprovedCoverLetter(FinremCaseDetails caseDetails) {
        FinremCaseData caseData = caseDetails.getCaseData();
        caseData.setOrderApprovedJudgeType(null);
        caseData.setOrderApprovedJudgeName(null);
        caseData.setOrderApprovedDate(null);
        caseData.setHearingNoticeDocumentPack(null);

        return caseData;
    }

    public AboutToStartOrSubmitCallbackResponse handleUploadApprovedOrderAboutToSubmit(FinremCaseDetails caseDetails,
                                                                                       String authorisationToken) {
        List<String> errors = new ArrayList<>();
        convertToPdfAndStoreApprovedHearingOrder(caseDetails, authorisationToken);
        contestedOrderApprovedLetterService.generateAndStoreContestedOrderApprovedLetter(caseDetails, authorisationToken);

        try {
            additionalHearingDocumentService.createAndStoreAdditionalHearingDocumentsFromApprovedOrder(authorisationToken, caseDetails);
        } catch (CourtDetailsParseException e) {
            log.error(e.getMessage());
            errors.add(e.getMessage());
            return AboutToStartOrSubmitCallbackResponse.builder().data(caseDetails.getCaseData()).errors(errors).build();
        }

        hearingOrderService.appendLatestDraftDirectionOrderToJudgesAmendedDirectionOrders(caseDetails);
        if (isAnotherHearingToBeListed(caseDetails)) {
            approvedOrderNoticeOfHearingService.createAndStoreHearingNoticeDocumentPack(caseDetails, authorisationToken);
        }
        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDetails.getCaseData()).build();
    }

    private void convertToPdfAndStoreApprovedHearingOrder(FinremCaseDetails caseDetails, String authorisationToken) {
        FinremCaseData caseData = caseDetails.getCaseData();
        List<DirectionOrderCollection> hearingOrders = caseData.getUploadHearingOrder();
        Optional<DirectionOrderCollection> latestHearingOrder = getLatestHearingOrder(hearingOrders);

        if (latestHearingOrder.isPresent()) {
            Document pdfHearingOrder = genericDocumentService.convertDocumentIfNotPdfAlready(
                latestHearingOrder.get().getValue().getUploadDraftDocument(), authorisationToken);
            hearingOrderService.updateCaseDataForLatestHearingOrderCollection(caseData, pdfHearingOrder);
        } else {
            throw new InvalidCaseDataException(BAD_REQUEST.value(), "Missing data from callbackRequest.");
        }
    }

    private boolean isAnotherHearingToBeListed(FinremCaseDetails caseDetails) {
        Optional<HearingDirectionDetail> latestHearingDirections = getLatestAdditionalHearingDirections(caseDetails);

        return latestHearingDirections.isPresent() && isYes(latestHearingDirections.get().getIsAnotherHearingYN());
    }

    private Optional<HearingDirectionDetail> getLatestAdditionalHearingDirections(FinremCaseDetails caseDetails) {
        List<HearingDirectionDetailsCollection> additionalHearingDetailsCollection
            = caseDetails.getCaseData().getHearingDirectionDetailsCollection();

        return !CollectionUtils.isEmpty(additionalHearingDetailsCollection)
            ? Optional.of(Iterables.getLast(additionalHearingDetailsCollection).getValue())
            : Optional.empty();
    }

    private Optional<DirectionOrderCollection> getLatestHearingOrder(List<DirectionOrderCollection> directionOrders) {
        return !CollectionUtils.isEmpty(directionOrders)
            ? Optional.of(Iterables.getLast(directionOrders))
            : Optional.empty();
    }
}
