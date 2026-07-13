package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralEmailCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralEmailHolder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralEmailWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory.GeneralEmailDocumentCategoriser;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;

@ExtendWith(MockitoExtension.class)
class GeneralEmailDocumentCategoriserTest {

    private static final String APPLICANT_EMAIL = "applicant@example.com";

    @Mock
    private FeatureToggleService featureToggleService;

    @Test
    void shouldCategoriseGeneralEmailDocument() {
        when(featureToggleService.isCaseFileViewEnabled()).thenReturn(true);

        CaseDocument document = caseDocument("email.pdf");

        FinremCaseData caseData = createCaseData(
            APPLICANT_EMAIL,
            List.of(DocumentCollectionItem.fromCaseDocument(document))
        );

        new GeneralEmailDocumentCategoriser(featureToggleService).categorise(caseData);

        assertThat(document.getCategoryId())
            .isEqualTo(DocumentCategory.COURT_CORRESPONDENCE_APPLICANT.getDocumentCategoryId());
    }

    @Test
    void shouldIgnoreGeneralEmailWithoutRecipientOrDocuments() {
        when(featureToggleService.isCaseFileViewEnabled()).thenReturn(true);

        FinremCaseData caseData = createCaseData(null, List.of());

        new GeneralEmailDocumentCategoriser(featureToggleService).categorise(caseData);

        assertThat(caseData.getGeneralEmailWrapper().getGeneralEmailCollection())
            .hasSize(1);
    }

    @Test
    void shouldIgnoreNullDocumentAndNotOverwriteExistingCategory() {
        when(featureToggleService.isCaseFileViewEnabled()).thenReturn(true);

        CaseDocument document = caseDocument("email.pdf");
        document.setCategoryId(
            DocumentCategory.COURT_CORRESPONDENCE_RESPONDENT.getDocumentCategoryId()
        );

        List<DocumentCollectionItem> documents = new ArrayList<>();
        documents.add(null);
        documents.add(DocumentCollectionItem.builder().build());
        documents.add(DocumentCollectionItem.fromCaseDocument(document));

        FinremCaseData caseData = createCaseData(APPLICANT_EMAIL, documents);

        new GeneralEmailDocumentCategoriser(featureToggleService).categorise(caseData);

        assertThat(document.getCategoryId())
            .isEqualTo(DocumentCategory.COURT_CORRESPONDENCE_RESPONDENT.getDocumentCategoryId());
    }

    private FinremCaseData createCaseData(String recipient,
                                          List<DocumentCollectionItem> documents) {
        return FinremCaseData.builder()
            .contactDetailsWrapper(ContactDetailsWrapper.builder()
                .applicantEmail(APPLICANT_EMAIL)
                .build())
            .generalEmailWrapper(GeneralEmailWrapper.builder()
                .generalEmailCollection(List.of(
                    GeneralEmailCollection.builder()
                        .value(GeneralEmailHolder.builder()
                            .generalEmailRecipient(recipient)
                            .generalEmailUploadedDocuments(documents)
                            .build())
                        .build()))
                .build())
            .build();
    }
}
