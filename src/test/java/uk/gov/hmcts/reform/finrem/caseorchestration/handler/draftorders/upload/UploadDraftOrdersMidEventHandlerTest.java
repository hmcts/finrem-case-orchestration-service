package uk.gov.hmcts.reform.finrem.caseorchestration.handler.draftorders.upload;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.agreed.AgreedDraftOrderAdditionalDocumentsCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.agreed.UploadAgreedDraftOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.agreed.UploadAgreedDraftOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.agreed.UploadedDraftOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.suggested.SuggestedDraftOrderAdditionalDocumentsCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.suggested.UploadSuggestedDraftOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.suggested.UploadSuggestedDraftOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DraftOrdersWrapper;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.DraftOrdersConstants.AGREED_DRAFT_ORDER_OPTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class UploadDraftOrdersMidEventHandlerTest {

    @InjectMocks
    private UploadDraftOrdersMidEventHandler handler;

    @Mock
    private FinremCaseDetailsMapper finremCaseDetailsMapper;

    @Test
    void canHandle() {
        assertCanHandle(handler, CallbackType.MID_EVENT, CaseType.CONTESTED, EventType.DRAFT_ORDERS);
    }

    private static UploadAgreedDraftOrderCollection toUploadAgreedDraftOrderCollection(String documentName, String... attachmentDocumentNames) {
        List<AgreedDraftOrderAdditionalDocumentsCollection> additionalDocuments = attachmentDocumentNames == null
            || attachmentDocumentNames.length == 0
            ? Collections.emptyList()
            : Arrays.stream(attachmentDocumentNames)
            .map(name -> AgreedDraftOrderAdditionalDocumentsCollection.builder()
                .value(CaseDocument.builder().documentFilename(name).build())
                .build())
            .toList();

        return UploadAgreedDraftOrderCollection.builder()
            .value(UploadedDraftOrder.builder()
                .agreedDraftOrderDocument(CaseDocument.builder().documentFilename(documentName).build())
                .agreedDraftOrderAdditionalDocumentsCollection(additionalDocuments)
                .build())
            .build();
    }

    private static UploadSuggestedDraftOrderCollection toUploadSuggestedDraftOrderCollection(String documentName, String... attachmentDocumentNames) {
        List<SuggestedDraftOrderAdditionalDocumentsCollection> additionalDocuments = attachmentDocumentNames == null
            || attachmentDocumentNames.length == 0
            ? Collections.emptyList()
            : Arrays.stream(attachmentDocumentNames)
            .map(name -> SuggestedDraftOrderAdditionalDocumentsCollection.builder()
                .value(CaseDocument.builder().documentFilename(name).build())
                .build())
            .toList();

        return UploadSuggestedDraftOrderCollection.builder()
            .value(uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.suggested.UploadedDraftOrder.builder()
                .suggestedDraftOrderDocument(CaseDocument.builder().documentFilename(documentName).build())
                .suggestedDraftOrderAdditionalDocumentsCollection(additionalDocuments)
                .build())
            .build();
    }

    @ParameterizedTest
    @MethodSource("provideAgreedDraftOrderTestCases")
    void shouldReturnNoErrorsWhenAllDocumentsAreWordFiles(List<UploadAgreedDraftOrderCollection> agreedDraftOrderCollection) {
        // Create the FinremCallbackRequest using the provided UploadAgreedDraftOrderCollection
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response =
            handler.handle(FinremCallbackRequestFactory.from(1727874196328932L, FinremCaseData.builder()
                .draftOrdersWrapper(DraftOrdersWrapper.builder()
                    .uploadAgreedDraftOrder(UploadAgreedDraftOrder.builder()
                        .uploadAgreedDraftOrderCollection(agreedDraftOrderCollection)
                        .build())
                    .build())
                .build()), AUTH_TOKEN);

        assertThat(response.getErrors()).isEmpty();
        assertThat(response.getData()).isNotNull();
    }

    private static Stream<Arguments> provideAgreedDraftOrderTestCases() {
        return Stream.of(
            // Case 1: All valid Word documents
            Arguments.of(List.of(
                toUploadAgreedDraftOrderCollection("sample1.doc"),
                toUploadAgreedDraftOrderCollection("sample2.doc")
            )),
            // Case 2: One null document value
            Arguments.of(List.of(
                UploadAgreedDraftOrderCollection.builder().value(null).build(),
                toUploadAgreedDraftOrderCollection("sample.doc")
            )),
            // Case 3: Special characters in filenames
            Arguments.of(List.of(
                toUploadAgreedDraftOrderCollection("sample@file.doc"),
                toUploadAgreedDraftOrderCollection("sample file.doc")
            )),
            // Case 4: Very long filename
            Arguments.of(List.of(
                toUploadAgreedDraftOrderCollection("a".repeat(255) + ".doc")
            )),
            // Case 5: Multiple valid Word documents
            Arguments.of(List.of(
                toUploadAgreedDraftOrderCollection("valid1.doc"),
                toUploadAgreedDraftOrderCollection("valid2.doc"),
                toUploadAgreedDraftOrderCollection("valid3.doc")
            ))
        );
    }

    @ParameterizedTest
    @MethodSource("provideNonWordDocumentEdgeCases")
    void shouldReturnErrorWhenNonWordDocumentsAreUploaded(List<UploadAgreedDraftOrderCollection> agreedDraftOrderCollection,
                                                          boolean agreedDraftOrderRejected, boolean attachmentFormatRejected) {
        // Create the FinremCallbackRequest using the provided UploadAgreedDraftOrderCollection
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response =
            handler.handle(FinremCallbackRequestFactory.from(1727874196328932L, FinremCaseData.builder()
                .draftOrdersWrapper(DraftOrdersWrapper.builder()
                    .typeOfDraftOrder(AGREED_DRAFT_ORDER_OPTION)
                    .uploadAgreedDraftOrder(UploadAgreedDraftOrder.builder()
                        .uploadAgreedDraftOrderCollection(agreedDraftOrderCollection)
                        .build())
                    .build())
                .build()), AUTH_TOKEN);

        if (!(agreedDraftOrderRejected || attachmentFormatRejected)) {
            assertThat(response.getErrors()).isEmpty();
        } else {
            String wordDocErrorMessage = "You must upload Microsoft Word documents. "
                + "Document names should clearly reflect the party name, the type of hearing and the date of the hearing.";
            String rejectedAttachmentMessage = "You must upload a PDF file in the additional attachments.";

            if (agreedDraftOrderRejected && attachmentFormatRejected) {
                assertThat(response.getErrors()).containsExactlyInAnyOrder(wordDocErrorMessage, rejectedAttachmentMessage);
            } else if (agreedDraftOrderRejected) {
                assertThat(response.getErrors()).contains(wordDocErrorMessage);
            } else {
                assertThat(response.getErrors()).contains(rejectedAttachmentMessage);
            }
        }
        assertThat(response.getData()).isNotNull();
    }
    
    private static Stream<org.junit.jupiter.params.provider.Arguments> provideNonWordDocumentEdgeCases() {
        return Stream.of(
            // Case 0: One document with an empty filename
            Arguments.of(List.of(
                toUploadAgreedDraftOrderCollection(""),
                toUploadAgreedDraftOrderCollection("sample.doc")
            ), true, false),
            // Case 1: Empty document filename
            Arguments.of(List.of(
                toUploadAgreedDraftOrderCollection("")
            ), true, false),
            // Case 2: Null document
            Arguments.of(List.of(
                UploadAgreedDraftOrderCollection.builder().value(UploadedDraftOrder.builder().agreedDraftOrderDocument(null).build()).build()
            ), false, false),
            // Case 3: Mixed document types
            Arguments.of(List.of(
                toUploadAgreedDraftOrderCollection("sample.doc"),
                toUploadAgreedDraftOrderCollection("sample.pdf")
            ), true, false),
            // Case 4: Unusual file extension
            Arguments.of(List.of(
                toUploadAgreedDraftOrderCollection("sample.docx.txt")
            ), true, false),
            // Case 5: Multiple non-Word documents
            Arguments.of(List.of(
                toUploadAgreedDraftOrderCollection("sample1.pdf"),
                toUploadAgreedDraftOrderCollection("sample2.txt")
            ), true, false),
            // Case 6: With attachment cases
            Arguments.of(List.of(
                toUploadAgreedDraftOrderCollection("sample.doc", "sampleAttachment.pdf")
            ), false, false),
            Arguments.of(List.of(
                toUploadAgreedDraftOrderCollection("sample.doc", "sampleAttachment.docx")
            ), false, true),
            Arguments.of(List.of(
                toUploadAgreedDraftOrderCollection("sample.doc", "sampleAttachment.pdf", "sampleAttachment.txt")
            ), false, true),
            Arguments.of(List.of(
                toUploadAgreedDraftOrderCollection("sample.pdf", "sampleAttachment.pdf")
            ), true, false),
            Arguments.of(List.of(
                toUploadAgreedDraftOrderCollection("sample.pdf", "sampleAttachment.xls")
            ), true, true)
        );
    }

    @ParameterizedTest
    @MethodSource("provideSuggestedDraftOrderTestCases")
    void shouldReturnNoErrorsWhenAllDocumentsAreWordFilesForSuggestedDraftOrders(List<UploadSuggestedDraftOrderCollection>
                                                                                     suggestedDraftOrderCollection,
                                                                                 boolean suggestedOrderRejected, boolean attachmentFormatRejected) {
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response =
            handler.handle(FinremCallbackRequestFactory.from(1727874196328932L, FinremCaseData.builder()
                .draftOrdersWrapper(DraftOrdersWrapper.builder()
                    .typeOfDraftOrder("aSuggestedDraftOrderPriorToAListedHearing") // Set type of draft order
                    .uploadSuggestedDraftOrder(UploadSuggestedDraftOrder.builder()
                        .uploadSuggestedDraftOrderCollection(suggestedDraftOrderCollection)
                        .build())
                    .build())
                .build()), AUTH_TOKEN);

        if (!(suggestedOrderRejected || attachmentFormatRejected)) {
            assertThat(response.getErrors()).isEmpty();
        } else {
            String wordDocErrorMessage = "You must upload Microsoft Word documents. "
                + "Document names should clearly reflect the party name, the type of hearing and the date of the hearing.";
            String rejectedAttachmentMessage = "You must upload a PDF file in the additional attachments.";

            if (suggestedOrderRejected && attachmentFormatRejected) {
                assertThat(response.getErrors()).containsExactlyInAnyOrder(wordDocErrorMessage, rejectedAttachmentMessage);
            } else if (suggestedOrderRejected) {
                assertThat(response.getErrors()).contains(wordDocErrorMessage);
            } else {
                assertThat(response.getErrors()).contains(rejectedAttachmentMessage);
            }
        }
        assertThat(response.getData()).isNotNull();
    }

    private static Stream<Arguments> provideSuggestedDraftOrderTestCases() {
        return Stream.of(
            // Case 1: All valid Word documents
            Arguments.of(List.of(
                toUploadSuggestedDraftOrderCollection("suggested1.doc"),
                toUploadSuggestedDraftOrderCollection("suggested2.doc")
            ), false, false),
            // Case 2: One null document value
            Arguments.of(List.of(
                UploadSuggestedDraftOrderCollection.builder().value(null).build(),
                toUploadSuggestedDraftOrderCollection("suggested.doc")
            ), false, false),
            // Case 3: Invalid file type
            Arguments.of(List.of(
                toUploadSuggestedDraftOrderCollection("suggested.pdf")
            ), true, false),
            // Case 4: With attachment cases
            Arguments.of(List.of(
                toUploadSuggestedDraftOrderCollection("suggested.pdf", "sampleAttachment.pdf")
            ), true, false),
            Arguments.of(List.of(
                toUploadSuggestedDraftOrderCollection("suggested.doc", "sampleAttachment.pdf")
            ), false, false),
            Arguments.of(List.of(
                toUploadSuggestedDraftOrderCollection("suggested.doc", "sampleAttachment.doc")
            ), false, true),
            Arguments.of(List.of(
                toUploadSuggestedDraftOrderCollection("suggested.doc", "sampleAttachment.pdf", "sampleAttachment1.docx")
            ), false, true),
            Arguments.of(List.of(
                toUploadSuggestedDraftOrderCollection("suggested.png", "sampleAttachment1.bmp")
            ), true, true)
        );
    }
}
