package uk.gov.hmcts.reform.finrem.caseorchestration.handler.draftorders.upload;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseAssignedUserRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseAssignedUserRolesResource;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HasSubmittedInfo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.CaseDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.agreed.AgreedDraftOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.suggested.SuggestedDraftOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.suggested.SuggestedDraftOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.suggested.SuggestedDraftOrderAdditionalDocumentsCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.suggested.SuggestedPensionSharingAnnex;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.suggested.SuggestedPensionSharingAnnexCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.suggested.UploadSuggestedDraftOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.suggested.UploadSuggestedDraftOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.suggested.UploadedDraftOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DraftOrdersWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseAssignedRoleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.DraftOrderService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamAuthService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory.DraftOrdersCategoriser;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty.CASE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty.RESPONDENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.DraftOrdersConstants.AGREED_DRAFT_ORDER_OPTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.DraftOrdersConstants.SUGGESTED_DRAFT_ORDER_OPTION;

@Slf4j
@Service
public class UploadDraftOrdersAboutToSubmitHandler extends FinremCallbackHandler {

    private final DraftOrdersCategoriser draftOrdersCategoriser;

    private final IdamAuthService idamAuthService;

    private final CaseAssignedRoleService caseAssignedRoleService;

    private final DraftOrderService draftOrderService;

    public UploadDraftOrdersAboutToSubmitHandler(FinremCaseDetailsMapper finremCaseDetailsMapper, DraftOrdersCategoriser draftOrdersCategoriser,
                                                 IdamAuthService idamAuthService, CaseAssignedRoleService caseAssignedRoleService,
                                                 DraftOrderService draftOrderService) {
        super(finremCaseDetailsMapper);
        this.draftOrdersCategoriser = draftOrdersCategoriser;
        this.idamAuthService = idamAuthService;
        this.caseAssignedRoleService = caseAssignedRoleService;
        this.draftOrderService = draftOrderService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.DRAFT_ORDERS.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        log.info("Invoking contested {} about to submit callback for Case ID: {}",
            callbackRequest.getEventType(), caseDetails.getId());
        FinremCaseData finremCaseData = caseDetails.getData();

        String userRole = checkRole(caseDetails.getId().toString(), userAuthorisation);

        String typeOfDraftOrder = finremCaseData.getDraftOrdersWrapper().getTypeOfDraftOrder();
        if (SUGGESTED_DRAFT_ORDER_OPTION.equals(typeOfDraftOrder)) {
            handleSuggestedDraftOrders(finremCaseData, userAuthorisation);
        } else if (AGREED_DRAFT_ORDER_OPTION.equals(typeOfDraftOrder)) {
            handleAgreedDraftOrder(finremCaseData, userAuthorisation);
        }

        draftOrdersCategoriser.categoriseDocuments(finremCaseData, userRole);

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(finremCaseData).build();
    }

    private void handleAgreedDraftOrder(FinremCaseData finremCaseData, String userAuthorisation) {
        DraftOrdersWrapper draftOrdersWrapper = finremCaseData.getDraftOrdersWrapper();

        List<AgreedDraftOrderCollection> newAgreedDraftOrderCollections = draftOrderService
            .processAgreedDraftOrders(draftOrdersWrapper.getUploadAgreedDraftOrder(), userAuthorisation);
        draftOrderService.populateDraftOrdersReviewCollection(finremCaseData, draftOrdersWrapper.getUploadAgreedDraftOrder(),
            newAgreedDraftOrderCollections);

        draftOrdersWrapper.appendAgreedDraftOrderCollection(newAgreedDraftOrderCollections);
    }

    private void handleSuggestedDraftOrders(FinremCaseData finremCaseData, String userAuthorisation) {
        DraftOrdersWrapper draftOrdersWrapper = finremCaseData.getDraftOrdersWrapper();
        UploadSuggestedDraftOrder uploadSuggestedDraftOrder = draftOrdersWrapper.getUploadSuggestedDraftOrder();

        if (uploadSuggestedDraftOrder != null) {
            List<SuggestedDraftOrderCollection> newSuggestedDraftOrderCollections = processSuggestedDraftOrders(uploadSuggestedDraftOrder,
                userAuthorisation);

            List<SuggestedDraftOrderCollection> existingSuggestedDraftOrderCollections =
                getExistingSuggestedDraftOrderCollections(draftOrdersWrapper);

            existingSuggestedDraftOrderCollections.addAll(newSuggestedDraftOrderCollections);
            draftOrdersWrapper.setSuggestedDraftOrderCollection(existingSuggestedDraftOrderCollections);
        }
    }

    private List<SuggestedDraftOrderCollection> processSuggestedDraftOrders(UploadSuggestedDraftOrder uploadSuggestedDraftOrder,
                                                                            String userAuthorisation) {
        List<SuggestedDraftOrderCollection> newSuggestedDraftOrderCollections = new ArrayList<>();

        // First check if 'order' is selected
        if (isOrdersSelected(uploadSuggestedDraftOrder.getUploadOrdersOrPsas())) {
            for (UploadSuggestedDraftOrderCollection uploadCollection : uploadSuggestedDraftOrder.getUploadSuggestedDraftOrderCollection()) {
                UploadedDraftOrder uploadDraftOrder = uploadCollection.getValue();

                // Create the draft order element
                SuggestedDraftOrder orderDraftOrder = mapToSuggestedDraftOrder(uploadDraftOrder, uploadSuggestedDraftOrder, userAuthorisation);

                SuggestedDraftOrderCollection orderCollection =
                    SuggestedDraftOrderCollection.builder()
                        .value(orderDraftOrder)
                        .build();

                newSuggestedDraftOrderCollections.add(orderCollection);
            }
        }

        //check if 'psa' is selected
        if (isPsaSelected(uploadSuggestedDraftOrder.getUploadOrdersOrPsas())) {
            for (SuggestedPensionSharingAnnexCollection psaCollection : uploadSuggestedDraftOrder.getSuggestedPsaCollection()) {
                SuggestedPensionSharingAnnex uploadPsa = psaCollection.getValue();

                //create the PSA element
                SuggestedDraftOrder psaDraftOrder = mapToSuggestedDraftOrderForPsa(uploadPsa, uploadSuggestedDraftOrder, userAuthorisation);

                SuggestedDraftOrderCollection psaOrderCollection =
                    SuggestedDraftOrderCollection.builder()
                        .value(psaDraftOrder)
                        .build();

                newSuggestedDraftOrderCollections.add(psaOrderCollection);
            }
        }

        return newSuggestedDraftOrderCollections;
    }

