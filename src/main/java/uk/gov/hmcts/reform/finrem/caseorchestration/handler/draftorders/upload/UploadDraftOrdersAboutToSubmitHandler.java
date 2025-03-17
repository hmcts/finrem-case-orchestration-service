package uk.gov.hmcts.reform.finrem.caseorchestration.handler.draftorders.upload;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.helper.DocumentWarningsHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseAssignedUserRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseAssignedUserRolesResource;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.State;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.HasSubmittedInfo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.OrderFiledBy;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.agreed.AgreedDraftOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.suggested.SuggestedDraftOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.suggested.SuggestedDraftOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.AdditionalDocumentsCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.agreed.AgreedPensionSharingAnnexCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.agreed.UploadAgreedDraftOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.agreed.UploadAgreedDraftOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.suggested.SuggestedPensionSharingAnnex;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.suggested.SuggestedPensionSharingAnnexCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.suggested.UploadSuggestedDraftOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.suggested.UploadSuggestedDraftOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.suggested.UploadedDraftOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DraftOrdersWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseAssignedRoleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.DraftOrderService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory.DraftOrdersCategoriser;

import java.util.ArrayList;
import java.util.List;

import static java.util.Optional.ofNullable;
import static java.util.stream.Stream.concat;
import static org.apache.commons.collections4.ListUtils.emptyIfNull;
import static uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.DRAFT_ORDERS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole.CASEWORKER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.DraftOrdersConstants.AGREED_DRAFT_ORDER_OPTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.DraftOrdersConstants.SUGGESTED_DRAFT_ORDER_OPTION;

@Slf4j
@Service
public class UploadDraftOrdersAboutToSubmitHandler extends FinremCallbackHandler {

    private final DraftOrdersCategoriser draftOrdersCategoriser;

    private final CaseAssignedRoleService caseAssignedRoleService;

    private final DraftOrderService draftOrderService;

    private final DocumentWarningsHelper documentWarningsHelper;

    public UploadDraftOrdersAboutToSubmitHandler(FinremCaseDetailsMapper finremCaseDetailsMapper, DraftOrdersCategoriser draftOrdersCategoriser,
                                                 CaseAssignedRoleService caseAssignedRoleService, DraftOrderService draftOrderService,
                                                 DocumentWarningsHelper documentWarningsHelper) {
        super(finremCaseDetailsMapper);
        this.draftOrdersCategoriser = draftOrdersCategoriser;
        this.caseAssignedRoleService = caseAssignedRoleService;
        this.draftOrderService = draftOrderService;
        this.documentWarningsHelper = documentWarningsHelper;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return ABOUT_TO_SUBMIT.equals(callbackType) && CONTESTED.equals(caseType) && DRAFT_ORDERS.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest, String userAuthorisation) {
        FinremCaseDetails caseDetailsBefore = callbackRequest.getCaseDetailsBefore();
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        log.info("Invoking contested {} about to submit callback for Case ID: {}",
            callbackRequest.getEventType(), caseDetails.getId());
        FinremCaseData finremCaseData = caseDetails.getData();

        OrderFiledBy orderFiledBy = getOrderFiledBy(caseDetails, userAuthorisation);

        String typeOfDraftOrder = finremCaseData.getDraftOrdersWrapper().getTypeOfDraftOrder();
        List<String> warnings = new ArrayList<>();
        State state = caseDetails.getState();
        if (SUGGESTED_DRAFT_ORDER_OPTION.equals(typeOfDraftOrder)) {
            handleSuggestedDraftOrders(finremCaseData, userAuthorisation, orderFiledBy);
            populateSuggestedDraftOrderDocumentWarnings(callbackRequest, userAuthorisation, warnings);
            state = caseDetailsBefore.getState();
        } else if (AGREED_DRAFT_ORDER_OPTION.equals(typeOfDraftOrder)) {
            handleAgreedDraftOrder(finremCaseData, userAuthorisation, orderFiledBy);
            populateAgreedDraftOrderDocumentWarnings(callbackRequest, userAuthorisation, warnings);
        }

        caseDetails.getData().getDraftOrdersWrapper().setUploadSuggestedDraftOrder(null);
        caseDetails.getData().getDraftOrdersWrapper().setUploadAgreedDraftOrder(null);
        caseDetails.getData().getDraftOrdersWrapper().setIsUnreviewedDocumentPresent(YesOrNo.YES);
        clearTemporaryFields(caseDetails);

        if (state == null) {
            throw new IllegalStateException("Unexpected null in state");
        }
        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().warnings(warnings).data(finremCaseData).state(state.getStateId())
            .build();
    }

