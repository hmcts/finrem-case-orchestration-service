package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicRadioList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicRadioListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HasSubmittedInfo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.CaseDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.agreed.AgreedDraftOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.agreed.AgreedDraftOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.OrderStatus;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.agreed.AgreedDraftOrderAdditionalDocumentsCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.agreed.AgreedPensionSharingAnnex;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.agreed.AgreedPensionSharingAnnexCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.agreed.UploadAgreedDraftOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.agreed.UploadAgreedDraftOrderCollection;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.ORDER_TYPE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.PSA_TYPE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;

@Service
@Slf4j
@RequiredArgsConstructor
public class DraftOrderService {

    private final IdamAuthService idamAuthService;

    public <T extends HasSubmittedInfo> T applySubmittedInfo(String userAuthorisation, T submittedInfo) {
        UserInfo userInfo = idamAuthService.getUserInfo(userAuthorisation);
        String submittedByName = userInfo.getName();
        submittedInfo.setSubmittedBy(submittedByName);
        submittedInfo.setSubmittedDate(LocalDateTime.now());
        return submittedInfo;
    }

    public boolean isOrdersSelected(List<String> uploadOrdersOrPsas) {
        return ofNullable(uploadOrdersOrPsas).orElse(List.of()).contains(ORDER_TYPE);
    }

    public boolean isPsaSelected(List<String> uploadOrdersOrPsas) {
        return ofNullable(uploadOrdersOrPsas).orElse(List.of()).contains(PSA_TYPE);
    }

    public List<AgreedDraftOrderCollection> processAgreedDraftOrders(UploadAgreedDraftOrder uploadAgreedDraftOrder, String userAuthorisation) {
        List<AgreedDraftOrderCollection> ret = new ArrayList<>();

        // First check if 'order' is selected
        if (isOrdersSelected(uploadAgreedDraftOrder.getUploadOrdersOrPsas())) {
            ofNullable(uploadAgreedDraftOrder.getUploadAgreedDraftOrderCollection()).orElse(List.of()).stream()
                .map(UploadAgreedDraftOrderCollection::getValue)
                .filter(Objects::nonNull)
                .map(udo -> mapToAgreedDraftOrder(udo, uploadAgreedDraftOrder, userAuthorisation))
                .map(orderDraftOrder -> AgreedDraftOrderCollection.builder()
                    .value(orderDraftOrder)
                    .build())
                .forEach(ret::add);
        }

        //check if 'psa' is selected
        if (isPsaSelected(uploadAgreedDraftOrder.getUploadOrdersOrPsas())) {
            ofNullable(uploadAgreedDraftOrder.getAgreedPsaCollection()).orElse(List.of()).stream()
                .map(AgreedPensionSharingAnnexCollection::getValue)
                .filter(Objects::nonNull)
                .map(uploadPsa -> mapToAgreedDraftOrderForPsa(uploadPsa, uploadAgreedDraftOrder, userAuthorisation))
                .map(psaDraftOrder -> AgreedDraftOrderCollection.builder()
                    .value(psaDraftOrder)
                    .build())
                .forEach(ret::add);
        }

        return ret;
    }

    private static void setUploadedOnBehalfOf(UploadAgreedDraftOrder uploadAgreedDraftOrder,
                                              AgreedDraftOrder.AgreedDraftOrderBuilder builder) {
        ofNullable(uploadAgreedDraftOrder.getUploadParty())
            .map(DynamicRadioList::getValue)
            .map(DynamicRadioListElement::getCode)
            .ifPresentOrElse(
                builder::uploadedOnBehalfOf,
                () -> log.error("Unexpected null 'uploadedParty' on upload agreed order journey.")
            );
    }

    private AgreedDraftOrder mapToAgreedDraftOrder(
        uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.agreed.UploadedDraftOrder uploadDraftOrder,
        UploadAgreedDraftOrder uploadAgreedDraftOrder, String userAuthorisation) {

        AgreedDraftOrder.AgreedDraftOrderBuilder builder = AgreedDraftOrder.builder();

        setUploadedOnBehalfOf(uploadAgreedDraftOrder, builder);
        // Map the draft order document
        ofNullable(uploadDraftOrder.getAgreedDraftOrderDocument()).ifPresent(builder::draftOrder);

        // Add additional attachments for orders only
        List<CaseDocumentCollection> attachments = ofNullable(uploadDraftOrder.getAgreedDraftOrderAdditionalDocumentsCollection())
            .orElse(List.of()).stream()
            .map(AgreedDraftOrderAdditionalDocumentsCollection::getValue)
            .filter(Objects::nonNull)
            .map(value -> CaseDocumentCollection.builder().value(value).build())
            .toList();
        if (!attachments.isEmpty()) {
            builder.attachments(attachments);
        }

        builder.resubmission(YesOrNo.forValue(ofNullable(uploadDraftOrder.getResubmission()).orElse(List.of()).contains(YES_VALUE)));
        builder.orderStatus(OrderStatus.TO_BE_REVIEWED);
        return applySubmittedInfo(userAuthorisation, builder.build());
    }

    private AgreedDraftOrder mapToAgreedDraftOrderForPsa(
        AgreedPensionSharingAnnex uploadPsa, UploadAgreedDraftOrder uploadAgreedDraftOrder, String userAuthorisation) {

        AgreedDraftOrder.AgreedDraftOrderBuilder builder = AgreedDraftOrder.builder();

        setUploadedOnBehalfOf(uploadAgreedDraftOrder, builder);

        // Map the PSA document
        ofNullable(uploadPsa).map(AgreedPensionSharingAnnex::getAgreedPensionSharingAnnexes).ifPresent(builder::pensionSharingAnnex);

        builder.orderStatus(OrderStatus.TO_BE_REVIEWED);
        return applySubmittedInfo(userAuthorisation, builder.build());
    }
}
