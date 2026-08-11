package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralEmailCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralEmailHolder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralEmailWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement.EvidenceManagementDownloadService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;

@ExtendWith(MockitoExtension.class)
class GeneralEmailServiceTest {

    private static final int MAX_FILE_SIZE = 2 * 1024 * 1024;
    private final LocalDateTime fixedLocalDateTime = LocalDateTime.of(2026, 5, 5, 23, 0, 0);
    private final CaseDocument attachment = caseDocument("A_DOC_1.pdf");
    private final List<DocumentCollectionItem> attachments = List.of(DocumentCollectionItem.fromCaseDocument(attachment));

    @InjectMocks
    private GeneralEmailService generalEmailService;

    @Mock
    private EvidenceManagementDownloadService evidenceManagementDownloadService;

    @Mock
    private BulkPrintDocumentService bulkPrintDocumentService;

    @Test
    void givenEmptyGeneralEmailCollection_whenStoreGeneralEmail_thenEmailIsStored() {
        FinremCaseData finremCaseData = FinremCaseData.builder()
            .generalEmailWrapper(GeneralEmailWrapper.builder()
                .generalEmailBody("Hi, This is the body of an email.")
                .generalEmailCreatedBy("John John")
                .generalEmailRecipient("Claire Mumford")
                .generalEmailUploadedDocuments(attachments)
                .build())
            .build();

        try (MockedStatic<LocalDateTime> mockedStatic = Mockito.mockStatic(LocalDateTime.class)) {
            mockedStatic.when(LocalDateTime::now).thenReturn(fixedLocalDateTime);

            generalEmailService.storeGeneralEmail(finremCaseData);

            assertThat(finremCaseData.getGeneralEmailWrapper().getGeneralEmailCollection())
                .extracting(GeneralEmailCollection::getValue)
                .extracting(GeneralEmailHolder::getGeneralEmailBody,
                    GeneralEmailHolder::getGeneralEmailRecipient,
                    GeneralEmailHolder::getGeneralEmailCreatedBy,
                    GeneralEmailHolder::getGeneralEmailDateSent,
                    GeneralEmailHolder::getGeneralEmailUploadedDocuments)
                .contains(Tuple.tuple("Hi, This is the body of an email.", "Claire Mumford", "John John",
                    fixedLocalDateTime, attachments));
        }
    }

    @Test
    void givenNonEmptyGeneralEmailCollection_whenStoreGeneralEmail_thenEmailIsStored() {
        FinremCaseData finremCaseData = FinremCaseData.builder()
            .generalEmailWrapper(GeneralEmailWrapper.builder()
                .generalEmailBody("Hi, This is the body of an email.")
                .generalEmailCreatedBy("John John")
                .generalEmailRecipient("Claire Mumford")
                .generalEmailUploadedDocuments(attachments)
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
            mockedStatic.when(LocalDateTime::now).thenReturn(fixedLocalDateTime);

            generalEmailService.storeGeneralEmail(finremCaseData);

            assertThat(finremCaseData.getGeneralEmailWrapper().getGeneralEmailCollection())
                .extracting(GeneralEmailCollection::getValue)
                .extracting(GeneralEmailHolder::getGeneralEmailBody,
                    GeneralEmailHolder::getGeneralEmailRecipient,
                    GeneralEmailHolder::getGeneralEmailCreatedBy,
                    GeneralEmailHolder::getGeneralEmailDateSent,
                    GeneralEmailHolder::getGeneralEmailUploadedDocuments)
                .containsExactly(
                    tuple("Hi, This is an existing email.", null, null, null, null),
                    tuple("Hi, This is the body of an email.", "Claire Mumford", "John John", fixedLocalDateTime,
                        attachments)
                );
        }
    }