    private OrderFiledBy getOrderFiledBy(FinremCaseDetails caseDetails, String userAuthorisation) {
        CaseRole userCaseRole = getUserCaseRole(caseDetails.getId().toString(), userAuthorisation);

        return switch (userCaseRole) {
            case APP_SOLICITOR -> OrderFiledBy.APPLICANT;
            case RESP_SOLICITOR -> OrderFiledBy.RESPONDENT;
            case CASEWORKER ->
                OrderFiledBy.forUploadPartyValue(getUploadParty(caseDetails.getData().getDraftOrdersWrapper()));
            case APP_BARRISTER -> OrderFiledBy.APPLICANT_BARRISTER;
            case RESP_BARRISTER -> OrderFiledBy.RESPONDENT_BARRISTER;
            case INTVR_SOLICITOR_1 -> OrderFiledBy.INTERVENER_1;
            case INTVR_SOLICITOR_2 -> OrderFiledBy.INTERVENER_2;
            case INTVR_SOLICITOR_3 -> OrderFiledBy.INTERVENER_3;
            case INTVR_SOLICITOR_4 -> OrderFiledBy.INTERVENER_4;
            default -> throw new IllegalArgumentException("Unexpected case role " + userCaseRole);
        };
    }

    private String getUploadParty(DraftOrdersWrapper draftOrdersWrapper) {
        String typeOfDraftOrder = draftOrdersWrapper.getTypeOfDraftOrder();
        if (SUGGESTED_DRAFT_ORDER_OPTION.equals(typeOfDraftOrder)) {
            return draftOrdersWrapper.getUploadSuggestedDraftOrder().getUploadParty().getValue().getCode();
        } else if (AGREED_DRAFT_ORDER_OPTION.equals(typeOfDraftOrder)) {
            return draftOrdersWrapper.getUploadAgreedDraftOrder().getUploadParty().getValue().getCode();
        } else {
            throw new IllegalArgumentException("Invalid type of draft order");
        }
    }

    private void handleAgreedDraftOrder(FinremCaseData finremCaseData, String userAuthorisation,
                                        OrderFiledBy orderFiledBy) {
        DraftOrdersWrapper draftOrdersWrapper = finremCaseData.getDraftOrdersWrapper();
        draftOrdersWrapper.getUploadAgreedDraftOrder().setOrderFiledBy(orderFiledBy);

        List<AgreedDraftOrderCollection> newAgreedDraftOrderCollections = draftOrderService
            .processAgreedDraftOrders(draftOrdersWrapper.getUploadAgreedDraftOrder(), userAuthorisation);
        draftOrderService.populateDraftOrdersReviewCollection(finremCaseData, draftOrdersWrapper.getUploadAgreedDraftOrder(),
            newAgreedDraftOrderCollections);

        draftOrdersWrapper.appendAgreedDraftOrderCollection(newAgreedDraftOrderCollections);
    }

