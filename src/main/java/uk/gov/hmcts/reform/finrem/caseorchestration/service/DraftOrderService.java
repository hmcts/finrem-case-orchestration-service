package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.InvalidCaseDataException;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicRadioList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicRadioListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Reviewable;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.HasSubmittedInfo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.agreed.AgreedDraftOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.agreed.AgreedDraftOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrderDocReviewCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrderDocumentReview;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrdersReview;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrdersReviewCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.OrderStatus;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.OrderType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.PsaDocReviewCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.PsaDocumentReview;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.AdditionalDocumentsCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.agreed.AgreedPensionSharingAnnex;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.agreed.AgreedPensionSharingAnnexCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.agreed.UploadAgreedDraftOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.agreed.UploadAgreedDraftOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DraftOrdersWrapper;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.DraftOrdersConstants.ACCELERATED_ORDER_OPTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.DraftOrdersConstants.AGREED_DRAFT_ORDER_OPTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.DraftOrdersConstants.ORDER_TYPE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.DraftOrdersConstants.PSA_TYPE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.DraftOrdersConstants.SUGGESTED_DRAFT_ORDER_OPTION;

@Service
@Slf4j
@RequiredArgsConstructor
public class DraftOrderService {
    private final IdamAuthService idamAuthService;
    private final HearingService hearingService;

    public <T extends HasSubmittedInfo> T applySubmittedInfo(String userAuthorisation, T submittedInfo) {
        UserDetails userDetails = idamAuthService.getUserDetails(userAuthorisation);
        String submittedByName = userDetails.getFullName();
        submittedInfo.setSubmittedBy(submittedByName);
        // capture email address if it's not uploaded by caseworker.
        if (isEmpty(submittedInfo.getUploadedOnBehalfOf())) {
            submittedInfo.setSubmittedByEmail(userDetails.getEmail());
        }
        submittedInfo.setSubmittedDate(LocalDateTime.now());
        return submittedInfo;
    }

    public boolean isOrdersSelected(List<String> uploadOrdersOrPsas) {
        return ofNullable(uploadOrdersOrPsas).orElse(List.of()).contains(ORDER_TYPE);
    }

    public boolean isPsaSelected(List<String> uploadOrdersOrPsas) {
        return ofNullable(uploadOrdersOrPsas).orElse(List.of()).contains(PSA_TYPE);
    }

    public List<AgreedDraftOrderCollection> processAgreedDraftOrders(DraftOrdersWrapper draftOrdersWrapper, String userAuthorisation) {
        OrderType orderType = getOrderType(draftOrdersWrapper.getTypeOfDraftOrder());

        List<AgreedDraftOrderCollection> processedDraftOrders = new ArrayList<>();
        UploadAgreedDraftOrder uploadAgreedDraftOrder = draftOrdersWrapper.getUploadAgreedDraftOrder();

        // First check if 'order' is selected
        if (isOrdersSelected(uploadAgreedDraftOrder.getUploadOrdersOrPsas())) {
            ofNullable(uploadAgreedDraftOrder.getUploadAgreedDraftOrderCollection())
                .orElse(List.of())
                .stream()
                .map(UploadAgreedDraftOrderCollection::getValue)
                .filter(Objects::nonNull)
                .map(uploadedOrder -> mapToAgreedDraftOrder(
                    uploadedOrder,
                    uploadAgreedDraftOrder,
                    userAuthorisation,
                    orderType
                ))
                .map(orderDraftOrder -> AgreedDraftOrderCollection.builder()
                .value(orderDraftOrder)
                .build())
                .forEach(processedDraftOrders::add);
        }

        //check if 'psa' is selected
        if (isPsaSelected(uploadAgreedDraftOrder.getUploadOrdersOrPsas())) {
            ofNullable(uploadAgreedDraftOrder.getAgreedPsaCollection())
                .orElse(List.of())
                .stream()
                .map(AgreedPensionSharingAnnexCollection::getValue)
                .filter(Objects::nonNull)
                .map(uploadPsa -> mapToAgreedDraftOrderForPsa(
                    uploadPsa,
                    uploadAgreedDraftOrder,
                    userAuthorisation,
                    orderType
                ))
                .map(psaDraftOrder -> AgreedDraftOrderCollection.builder()
                    .value(psaDraftOrder)
                    .build())
                .forEach(processedDraftOrders::add);
        }

        return processedDraftOrders;
    }

