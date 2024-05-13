package uk.gov.hmcts.reform.finrem.caseorchestration.scheduler;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.address.LetterAddresseeGeneratorMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ApproveOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ApproveOrdersHolder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ApprovedOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ApprovedOrderConsolidateCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DraftDirectionOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DraftDirectionOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationItems;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrderSentToPartiesCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.SendOrderDocuments;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.State;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DraftDirectionWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralApplicationWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralApplicationsCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.OrderWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.UploadCaseDocumentWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CcdService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SystemUserService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.FdrDocumentsHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.applicant.ApplicantFdrDocumentCategoriser;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.applicant.ApplicantOtherDocumentsHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenerfour.IntervenerFourFdrDocumentCategoriser;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenerfour.IntervenerFourFdrHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenerfour.IntervenerFourOtherDocumentsHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenerone.IntervenerOneFdrDocumentCategoriser;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenerone.IntervenerOneFdrHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenerone.IntervenerOneOtherDocumentsHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenerthree.IntervenerThreeFdrDocumentCategoriser;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenerthree.IntervenerThreeFdrHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenerthree.IntervenerThreeOtherDocumentsHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenertwo.IntervenerTwoFdrDocumentCategoriser;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenertwo.IntervenerTwoFdrHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenertwo.IntervenerTwoOtherDocumentsHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.respondent.RespondentFdrDocumentCategoriser;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.respondent.RespondentOtherDocumentsHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory.GeneralApplicationsCategoriser;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory.SendOrdersCategoriser;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory.UploadedDraftOrderCategoriser;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.csv.CaseReference;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory.ADMINISTRATIVE_DOCUMENTS_TRANSITIONAL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory.APPLICATIONS_OTHER_APPLICATION_APPLICATION_1;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_APPLICANT_POINTS_OF_CLAIM_OR_DEFENCE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory.POST_HEARING_DRAFT_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory.REPORTS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory.SYSTEM_DUPLICATES;

@ExtendWith(SpringExtension.class)
@TestPropertySource(properties = {"cron.cfvUpdate.batchSize=1"})
@ContextConfiguration(classes = {
    ObjectMapper.class, FinremCaseDetailsMapper.class,
    CfvUpdateTask.class,
    CfvUpdateTaskDocumentCategoriser.class, DocumentHelper.class, CaseDataService.class,
    UploadedDraftOrderCategoriser.class, GeneralApplicationsCategoriser.class, SendOrdersCategoriser.class,
    ApplicantOtherDocumentsHandler.class, RespondentOtherDocumentsHandler.class,
    IntervenerOneOtherDocumentsHandler.class, IntervenerTwoOtherDocumentsHandler.class,
    IntervenerThreeOtherDocumentsHandler.class, IntervenerFourOtherDocumentsHandler.class,
    ApplicantFdrDocumentCategoriser.class, RespondentFdrDocumentCategoriser.class,
    IntervenerOneFdrDocumentCategoriser.class, IntervenerTwoFdrDocumentCategoriser.class,
    IntervenerThreeFdrDocumentCategoriser.class, IntervenerFourFdrDocumentCategoriser.class,
    FdrDocumentsHandler.class, IntervenerOneFdrHandler.class,
    IntervenerTwoFdrHandler.class, IntervenerThreeFdrHandler.class, IntervenerFourFdrHandler.class
})
class CfvUpdateTaskTest {

    @Autowired
    CfvUpdateTask cfvUpdateTask;

    @Autowired
    FinremCaseDetailsMapper finremCaseDetailsMapper;

    @MockBean
    CcdService ccdService;

    @MockBean
    SystemUserService systemUserService;

    @MockBean
    FeatureToggleService featureToggleService;

    @MockBean
    GenericDocumentService genericDocumentService;

    @MockBean
    LetterAddresseeGeneratorMapper letterAddresseeGeneratorMapper;

    @BeforeEach
    void setup() {
        when(systemUserService.getSysUserToken()).thenReturn(AUTH_TOKEN);
        when(featureToggleService.isCaseFileViewEnabled()).thenReturn(true);
    }

