package uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcategory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicRadioList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicRadioListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.suggested.SuggestedPensionSharingAnnex;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.suggested.SuggestedPensionSharingAnnexCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.suggested.UploadSuggestedDraftOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.suggested.UploadSuggestedDraftOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.suggested.UploadedDraftOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DraftOrdersWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory.DraftOrdersCategoriser;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.DraftOrdersConstants.SUGGESTED_DRAFT_ORDER_OPTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.DraftOrdersConstants.UPLOAD_PARTY_APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.DraftOrdersConstants.UPLOAD_PARTY_RESPONDENT;

@ExtendWith(MockitoExtension.class)
class DraftOrdersCategoriserTest {

    @InjectMocks
    private DraftOrdersCategoriser categoriser;

    private FinremCaseData caseData;

    @BeforeEach
    public void setUp() {
        caseData = new FinremCaseData();
        caseData.setDraftOrdersWrapper(new DraftOrdersWrapper());
        caseData.getDraftOrdersWrapper().setUploadSuggestedDraftOrder(new UploadSuggestedDraftOrder());
    }

    @Test
    void givenApplicantUploadsDraftOrder_whenCategoriseDocuments_thenCategoryIsSet() {
        // Set up case data
        caseData.getDraftOrdersWrapper().setTypeOfDraftOrder(SUGGESTED_DRAFT_ORDER_OPTION);

        DynamicRadioList uploadPartyRadioList;

        DynamicRadioListElement elementApplicant = DynamicRadioListElement.builder()
            .code(UPLOAD_PARTY_APPLICANT)
            .label("The applicant")
            .build();

        uploadPartyRadioList = DynamicRadioList.builder()
            .listItems(List.of(elementApplicant))
            .build();

        caseData.getDraftOrdersWrapper().getUploadSuggestedDraftOrder().setUploadParty(uploadPartyRadioList);
        caseData.getDraftOrdersWrapper().getUploadSuggestedDraftOrder().getUploadParty().setValue(elementApplicant);

        List<UploadSuggestedDraftOrderCollection> draftOrderCollection = new ArrayList<>();
        UploadedDraftOrder draftOrder = new UploadedDraftOrder();
        draftOrder.setSuggestedDraftOrderDocument(new CaseDocument());
        draftOrderCollection.add(new UploadSuggestedDraftOrderCollection(draftOrder));

        caseData.getDraftOrdersWrapper().getUploadSuggestedDraftOrder().setUploadSuggestedDraftOrderCollection(draftOrderCollection);

        // Act
        categoriser.categoriseDocuments(caseData, "applicant");

        // Assert that the category has been set
        assertThat(draftOrder.getSuggestedDraftOrderDocument().getCategoryId())
            .isEqualTo(DocumentCategory.HEARING_DOCUMENTS_APPLICANT_PRE_HEARING_DRAFT_ORDER.getDocumentCategoryId());
    }

    @Test
    void givenRespondentUploadsDraftOrder_whenCategoriseDocuments_thenCategoryIsSet() {
        caseData.getDraftOrdersWrapper().setTypeOfDraftOrder(SUGGESTED_DRAFT_ORDER_OPTION);

        DynamicRadioList uploadPartyRadioList;
        DynamicRadioListElement elementRespondent = DynamicRadioListElement.builder()
            .code(UPLOAD_PARTY_RESPONDENT)
            .label("The respondent")
            .build();
        uploadPartyRadioList = DynamicRadioList.builder()
            .listItems(List.of(elementRespondent))
            .build();

        caseData.getDraftOrdersWrapper().getUploadSuggestedDraftOrder().setUploadParty(uploadPartyRadioList);
        caseData.getDraftOrdersWrapper().getUploadSuggestedDraftOrder().getUploadParty().setValue(elementRespondent);

        List<UploadSuggestedDraftOrderCollection> draftOrderCollection = new ArrayList<>();
        UploadedDraftOrder draftOrder = new UploadedDraftOrder();
        draftOrder.setSuggestedDraftOrderDocument(new CaseDocument());
        draftOrderCollection.add(new UploadSuggestedDraftOrderCollection(draftOrder));

        caseData.getDraftOrdersWrapper().getUploadSuggestedDraftOrder().setUploadSuggestedDraftOrderCollection(draftOrderCollection);

        // Act
        categoriser.categoriseDocuments(caseData, "respondent");

        assertThat(draftOrder.getSuggestedDraftOrderDocument().getCategoryId())
            .isEqualTo(DocumentCategory.HEARING_DOCUMENTS_RESPONDENT_PRE_HEARING_DRAFT_ORDER.getDocumentCategoryId());
    }

    @Test
    void givenBothDraftOrderAndPensionSharingAnnex_whenCategoriseDocuments_thenSetCategoriesForBoth() {
        caseData.getDraftOrdersWrapper().setTypeOfDraftOrder(SUGGESTED_DRAFT_ORDER_OPTION);

        DynamicRadioList uploadPartyRadioList = DynamicRadioList.builder().build();
        DynamicRadioListElement elementApplicant = DynamicRadioListElement.builder()
            .code(UPLOAD_PARTY_APPLICANT)
            .label("The applicant")
            .build();
        uploadPartyRadioList.setListItems(List.of(elementApplicant));
        uploadPartyRadioList.setValue(elementApplicant);

        UploadSuggestedDraftOrder uploadSuggestedDraftOrder = new UploadSuggestedDraftOrder();
        uploadSuggestedDraftOrder.setUploadParty(uploadPartyRadioList);

        List<UploadSuggestedDraftOrderCollection> draftOrderCollection = new ArrayList<>();
        UploadedDraftOrder draftOrder = new UploadedDraftOrder();
        draftOrder.setSuggestedDraftOrderDocument(new CaseDocument());
        draftOrderCollection.add(new UploadSuggestedDraftOrderCollection(draftOrder));

        // Create and set PSA document
        List<SuggestedPensionSharingAnnexCollection> psaCollection = new ArrayList<>();
        SuggestedPensionSharingAnnex psa = new SuggestedPensionSharingAnnex();
        psa.setSuggestedPensionSharingAnnexes(new CaseDocument());
        psaCollection.add(new SuggestedPensionSharingAnnexCollection(psa));

        // Set collections in the wrapper
        uploadSuggestedDraftOrder.setUploadSuggestedDraftOrderCollection(draftOrderCollection);
        uploadSuggestedDraftOrder.setSuggestedPsaCollection(psaCollection);
        caseData.getDraftOrdersWrapper().setUploadSuggestedDraftOrder(uploadSuggestedDraftOrder);

        categoriser.categoriseDocuments(caseData, "applicant");

        assertThat(draftOrder.getSuggestedDraftOrderDocument().getCategoryId())
            .isEqualTo(DocumentCategory.HEARING_DOCUMENTS_APPLICANT_PRE_HEARING_DRAFT_ORDER.getDocumentCategoryId());

        assertThat(psa.getSuggestedPensionSharingAnnexes().getCategoryId())
            .isEqualTo(DocumentCategory.HEARING_DOCUMENTS_APPLICANT_PRE_HEARING_DRAFT_ORDER.getDocumentCategoryId());
    }
}