    private SuggestedDraftOrder mapToSuggestedDraftOrder(
        UploadedDraftOrder uploadDraftOrder, UploadSuggestedDraftOrder uploadSuggestedDraftOrder, String userAuthorisation) {

        SuggestedDraftOrder.SuggestedDraftOrderBuilder suggestedDraftOrderBuilder = SuggestedDraftOrder.builder();

        if (uploadSuggestedDraftOrder.getUploadParty() != null) {
            suggestedDraftOrderBuilder.uploadedOnBehalfOf(uploadSuggestedDraftOrder.getUploadParty().getValue().getCode());
        }

        //Map the draft order document
        if (!ObjectUtils.isEmpty(uploadDraftOrder.getSuggestedDraftOrderDocument())) {
            suggestedDraftOrderBuilder.draftOrder(uploadDraftOrder.getSuggestedDraftOrderDocument());
        }

        // Add additional attachments for orders only
        if (!ObjectUtils.isEmpty(uploadDraftOrder.getSuggestedDraftOrderAdditionalDocumentsCollection())) {
            List<CaseDocumentCollection> attachments = new ArrayList<>();
            for (SuggestedDraftOrderAdditionalDocumentsCollection additionalDoc :
                uploadDraftOrder.getSuggestedDraftOrderAdditionalDocumentsCollection()) {
                if (additionalDoc.getValue() != null) {
                    attachments.add(CaseDocumentCollection.builder()
                        .value(additionalDoc.getValue())
                        .build());
                }
            }
            suggestedDraftOrderBuilder.attachments(attachments);
        }

        return applySubmittedInfo(userAuthorisation, suggestedDraftOrderBuilder.build());
    }

    private SuggestedDraftOrder mapToSuggestedDraftOrderForPsa(
        SuggestedPensionSharingAnnex uploadPsa, UploadSuggestedDraftOrder uploadSuggestedDraftOrder, String userAuthorisation) {

        SuggestedDraftOrder.SuggestedDraftOrderBuilder suggestedDraftOrderBuilder = SuggestedDraftOrder.builder();

        if (uploadSuggestedDraftOrder.getUploadParty() != null) {
            suggestedDraftOrderBuilder.uploadedOnBehalfOf(uploadSuggestedDraftOrder.getUploadParty().getValue().getCode());
        }

        // Map the PSA document
        if (uploadPsa != null) {
            suggestedDraftOrderBuilder.pensionSharingAnnex(uploadPsa.getSuggestedPensionSharingAnnexes());
        }

        return applySubmittedInfo(userAuthorisation, suggestedDraftOrderBuilder.build());
    }

    private List<SuggestedDraftOrderCollection> getExistingSuggestedDraftOrderCollections(DraftOrdersWrapper draftOrdersWrapper) {
        List<SuggestedDraftOrderCollection> existingSuggestedDraftOrderCollections = draftOrdersWrapper.getSuggestedDraftOrderCollection();
        if (existingSuggestedDraftOrderCollections == null) {
            existingSuggestedDraftOrderCollections = new ArrayList<>();
        }
        return existingSuggestedDraftOrderCollections;
    }

    private String checkRole(String id, String auth) {

        CaseAssignedUserRolesResource caseAssignedUserRole =
            caseAssignedRoleService.getCaseAssignedUserRole(id, auth);

        if (caseAssignedUserRole != null) {
            List<CaseAssignedUserRole> caseAssignedUserRoleList = caseAssignedUserRole.getCaseAssignedUserRoles();
            if (!caseAssignedUserRoleList.isEmpty()) {
                String loggedInUserCaseRole = caseAssignedUserRoleList.get(0).getCaseRole();

                if (loggedInUserCaseRole.contains(CaseRole.APP_SOLICITOR.getCcdCode())) {
                    return APPLICANT.getValue();
                } else if (loggedInUserCaseRole.contains(CaseRole.RESP_SOLICITOR.getCcdCode())) {
                    return RESPONDENT.getValue();
                }
            }
        }

        return CASE.getValue();
    }

    private <T extends HasSubmittedInfo> T applySubmittedInfo(String userAuthorisation, T submittedInfo) {
        UserInfo userInfo = idamAuthService.getUserInfo(userAuthorisation);
        String submittedByName = userInfo.getName();
        submittedInfo.setSubmittedBy(submittedByName);
        submittedInfo.setSubmittedDate(LocalDateTime.now());
        return submittedInfo;
    }

    private boolean isOrdersSelected(List<String> uploadOrdersOrPsas) {
        return draftOrderService.isOrdersSelected(uploadOrdersOrPsas);
    }

    private boolean isPsaSelected(List<String> uploadOrdersOrPsas) {
        return draftOrderService.isPsaSelected(uploadOrdersOrPsas);
    }
}