    private OrderType getOrderType(String typeOfDraftOrder) {

        return switch (typeOfDraftOrder) {
            case AGREED_DRAFT_ORDER_OPTION -> OrderType.AGREED_ORDER;
            case ACCELERATED_ORDER_OPTION -> OrderType.ACCELERATED_PROCEDURE_ORDER;
            default -> throw new InvalidCaseDataException(
                BAD_REQUEST.value(),
                format("Unsupported draft order type: %s", typeOfDraftOrder)
            );
        };
    }

    private void setUploadedOnBehalfOf(UploadAgreedDraftOrder uploadAgreedDraftOrder,
                                       AgreedDraftOrder.AgreedDraftOrderBuilder builder) {
        // The `uploadParty` could be null if the draft order is uploaded by external parties.
        ofNullable(uploadAgreedDraftOrder.getUploadParty())
            .map(DynamicRadioList::getValue)
            .map(DynamicRadioListElement::getCode)
            .ifPresent(builder::uploadedOnBehalfOf);
    }

    private AgreedDraftOrder mapToAgreedDraftOrder(
        uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.agreed.UploadedDraftOrder uploadDraftOrder,
        UploadAgreedDraftOrder uploadAgreedDraftOrder, String userAuthorisation, OrderType orderType) {

        AgreedDraftOrder.AgreedDraftOrderBuilder builder = AgreedDraftOrder.builder();

        setUploadedOnBehalfOf(uploadAgreedDraftOrder, builder);
        // Map the draft order document
        ofNullable(uploadDraftOrder.getAgreedDraftOrderDocument()).ifPresent(builder::draftOrder);

        // Add additional attachments for orders only
        List<DocumentCollectionItem> attachments = ofNullable(uploadDraftOrder.getAdditionalDocuments())
            .orElse(List.of()).stream()
            .map(AdditionalDocumentsCollection::getValue)
            .filter(Objects::nonNull)
            .map(value -> DocumentCollectionItem.builder().value(value.getOrderAttachment()).build())
            .toList();
        if (!attachments.isEmpty()) {
            builder.attachments(attachments);
        }

        builder.orderType(orderType);
        builder.resubmission(YesOrNo.forValue(ofNullable(uploadDraftOrder.getResubmission()).orElse(List.of()).contains(YES_VALUE)));
        builder.orderStatus(OrderStatus.TO_BE_REVIEWED);
        return applySubmittedInfo(userAuthorisation, builder.build());
    }

    private AgreedDraftOrder mapToAgreedDraftOrderForPsa(
        AgreedPensionSharingAnnex uploadPsa, UploadAgreedDraftOrder uploadAgreedDraftOrder, String userAuthorisation, OrderType orderType) {

        AgreedDraftOrder.AgreedDraftOrderBuilder builder = AgreedDraftOrder.builder();

        setUploadedOnBehalfOf(uploadAgreedDraftOrder, builder);

        // Map the PSA document
        ofNullable(uploadPsa).map(AgreedPensionSharingAnnex::getAgreedPensionSharingAnnexes).ifPresent(builder::pensionSharingAnnex);

        builder.orderType(orderType);
        builder.orderStatus(OrderStatus.TO_BE_REVIEWED);
        return applySubmittedInfo(userAuthorisation, builder.build());
    }