    private void handleSuggestedDraftOrders(FinremCaseData finremCaseData, String userAuthorisation,
                                            OrderFiledBy orderFiledBy) {
        DraftOrdersWrapper draftOrdersWrapper = finremCaseData.getDraftOrdersWrapper();
        UploadSuggestedDraftOrder uploadSuggestedDraftOrder = draftOrdersWrapper.getUploadSuggestedDraftOrder();
        uploadSuggestedDraftOrder.setOrderFiledBy(orderFiledBy);

        List<SuggestedDraftOrderCollection> newSuggestedDraftOrderCollections = processSuggestedDraftOrders(uploadSuggestedDraftOrder,
            userAuthorisation);

        List<SuggestedDraftOrderCollection> existingSuggestedDraftOrderCollections =
            getExistingSuggestedDraftOrderCollections(draftOrdersWrapper);

        existingSuggestedDraftOrderCollections.addAll(newSuggestedDraftOrderCollections);
        draftOrdersWrapper.setSuggestedDraftOrderCollection(existingSuggestedDraftOrderCollections);

        draftOrdersCategoriser.categoriseDocuments(finremCaseData);
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
        if (!ObjectUtils.isEmpty(uploadDraftOrder.getAdditionalDocuments())) {
            List<DocumentCollection> attachments = new ArrayList<>();
            for (AdditionalDocumentsCollection additionalDoc :
                uploadDraftOrder.getAdditionalDocuments()) {
                if (additionalDoc.getValue().getOrderAttachment() != null) {
                    attachments.add(DocumentCollection.builder()
                        .value(additionalDoc.getValue().getOrderAttachment())
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

    private CaseRole getUserCaseRole(String id, String auth) {
        CaseAssignedUserRolesResource caseAssignedUserRole =
            caseAssignedRoleService.getCaseAssignedUserRole(id, auth);

        if (caseAssignedUserRole != null) {
            List<CaseAssignedUserRole> caseAssignedUserRoleList = caseAssignedUserRole.getCaseAssignedUserRoles();

            if (!caseAssignedUserRoleList.isEmpty()) {
                String loggedInUserCaseRole = caseAssignedUserRoleList.get(0).getCaseRole();
                return CaseRole.forValue(loggedInUserCaseRole);
            }
        }

        return CASEWORKER;
    }

    private <T extends HasSubmittedInfo> T applySubmittedInfo(String userAuthorisation, T submittedInfo) {
        return draftOrderService.applySubmittedInfo(userAuthorisation, submittedInfo);
    }

    private boolean isOrdersSelected(List<String> uploadOrdersOrPsas) {
        return draftOrderService.isOrdersSelected(uploadOrdersOrPsas);
    }

    private boolean isPsaSelected(List<String> uploadOrdersOrPsas) {
        return draftOrderService.isPsaSelected(uploadOrdersOrPsas);
    }

    private List<UploadAgreedDraftOrderCollection> getUploadAgreedDraftOrderCollection(FinremCaseData data) {
        return emptyIfNull(ofNullable(data.getDraftOrdersWrapper().getUploadAgreedDraftOrder()).orElse(UploadAgreedDraftOrder.builder().build())
            .getUploadAgreedDraftOrderCollection());
    }

    private List<AgreedPensionSharingAnnexCollection> getAgreedPsaCollection(FinremCaseData data) {
        return emptyIfNull(ofNullable(data.getDraftOrdersWrapper().getUploadAgreedDraftOrder()).orElse(UploadAgreedDraftOrder.builder().build())
            .getAgreedPsaCollection());
    }

    private List<UploadSuggestedDraftOrderCollection> getUploadSuggestedDraftOrderCollection(FinremCaseData data) {
        return emptyIfNull(ofNullable(data.getDraftOrdersWrapper().getUploadSuggestedDraftOrder()).orElse(UploadSuggestedDraftOrder.builder().build())
            .getUploadSuggestedDraftOrderCollection());
    }

    private List<SuggestedPensionSharingAnnexCollection> getSuggestedPsaCollection(FinremCaseData data) {
        return emptyIfNull(ofNullable(data.getDraftOrdersWrapper().getUploadSuggestedDraftOrder()).orElse(UploadSuggestedDraftOrder.builder().build())
            .getSuggestedPsaCollection());
    }

    private void populateSuggestedDraftOrderDocumentWarnings(FinremCallbackRequest callbackRequest, String userAuthorisation, List<String> warnings) {
        warnings.addAll(documentWarningsHelper.getDocumentWarnings(callbackRequest,
            data -> concat(
                getUploadSuggestedDraftOrderCollection(data).stream().map(UploadSuggestedDraftOrderCollection::getValue),
                getSuggestedPsaCollection(data).stream().map(SuggestedPensionSharingAnnexCollection::getValue)
            ).toList(),
            userAuthorisation));
    }

    private void populateAgreedDraftOrderDocumentWarnings(FinremCallbackRequest callbackRequest, String userAuthorisation, List<String> warnings) {
        warnings.addAll(documentWarningsHelper.getDocumentWarnings(callbackRequest,
            data -> concat(
                getUploadAgreedDraftOrderCollection(data).stream().map(UploadAgreedDraftOrderCollection::getValue),
                getAgreedPsaCollection(data).stream().map(AgreedPensionSharingAnnexCollection::getValue)
            ).toList(), userAuthorisation));
    }

    private void clearTemporaryFields(FinremCaseDetails caseDetails) {
        caseDetails.getData().getDraftOrdersWrapper().setUploadSuggestedDraftOrder(null); // Clear the temporary field
        caseDetails.getData().getDraftOrdersWrapper().setUploadAgreedDraftOrder(null); // Clear the temporary field
    }
}