    @Test
    void testGetCaseReferences() {
        FinremCaseData caseData = FinremCaseData.builder().build();
        SearchResult searchResult = createSearchResult(caseData);
        when(ccdService.esSearchCases(any(CaseType.class), anyString(), anyString())).thenReturn(searchResult);

        List<CaseReference> caseReferences = cfvUpdateTask.getCaseReferences();
        assertThat(caseReferences).hasSize(1);
    }

    @Test
    void testExecuteTask() {
        FinremCaseData caseData = FinremCaseData.builder()
            .draftDirectionWrapper(createDraftDirectionWrapper())
            .generalApplicationWrapper(createGeneralApplicationWrapper())
            .ordersSentToPartiesCollection(createOrdersSentToParties())
            .orderWrapper(createOrderWrapper())
            .uploadCaseDocumentWrapper(createUploadCaseDocumentWrapper())
            .build();
        FinremCaseDetails caseDetails = FinremCaseDetails.builder()
            .data(caseData)
            .build();

        cfvUpdateTask.executeTask(caseDetails);

        assertThat(caseDetails.getData().getCfvMigrationWrapper().getCfvMigrationVersion()).isEqualTo("1");
        verifyDraftDirectionWrapper(caseData.getDraftDirectionWrapper());
        verifyGeneralApplicationsWrapper(caseData.getGeneralApplicationWrapper());
        verifyOrdersSentToParties(caseData.getOrdersSentToPartiesCollection());
        verifyOrderWrapper(caseData.getOrderWrapper());
        verifyUploadCaseDocumentWrapper(caseData.getUploadCaseDocumentWrapper());
    }

    private DraftDirectionWrapper createDraftDirectionWrapper() {
        DraftDirectionOrderCollection draftDirectionOrder = DraftDirectionOrderCollection.builder()
            .value(DraftDirectionOrder.builder()
                .uploadDraftDocument(CaseDocument.builder().build())
                .build())
            .build();

        DraftDirectionOrderCollection judgesAmendedOrder = DraftDirectionOrderCollection.builder()
            .value(DraftDirectionOrder.builder()
                .uploadDraftDocument(CaseDocument.builder().build())
                .build())
            .build();

        return DraftDirectionWrapper.builder()
            .draftDirectionOrderCollection(List.of(draftDirectionOrder))
            .judgesAmendedOrderCollection(List.of(judgesAmendedOrder))
            .build();
    }

    private void verifyDraftDirectionWrapper(DraftDirectionWrapper draftDirectionWrapper) {
        assertThat(draftDirectionWrapper.getDraftDirectionOrderCollection().get(0).getValue().getUploadDraftDocument()
            .getCategoryId()).isEqualTo(POST_HEARING_DRAFT_ORDER.getDocumentCategoryId());
        assertThat(draftDirectionWrapper.getJudgesAmendedOrderCollection().get(0).getValue().getUploadDraftDocument()
            .getCategoryId()).isEqualTo(SYSTEM_DUPLICATES.getDocumentCategoryId());
    }

    private GeneralApplicationWrapper createGeneralApplicationWrapper() {
        GeneralApplicationsCollection generalApplication = GeneralApplicationsCollection.builder()
            .value(GeneralApplicationItems.builder()
                .generalApplicationDocument(CaseDocument.builder().build())
                .build()
            )
            .build();

        return GeneralApplicationWrapper.builder()
            .generalApplications(List.of(generalApplication))
            .build();
    }

    private void verifyGeneralApplicationsWrapper(GeneralApplicationWrapper generalApplicationWrapper) {
        assertThat(generalApplicationWrapper.getGeneralApplications().get(0).getValue().getGeneralApplicationDocument()
            .getCategoryId()).isEqualTo(APPLICATIONS_OTHER_APPLICATION_APPLICATION_1.getDocumentCategoryId());
    }