    public void populateDraftOrdersReviewCollection(FinremCaseData finremCaseData, UploadAgreedDraftOrder uploadAgreedDraftOrder,
                                                    List<AgreedDraftOrderCollection> newAgreedDraftOrderCollection) {
        // ensure non-null hearing details
        if (uploadAgreedDraftOrder.getHearingDetails() == null) {
            throw new InvalidCaseDataException(BAD_REQUEST.value(),
                format("Unexpected null hearing details for Case ID: %s", finremCaseData.getCcdCaseId()));
        }

        // ensure non-null judge
        if (YesOrNo.YES.equals(uploadAgreedDraftOrder.getJudgeKnownAtHearing()) && uploadAgreedDraftOrder.getJudge() == null) {
            throw new InvalidCaseDataException(BAD_REQUEST.value(),
                format("Unexpected null judge for Case ID: %s", finremCaseData.getCcdCaseId()));
        }

        String hearingType = hearingService.getHearingType(finremCaseData, uploadAgreedDraftOrder.getHearingDetails().getValue());
        LocalDate hearingDate = hearingService.getHearingDate(finremCaseData, uploadAgreedDraftOrder.getHearingDetails().getValue());
        String hearingTime = hearingService.getHearingTime(finremCaseData, uploadAgreedDraftOrder.getHearingDetails().getValue());
        String hearingJudge = uploadAgreedDraftOrder.getJudge();

        // Check if DraftOrdersReview with the specified key exists
        DraftOrdersReview existingDraftOrderReview = ofNullable(finremCaseData.getDraftOrdersWrapper().getDraftOrdersReviewCollection())
            .orElse(List.of()).stream()
            .map(DraftOrdersReviewCollection::getValue)
            .filter(Objects::nonNull)
            .filter(dor -> dor.getHearingType().equals(hearingType)
                && dor.getHearingDate().equals(hearingDate)
                && dor.getHearingTime().equals(hearingTime)
                && Objects.equals(dor.getHearingJudge(), hearingJudge))
            .findFirst()
            .orElse(null);

        DraftOrdersReview newDraftOrderReview;
        if (existingDraftOrderReview == null) {
            // Create new DraftOrdersReview if it doesn't exist
            newDraftOrderReview = DraftOrdersReview.builder()
                .hearingType(hearingType)
                .hearingDate(hearingDate)
                .hearingTime(hearingTime)
                .hearingJudge(hearingJudge)
                .psaDocReviewCollection(new ArrayList<>())
                .draftOrderDocReviewCollection(new ArrayList<>())
                .build();

            // Append new DraftOrdersReview to the collection
            finremCaseData.getDraftOrdersWrapper().appendDraftOrdersReviewCollection(List.of(
                DraftOrdersReviewCollection.builder().value(newDraftOrderReview).build()
            ));
        } else {
            newDraftOrderReview = existingDraftOrderReview; // Use the existing DraftOrdersReview
        }

        // Process AgreedDraftOrderCollection
        List<DraftOrderDocReviewCollection> draftOrderDocReviewCollection = new ArrayList<>();
        ofNullable(newAgreedDraftOrderCollection).orElse(List.of()).stream()
            .filter(Objects::nonNull)
            .map(AgreedDraftOrderCollection::getValue)
            .filter(Objects::nonNull)
            .filter(ado -> ado.getDraftOrder() != null)
            .map(ado -> DraftOrderDocReviewCollection.builder()
                .value(DraftOrderDocumentReview.builder()
                    .hearingType(hearingType)
                    .orderType(ado.getOrderType())
                    .orderStatus(ado.getOrderStatus())
                    .draftOrderDocument(ado.getDraftOrder())
                    .submittedBy(ado.getSubmittedBy())
                    .submittedByEmail(ado.getSubmittedByEmail())
                    .orderFiledBy(uploadAgreedDraftOrder.getOrderFiledBy())
                    .uploadedOnBehalfOf(ado.getUploadedOnBehalfOf())
                    .submittedDate(ado.getSubmittedDate())
                    .resubmission(ado.getResubmission())
                    .attachments(ado.getAttachments())
                    .build())
                .build())
            .forEach(draftOrderDocReviewCollection::add);
        newDraftOrderReview.getDraftOrderDocReviewCollection().addAll(draftOrderDocReviewCollection);

        List<PsaDocReviewCollection> psaDocReviewCollection = new ArrayList<>();
        ofNullable(newAgreedDraftOrderCollection).orElse(List.of()).stream()
            .filter(Objects::nonNull)
            .map(AgreedDraftOrderCollection::getValue)
            .filter(Objects::nonNull)
            .filter(ado -> ado.getPensionSharingAnnex() != null)
            .map(ado -> PsaDocReviewCollection.builder()
                .value(PsaDocumentReview.builder()
                    .hearingType(hearingType)
                    .orderType(ado.getOrderType())
                    .orderStatus(ado.getOrderStatus())
                    .psaDocument(ado.getPensionSharingAnnex())
                    .submittedBy(ado.getSubmittedBy())
                    .submittedByEmail(ado.getSubmittedByEmail())
                    .orderFiledBy(uploadAgreedDraftOrder.getOrderFiledBy())
                    .uploadedOnBehalfOf(ado.getUploadedOnBehalfOf())
                    .submittedDate(ado.getSubmittedDate())
                    .resubmission(ado.getResubmission())
                    .build())
                .build())
            .forEach(psaDocReviewCollection::add);
        newDraftOrderReview.getPsaDocReviewCollection().addAll(psaDocReviewCollection);
    }

    public boolean isDraftOrderReviewOverdue(FinremCaseDetails caseDetails, int daysSinceOrderUpload) {
        return !getDraftOrderReviewOverdue(caseDetails, daysSinceOrderUpload).isEmpty();
    }