    @Test
    void givenUploadedDocuments_whenGetUploadedDocuments_thenReturnDocumentsOnly() {
        CaseDocument secondAttachment = caseDocument("A_DOC_2.pdf");

        List<DocumentCollectionItem> uploadedDocuments = new ArrayList<>();
        uploadedDocuments.add(null);
        uploadedDocuments.add(DocumentCollectionItem.builder().build());
        uploadedDocuments.add(DocumentCollectionItem.fromCaseDocument(attachment));
        uploadedDocuments.add(DocumentCollectionItem.fromCaseDocument(secondAttachment));

        FinremCaseData finremCaseData = FinremCaseData.builder()
            .generalEmailWrapper(GeneralEmailWrapper.builder()
                .generalEmailUploadedDocuments(uploadedDocuments)
                .build())
            .build();

        assertThat(finremCaseData.getGeneralEmailWrapper().getUploadedDocuments())
            .containsExactly(attachment, secondAttachment);
    }

    @Test
    void givenMoreThanTenDocuments_whenValidateUploadedDocuments_thenPopulateError() {
        List<DocumentCollectionItem> uploadedDocuments = IntStream.rangeClosed(1, 11)
            .mapToObj(i -> DocumentCollectionItem.fromCaseDocument(caseDocument("A_DOC_" + i + ".pdf")))
            .toList();

        FinremCaseData finremCaseData = FinremCaseData.builder()
            .generalEmailWrapper(GeneralEmailWrapper.builder()
                .generalEmailUploadedDocuments(uploadedDocuments)
                .build())
            .build();

        when(evidenceManagementDownloadService.getByteArray(any(CaseDocument.class), eq("authToken")))
            .thenReturn(new byte[] {1});

        List<String> errors = new ArrayList<>();

        generalEmailService.validateUploadedDocuments(finremCaseData, "authToken", errors);

        assertThat(errors).containsOnly("A maximum of 10 email attachments is supported");
    }

    @Test
    void givenInvalidFileType_whenValidateUploadedDocuments_thenPopulateError() {
        CaseDocument invalidDocument = caseDocument("A_DOC_1.txt");

        FinremCaseData finremCaseData = FinremCaseData.builder()
            .generalEmailWrapper(GeneralEmailWrapper.builder()
                .generalEmailUploadedDocuments(List.of(DocumentCollectionItem.fromCaseDocument(invalidDocument)))
                .build())
            .build();

        List<String> errors = new ArrayList<>();

        generalEmailService.validateUploadedDocuments(finremCaseData, "authToken", errors);

        assertThat(errors).containsOnly("Only Word and PDF documents are permitted");
        verify(bulkPrintDocumentService, never()).validateEncryptionOnUploadedDocument(
            any(CaseDocument.class),
            any(),
            anyList(),
            anyString()
        );
        verify(evidenceManagementDownloadService, never()).getByteArray(any(CaseDocument.class), anyString());
    }

    @Test
    void givenFileSizeExceedsLimit_whenValidateUploadedDocuments_thenPopulateError() {
        CaseDocument largeDocument = caseDocument("A_DOC_1.pdf");

        FinremCaseData finremCaseData = FinremCaseData.builder()
            .generalEmailWrapper(GeneralEmailWrapper.builder()
                .generalEmailUploadedDocuments(List.of(DocumentCollectionItem.fromCaseDocument(largeDocument)))
                .build())
            .build();

        when(evidenceManagementDownloadService.getByteArray(largeDocument, "authToken"))
            .thenReturn(new byte[MAX_FILE_SIZE + 1]);

        List<String> errors = new ArrayList<>();

        generalEmailService.validateUploadedDocuments(finremCaseData, "authToken", errors);

        assertThat(errors).containsOnly("You attached a document which exceeds the size limit: 2MB");
        verify(bulkPrintDocumentService).validateEncryptionOnUploadedDocument(
            eq(largeDocument),
            any(),
            anyList(),
            eq("authToken")
        );
        verify(evidenceManagementDownloadService).getByteArray(largeDocument, "authToken");
    }
}