    private List<OrderSentToPartiesCollection> createOrdersSentToParties() {
        OrderSentToPartiesCollection orderSentToPartiesCollection = OrderSentToPartiesCollection.builder()
            .value(SendOrderDocuments.builder()
                .caseDocument(CaseDocument.builder().build())
                .build())
            .build();

        return List.of(orderSentToPartiesCollection);
    }

    private void verifyOrdersSentToParties(List<OrderSentToPartiesCollection> ordersSentToParties) {
        assertThat(ordersSentToParties.get(0).getValue().getCaseDocument()
            .getCategoryId()).isEqualTo(ADMINISTRATIVE_DOCUMENTS_TRANSITIONAL.getDocumentCategoryId());
    }

    private OrderWrapper createOrderWrapper() {
        ApprovedOrderCollection approvedOrderCollection = ApprovedOrderCollection.builder()
            .value(ApproveOrder.builder()
                .caseDocument(CaseDocument.builder().build())
                .build()
            )
            .build();
        ApprovedOrderConsolidateCollection intervener1Orders = ApprovedOrderConsolidateCollection.builder()
            .value(ApproveOrdersHolder.builder()
                .approveOrders(List.of(approvedOrderCollection))
                .build())
            .build();

        return OrderWrapper.builder()
            .intv1OrderCollections(List.of(intervener1Orders))
            .build();
    }

    private void verifyOrderWrapper(OrderWrapper orderWrapper) {
        assertThat(orderWrapper.getIntv1OrderCollections().get(0).getValue().getApproveOrders().get(0).getValue()
            .getCaseDocument().getCategoryId()).isEqualTo(SYSTEM_DUPLICATES.getDocumentCategoryId());
    }

    private UploadCaseDocumentWrapper createUploadCaseDocumentWrapper() {
        UploadCaseDocumentCollection applicantPensionPlan = UploadCaseDocumentCollection.builder()
            .uploadCaseDocument(UploadCaseDocument.builder()
                .caseDocumentParty(CaseDocumentParty.APPLICANT)
                .caseDocumentType(CaseDocumentType.PENSION_PLAN)
                .caseDocumentFdr(YesOrNo.NO)
                .caseDocuments(CaseDocument.builder().build())
                .build())
            .build();

        UploadCaseDocumentCollection applicantFdr = UploadCaseDocumentCollection.builder()
            .uploadCaseDocument(UploadCaseDocument.builder()
                .caseDocumentParty(CaseDocumentParty.APPLICANT)
                .caseDocumentType(CaseDocumentType.POINTS_OF_CLAIM_OR_DEFENCE)
                .caseDocumentFdr(YesOrNo.YES)
                .caseDocuments(CaseDocument.builder().build())
                .build())
            .build();

        return UploadCaseDocumentWrapper.builder()
            .appOtherCollection(List.of(applicantPensionPlan))
            .fdrCaseDocumentCollection(List.of(applicantFdr))
            .build();
    }

    private void verifyUploadCaseDocumentWrapper(UploadCaseDocumentWrapper uploadCaseDocumentWrapper) {
        assertThat(uploadCaseDocumentWrapper.getAppOtherCollection().get(0).getUploadCaseDocument().getCaseDocuments()
            .getCategoryId()).isEqualTo(REPORTS.getDocumentCategoryId());
        assertThat(uploadCaseDocumentWrapper.getFdrCaseDocumentCollection().get(0).getUploadCaseDocument()
            .getCaseDocuments().getCategoryId()).isEqualTo(
            FDR_DOCUMENTS_AND_FDR_BUNDLE_APPLICANT_POINTS_OF_CLAIM_OR_DEFENCE.getDocumentCategoryId());
    }

    private SearchResult createSearchResult(FinremCaseData caseData) {
        FinremCaseDetails caseDetails = FinremCaseDetails.builder()
            .id(1L)
            .caseType(CaseType.CONTESTED)
            .state(State.APPLICATION_ISSUED)
            .data(caseData)
            .build();

        return SearchResult.builder()
            .cases(List.of(finremCaseDetailsMapper.mapToCaseDetails(caseDetails)))
            .total(1)
            .build();
    }
}
