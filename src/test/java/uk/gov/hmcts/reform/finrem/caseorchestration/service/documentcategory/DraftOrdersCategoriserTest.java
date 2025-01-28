package uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcategory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.OrderFiledBy;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.suggested.SuggestedDraftOrderAdditionalDocumentsCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.suggested.SuggestedPensionSharingAnnex;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.suggested.SuggestedPensionSharingAnnexCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.suggested.UploadSuggestedDraftOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.suggested.UploadSuggestedDraftOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.suggested.UploadedDraftOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory.DraftOrdersCategoriser;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.DraftOrdersConstants.SUGGESTED_DRAFT_ORDER_OPTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory.HEARING_DOCUMENTS_APPLICANT_PRE_HEARING_DRAFT_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory.HEARING_DOCUMENTS_INTERVENER_1_PRE_HEARING_DRAFT_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory.HEARING_DOCUMENTS_INTERVENER_2_PRE_HEARING_DRAFT_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory.HEARING_DOCUMENTS_INTERVENER_3_PRE_HEARING_DRAFT_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory.HEARING_DOCUMENTS_INTERVENER_4_PRE_HEARING_DRAFT_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory.HEARING_DOCUMENTS_RESPONDENT_PRE_HEARING_DRAFT_ORDER;

class DraftOrdersCategoriserTest {

    private DraftOrdersCategoriser categoriser;

    private FinremCaseData caseData;

    @BeforeEach
    public void setUp() {
        categoriser = new DraftOrdersCategoriser();
        caseData = new FinremCaseData();
        caseData.getDraftOrdersWrapper().setUploadSuggestedDraftOrder(new UploadSuggestedDraftOrder());
    }

    @ParameterizedTest
    @MethodSource("provideDocumentCategories")
    void givenUploadedSuggestedDraftOrder_whenCategoriseDocuments_thenCategoryIsSet(OrderFiledBy orderFiledBy,
                                                                                    DocumentCategory expectedDocumentCategory) {
        // Set up case data
        caseData.getDraftOrdersWrapper().setTypeOfDraftOrder(SUGGESTED_DRAFT_ORDER_OPTION);
        UploadSuggestedDraftOrder uploadSuggestedDraftOrder = caseData.getDraftOrdersWrapper().getUploadSuggestedDraftOrder();
        uploadSuggestedDraftOrder.setOrderFiledBy(orderFiledBy);
        uploadSuggestedDraftOrder.setUploadSuggestedDraftOrderCollection(buildSuggestedDraftOrders());
        uploadSuggestedDraftOrder.setSuggestedPsaCollection(buildSuggestedPsaCollection());

        // Act
        categoriser.categoriseDocuments(caseData);

        // Assert that the category has been set
        UploadedDraftOrder uploadedDraftOrder = uploadSuggestedDraftOrder.getUploadSuggestedDraftOrderCollection()
            .get(0).getValue();

        assertThat(uploadedDraftOrder.getSuggestedDraftOrderDocument().getCategoryId())
            .isEqualTo(expectedDocumentCategory.getDocumentCategoryId());
        assertThat(uploadedDraftOrder.getSuggestedDraftOrderAdditionalDocumentsCollection())
            .hasSize(3)
            .extracting(SuggestedDraftOrderAdditionalDocumentsCollection::getValue)
            .extracting(CaseDocument::getCategoryId)
            .allMatch(categoryId -> categoryId.equals(expectedDocumentCategory.getDocumentCategoryId()));
    }

    private List<UploadSuggestedDraftOrderCollection> buildSuggestedDraftOrders() {
        UploadedDraftOrder draftOrder = new UploadedDraftOrder();
        draftOrder.setSuggestedDraftOrderDocument(new CaseDocument());
        draftOrder.setSuggestedDraftOrderAdditionalDocumentsCollection(List.of(
            SuggestedDraftOrderAdditionalDocumentsCollection.builder().value(new CaseDocument()).build(),
            SuggestedDraftOrderAdditionalDocumentsCollection.builder().value(new CaseDocument()).build(),
            SuggestedDraftOrderAdditionalDocumentsCollection.builder().value(new CaseDocument()).build()
        ));

        return List.of(UploadSuggestedDraftOrderCollection.builder().value(draftOrder).build());
    }

    private List<SuggestedPensionSharingAnnexCollection> buildSuggestedPsaCollection() {
        SuggestedPensionSharingAnnex psa = new SuggestedPensionSharingAnnex();
        psa.setSuggestedPensionSharingAnnexes(new CaseDocument());

        return List.of(SuggestedPensionSharingAnnexCollection.builder().value(psa).build());
    }

    private static Stream<Arguments> provideDocumentCategories() {
        return Stream.of(
            Arguments.of(OrderFiledBy.APPLICANT, HEARING_DOCUMENTS_APPLICANT_PRE_HEARING_DRAFT_ORDER),
            Arguments.of(OrderFiledBy.RESPONDENT, HEARING_DOCUMENTS_RESPONDENT_PRE_HEARING_DRAFT_ORDER),
            Arguments.of(OrderFiledBy.APPLICANT_BARRISTER, HEARING_DOCUMENTS_APPLICANT_PRE_HEARING_DRAFT_ORDER),
            Arguments.of(OrderFiledBy.RESPONDENT_BARRISTER, HEARING_DOCUMENTS_RESPONDENT_PRE_HEARING_DRAFT_ORDER),
            Arguments.of(OrderFiledBy.INTERVENER_1, HEARING_DOCUMENTS_INTERVENER_1_PRE_HEARING_DRAFT_ORDER),
            Arguments.of(OrderFiledBy.INTERVENER_2, HEARING_DOCUMENTS_INTERVENER_2_PRE_HEARING_DRAFT_ORDER),
            Arguments.of(OrderFiledBy.INTERVENER_3, HEARING_DOCUMENTS_INTERVENER_3_PRE_HEARING_DRAFT_ORDER),
            Arguments.of(OrderFiledBy.INTERVENER_4, HEARING_DOCUMENTS_INTERVENER_4_PRE_HEARING_DRAFT_ORDER)
        );
    }
}