    public List<DraftOrdersReview> getDraftOrderReviewOverdue(FinremCaseDetails caseDetails, int daysSinceOrderUpload) {
        DraftOrdersWrapper draftOrdersWrapper = caseDetails.getData().getDraftOrdersWrapper();
        return getDraftOrderReviewOverdue(draftOrdersWrapper, daysSinceOrderUpload);
    }

    private List<DraftOrdersReview> getDraftOrderReviewOverdue(DraftOrdersWrapper draftOrdersWrapper, int daysSinceOrderUpload) {
        LocalDate thresholdDate = LocalDate.now().minusDays(daysSinceOrderUpload);
        log.info("thresholdDate for daysSinceOrderUpload={}: {}", daysSinceOrderUpload, thresholdDate);

        return ofNullable(draftOrdersWrapper.getDraftOrdersReviewCollection()).orElse(List.of()).stream()
            .map(DraftOrdersReviewCollection::getValue) // Get DraftOrdersReview from collection
            .filter(draftOrdersReview -> draftOrdersReview.getHearingJudge() != null)
            .filter(draftOrderReview -> draftOrderReview.getEarliestToBeReviewedOrderDate() != null
                && draftOrderReview.getEarliestToBeReviewedOrderDate().isBefore(thresholdDate)) // Check the date condition
            .toList();
    }

    /**
     * Sets the notification sent date on any Draft Order or Pension Sharing Annex documents where a review is overdue.
     *
     * @param draftOrdersReview    draft order documents for a hearing
     * @param daysSinceOrderUpload threshold days for when a review becomes overdue
     */
    public void updateOverdueDocuments(DraftOrdersReview draftOrdersReview, int daysSinceOrderUpload) {
        LocalDate thresholdDate = LocalDate.now().minusDays(daysSinceOrderUpload);
        LocalDateTime notificationSentDate = LocalDateTime.now();
        draftOrdersReview.getDraftOrderDocReviewCollection().stream()
            .map(DraftOrderDocReviewCollection::getValue)
            .filter(d -> isOverdue(d, thresholdDate))
            .toList().forEach(d -> d.setNotificationSentDate(notificationSentDate));

        draftOrdersReview.getPsaDocReviewCollection().stream()
            .map(PsaDocReviewCollection::getValue)
            .filter(d -> isOverdue(d, thresholdDate))
            .toList().forEach(d -> d.setNotificationSentDate(notificationSentDate));
    }

    private boolean isOverdue(Reviewable reviewable, LocalDate thresholdDate) {
        return OrderStatus.TO_BE_REVIEWED.equals(reviewable.getOrderStatus())
            && reviewable.getSubmittedDate().isBefore(thresholdDate.atStartOfDay())
            && reviewable.getNotificationSentDate() == null;
    }

    public void clearEmptyOrdersInDraftOrdersReviewCollection(FinremCaseData caseData) {

        if (CollectionUtils.isEmpty(caseData.getDraftOrdersWrapper().getDraftOrdersReviewCollection())) {
            caseData.getDraftOrdersWrapper().setDraftOrdersReviewCollection(new ArrayList<>());
        } else {
            List<DraftOrdersReviewCollection> draftOrdersReviewCollection =
                new ArrayList<>(caseData.getDraftOrdersWrapper().getDraftOrdersReviewCollection());

            //Remove any empty reviews that don't contain any draft orders or pension sharing annexes
            draftOrdersReviewCollection.removeIf(review ->
                CollectionUtils.isEmpty(review.getValue().getDraftOrderDocReviewCollection())
                    && CollectionUtils.isEmpty(review.getValue().getPsaDocReviewCollection())
            );

            // Check for unreviewedDocuments
            boolean hasUnreviewedDocuments = caseData.getDraftOrdersWrapper().getDraftOrdersReviewCollection().stream()
                .anyMatch(review ->
                    !CollectionUtils.isEmpty(review.getValue().getDraftOrderDocReviewCollection())
                        || !CollectionUtils.isEmpty(review.getValue().getPsaDocReviewCollection()));

            caseData.getDraftOrdersWrapper().setIsUnreviewedDocumentPresent(hasUnreviewedDocuments ? YesOrNo.YES : YesOrNo.NO);
            caseData.getDraftOrdersWrapper().setDraftOrdersReviewCollection(draftOrdersReviewCollection);
        }
    }
}
