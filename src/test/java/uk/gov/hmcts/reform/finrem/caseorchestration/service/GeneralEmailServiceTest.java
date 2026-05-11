package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralEmailCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralEmailHolder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralEmailWrapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;

@ExtendWith(MockitoExtension.class)
class GeneralEmailServiceTest {

    private static final LocalDateTime FIXED_DATE = LocalDateTime.of(2026, 5, 5, 23, 0, 0);

    @InjectMocks
    private GeneralEmailService generalEmailService;

    @Test
    void givenEmptyGeneralEmailCollection_whenStoreGeneralEmail_thenEmailIsStored() {
        DocumentCollectionItem documentCollectionItem =
            DocumentCollectionItem.fromCaseDocument(caseDocument("A_DOC_1.pdf"));

        FinremCaseData finremCaseData = FinremCaseData.builder()
            .generalEmailWrapper(GeneralEmailWrapper.builder()
                .generalEmailBody("Hi, This is the body of an email.")
                .generalEmailCreatedBy("John John")
                .generalEmailRecipient("Claire Mumford")
                .generalEmailUploadedDocuments(List.of(
                    documentCollectionItem
                ))
                .build())
            .build();

        try (MockedStatic<LocalDateTime> mockedStatic = Mockito.mockStatic(LocalDateTime.class)) {
            mockedStatic.when(LocalDateTime::now).thenReturn(FIXED_DATE);

            generalEmailService.storeGeneralEmail(finremCaseData);

            assertThat(finremCaseData.getGeneralEmailWrapper().getGeneralEmailCollection())
                .extracting(GeneralEmailCollection::getValue)
                .extracting(GeneralEmailHolder::getGeneralEmailBody,
                    GeneralEmailHolder::getGeneralEmailRecipient,
                    GeneralEmailHolder::getGeneralEmailCreatedBy,
                    GeneralEmailHolder::getGeneralEmailDateSent)
                .contains(Tuple.tuple("Hi, This is the body of an email.", "Claire Mumford", "John John",
                    FIXED_DATE));
            assertThat(finremCaseData.getGeneralEmailWrapper().getGeneralEmailCollection())
                .extracting(GeneralEmailCollection::getValue)
                .extracting(GeneralEmailHolder::getGeneralEmailUploadedDocuments)
                .contains(List.of(documentCollectionItem));
        }
    }

    @Test
    void givenNonEmptyGeneralEmailCollection_whenStoreGeneralEmail_thenEmailIsStored() {
        DocumentCollectionItem documentCollectionItem =
            DocumentCollectionItem.fromCaseDocument(caseDocument("A_DOC_1.pdf"));

        FinremCaseData finremCaseData = FinremCaseData.builder()
            .generalEmailWrapper(GeneralEmailWrapper.builder()
                .generalEmailBody("Hi, This is the body of an email.")
                .generalEmailCreatedBy("John John")
                .generalEmailRecipient("Claire Mumford")
                .generalEmailUploadedDocuments(List.of(
                    documentCollectionItem
                ))
                .generalEmailCollection(new ArrayList<>(List.of(
                    GeneralEmailCollection.builder()
                        .value(GeneralEmailHolder.builder()
                            .generalEmailBody("Hi, This is an existing email.")
                            .build())
                        .build()
                )))
                .build())
            .build();

        try (MockedStatic<LocalDateTime> mockedStatic = Mockito.mockStatic(LocalDateTime.class)) {
            mockedStatic.when(LocalDateTime::now).thenReturn(FIXED_DATE);

            generalEmailService.storeGeneralEmail(finremCaseData);

            assertThat(finremCaseData.getGeneralEmailWrapper().getGeneralEmailCollection())
                .extracting(GeneralEmailCollection::getValue)
                .extracting(GeneralEmailHolder::getGeneralEmailBody,
                    GeneralEmailHolder::getGeneralEmailRecipient,
                    GeneralEmailHolder::getGeneralEmailCreatedBy,
                    GeneralEmailHolder::getGeneralEmailDateSent)
                .containsExactly(
                    Tuple.tuple("Hi, This is an existing email.", null, null, null),
                    Tuple.tuple("Hi, This is the body of an email.", "Claire Mumford", "John John", FIXED_DATE)
                );
            assertThat(finremCaseData.getGeneralEmailWrapper().getGeneralEmailCollection())
                .extracting(GeneralEmailCollection::getValue)
                .extracting(GeneralEmailHolder::getGeneralEmailUploadedDocuments)
                .contains(List.of(documentCollectionItem));
        }
    }
}
