package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
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

    public List<AgreedDraftOrderCollection> processAgreedDraftOrders(UploadAgreedDraftOrder uploadAgreedDraftOrder,
                                                                      String userAuthorisation) {
        List<AgreedDraftOrderCollection> ret = new ArrayList<>();

        // First check if 'order' is selected
        if (isOrdersSelected(uploadAgreedDraftOrder.getUploadOrdersOrPsas())) {
            uploadAgreedDraftOrder.getUploadAgreedDraftOrderCollection().stream()
                .map(UploadAgreedDraftOrderCollection::getValue)
                .map(udo -> mapToAgreedDraftOrder(udo, uploadAgreedDraftOrder, userAuthorisation))
                .map(orderDraftOrder -> AgreedDraftOrderCollection.builder()
                    .value(orderDraftOrder)
                    .build())
                .forEach(ret::add);
        }

        //check if 'psa' is selected
        if (isPsaSelected(uploadAgreedDraftOrder.getUploadOrdersOrPsas())) {
            uploadAgreedDraftOrder.getAgreedPsaCollection().stream()
                .map(AgreedPensionSharingAnnexCollection::getValue)
                .map(uploadPsa -> mapToAgreedDraftOrderForPsa(uploadPsa, uploadAgreedDraftOrder, userAuthorisation))
                .map(psaDraftOrder -> AgreedDraftOrderCollection.builder()
                    .value(psaDraftOrder)
                    .build())
                .forEach(ret::add);
        }

        return ret;
    }

    private AgreedDraftOrder mapToAgreedDraftOrder(
        uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.agreed.UploadedDraftOrder uploadDraftOrder,
        UploadAgreedDraftOrder uploadAgreedDraftOrder, String userAuthorisation) {

        AgreedDraftOrder.AgreedDraftOrderBuilder builder = AgreedDraftOrder.builder();

        if (uploadAgreedDraftOrder.getUploadParty() != null) {
            builder.uploadedOnBehalfOf(uploadAgreedDraftOrder.getUploadParty().getValue().getCode());
        }

        // Map the draft order document
        if (!ObjectUtils.isEmpty(uploadDraftOrder.getAgreedDraftOrderDocument())) {
            builder.draftOrder(uploadDraftOrder.getAgreedDraftOrderDocument());
        }

        // Add additional attachments for orders only
        if (!ObjectUtils.isEmpty(uploadDraftOrder.getAgreedDraftOrderAdditionalDocumentsCollection())) {
            List<CaseDocumentCollection> attachments = uploadDraftOrder.getAgreedDraftOrderAdditionalDocumentsCollection().stream()
                .map(AgreedDraftOrderAdditionalDocumentsCollection::getValue)
                .filter(Objects::nonNull)
                .map(value -> CaseDocumentCollection.builder().value(value).build())
                .toList();
            builder.attachments(attachments);
        }

        builder.resubmission(YesOrNo.forValue(
            ofNullable(uploadDraftOrder.getResubmission()).orElse(List.of()).contains(YES_VALUE)
        ));

        builder.orderStatus(OrderStatus.TO_BE_REVIEWED);
        return applySubmittedInfo(userAuthorisation, builder.build());
    }

    private AgreedDraftOrder mapToAgreedDraftOrderForPsa(
        AgreedPensionSharingAnnex uploadPsa, UploadAgreedDraftOrder uploadAgreedDraftOrder, String userAuthorisation) {

        AgreedDraftOrder.AgreedDraftOrderBuilder builder = AgreedDraftOrder.builder();

        if (uploadAgreedDraftOrder.getUploadParty() != null) {
            builder.uploadedOnBehalfOf(uploadAgreedDraftOrder.getUploadParty().getValue().getCode());
        }

        // Map the PSA document
        if (uploadPsa != null) {
            builder.pensionSharingAnnex(uploadPsa.getAgreedPensionSharingAnnexes());
        }

        builder.orderStatus(OrderStatus.TO_BE_REVIEWED);
        return applySubmittedInfo(userAuthorisation, builder.build());
    }
}
