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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.agreed.UploadAgreedDraftOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.agreed.UploadAgreedDraftOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.agreed.UploadedDraftOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.suggested.UploadSuggestedDraftOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.suggested.UploadSuggestedDraftOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DraftOrdersWrapper;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.DraftOrdersConstants.AGREED_DRAFT_ORDER_OPTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class UploadDraftOrderMidEventHandlerTest {

    @InjectMocks
    private UploadDraftOrdersMidEventHandler handler;

    @Mock
    private FinremCaseDetailsMapper finremCaseDetailsMapper;

    @Test
    void canHandle() {
        assertCanHandle(handler, CallbackType.MID_EVENT, CaseType.CONTESTED, EventType.DRAFT_ORDERS);
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
            Arguments.of(
                List.of(
                    UploadAgreedDraftOrderCollection.builder()
                        .value(UploadedDraftOrder.builder()
                            .agreedDraftOrderDocument(CaseDocument.builder().documentFilename("sample1.doc").build())
                            .build())
                        .build(),
                    UploadAgreedDraftOrderCollection.builder()
                        .value(UploadedDraftOrder.builder()
                            .agreedDraftOrderDocument(CaseDocument.builder().documentFilename("sample2.docx").build())
                            .build())
                        .build()
                )
            ),
            // Case 2: One null document value
            Arguments.of(
                List.of(
                    UploadAgreedDraftOrderCollection.builder()
                        .value(null) // Null document
                        .build(),
                    UploadAgreedDraftOrderCollection.builder()
                        .value(UploadedDraftOrder.builder()
                            .agreedDraftOrderDocument(CaseDocument.builder().documentFilename("sample.doc").build())
                            .build())
                        .build()
                )
            ),
            // Case 3: Special characters in filenames
            Arguments.of(
                List.of(
                    UploadAgreedDraftOrderCollection.builder()
                        .value(UploadedDraftOrder.builder()
                            .agreedDraftOrderDocument(CaseDocument.builder().documentFilename("sample@file.doc").build())
                            .build())
                        .build(),
                    UploadAgreedDraftOrderCollection.builder()
                        .value(UploadedDraftOrder.builder()
                            .agreedDraftOrderDocument(CaseDocument.builder().documentFilename("sample file.doc").build())
                            .build())
                        .build()
                )
            ),
            // Case 4: Very long filename
            Arguments.of(
                List.of(
                    UploadAgreedDraftOrderCollection.builder()
                        .value(UploadedDraftOrder.builder()
                            // Long filename
                            .agreedDraftOrderDocument(CaseDocument.builder().documentFilename("a".repeat(255) + ".doc").build())
                            .build())
                        .build()
                )
            ),
            // Case 5: Multiple valid Word documents
            Arguments.of(
                List.of(
                    UploadAgreedDraftOrderCollection.builder()
                        .value(UploadedDraftOrder.builder()
                            .agreedDraftOrderDocument(CaseDocument.builder().documentFilename("valid1.doc").build())
                            .build())
                        .build(),
                    UploadAgreedDraftOrderCollection.builder()
                        .value(UploadedDraftOrder.builder()
                            .agreedDraftOrderDocument(CaseDocument.builder().documentFilename("valid2.docx").build())
                            .build())
                        .build(),
                    UploadAgreedDraftOrderCollection.builder()
                        .value(UploadedDraftOrder.builder()
                            .agreedDraftOrderDocument(CaseDocument.builder().documentFilename("valid3.doc").build())
                            .build())
                        .build()
                )
            )
        );
    }

    @ParameterizedTest
    @MethodSource("provideNonWordDocumentEdgeCases")
    void shouldReturnErrorWhenNonWordDocumentsAreUploaded(List<UploadAgreedDraftOrderCollection> agreedDraftOrderCollection,
                                                          boolean showErrorMessage) {
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

        if (!showErrorMessage) {
            assertThat(response.getErrors()).isEmpty();
        } else {
            assertThat(response.getErrors()).containsExactly("You must upload Microsoft Word documents. "
                + "Document names should clearly reflect the party name, the type of hearing and the date of the hearing.");
        }
        assertThat(response.getData()).isNotNull();
    }

    private static Stream<org.junit.jupiter.params.provider.Arguments> provideNonWordDocumentEdgeCases() {
        return Stream.of(
            // Case 2: One document with an empty filename
            Arguments.of(
                List.of(
                    UploadAgreedDraftOrderCollection.builder()
                        .value(UploadedDraftOrder.builder()
                            .agreedDraftOrderDocument(CaseDocument.builder().documentFilename("").build())
                            .build())
                        .build(),
                    UploadAgreedDraftOrderCollection.builder()
                        .value(UploadedDraftOrder.builder()
                            .agreedDraftOrderDocument(CaseDocument.builder().documentFilename("sample.doc").build())
                            .build())
                        .build()
                ), true
            ),
            // Case 1: Empty document filename
            Arguments.of(
                List.of(
                    UploadAgreedDraftOrderCollection.builder()
                        .value(UploadedDraftOrder.builder()
                            .agreedDraftOrderDocument(CaseDocument.builder().documentFilename("").build())
                            .build())
                        .build()
                ), true
            ),
            // Case 2: Null document
            Arguments.of(
                List.of(
                    UploadAgreedDraftOrderCollection.builder()
                        .value(UploadedDraftOrder.builder()
                            .agreedDraftOrderDocument(null) // Null document
                            .build())
                        .build()
                ), false
            ),
            // Case 3: Mixed document types
            Arguments.of(
                List.of(
                    UploadAgreedDraftOrderCollection.builder()
                        .value(UploadedDraftOrder.builder()
                            .agreedDraftOrderDocument(CaseDocument.builder().documentFilename("sample.doc").build())
                            .build())
                        .build(),
                    UploadAgreedDraftOrderCollection.builder()
                        .value(UploadedDraftOrder.builder()
                            .agreedDraftOrderDocument(CaseDocument.builder().documentFilename("sample.pdf").build())
                            .build())
                        .build()
                ), true
            ),
            // Case 4: Unusual file extension
            Arguments.of(
                List.of(
                    UploadAgreedDraftOrderCollection.builder()
                        .value(UploadedDraftOrder.builder()
                            .agreedDraftOrderDocument(CaseDocument.builder().documentFilename("sample.docx.txt").build())
                            .build())
                        .build()
                ), true
            ),
            // Case 5: Multiple non-Word documents
            Arguments.of(
                List.of(
                    UploadAgreedDraftOrderCollection.builder()
                        .value(UploadedDraftOrder.builder()
                            .agreedDraftOrderDocument(CaseDocument.builder().documentFilename("sample1.pdf").build())
                            .build())
                        .build(),
                    UploadAgreedDraftOrderCollection.builder()
                        .value(UploadedDraftOrder.builder()
                            .agreedDraftOrderDocument(CaseDocument.builder().documentFilename("sample2.txt").build())
                            .build())
                        .build()
                ), true
            )
        );
    }

    @ParameterizedTest
    @MethodSource("provideSuggestedDraftOrderTestCases")
    void shouldReturnNoErrorsWhenAllDocumentsAreWordFilesForSuggestedDraftOrders(List<UploadSuggestedDraftOrderCollection>
                                                                                     suggestedDraftOrderCollection,
                                                                                 boolean showErrorMessage) {
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response =
            handler.handle(FinremCallbackRequestFactory.from(1727874196328932L, FinremCaseData.builder()
                .draftOrdersWrapper(DraftOrdersWrapper.builder()
                    .typeOfDraftOrder("aSuggestedDraftOrderPriorToAListedHearing") // Set type of draft order
                    .uploadSuggestedDraftOrder(UploadSuggestedDraftOrder.builder()
                        .uploadSuggestedDraftOrderCollection(suggestedDraftOrderCollection)
                        .build())
                    .build())
                .build()), AUTH_TOKEN);

        if (!showErrorMessage) {
            assertThat(response.getErrors()).isEmpty();
        } else {
            assertThat(response.getErrors()).containsExactly("You must upload Microsoft Word documents. "
                + "Document names should clearly reflect the party name, the type of hearing and the date of the hearing.");
        }
        assertThat(response.getData()).isNotNull();
    }

    private static Stream<Arguments> provideSuggestedDraftOrderTestCases() {
        return Stream.of(
            // Case 1: All valid Word documents
            Arguments.of(
                List.of(
                    UploadSuggestedDraftOrderCollection.builder()
                        .value(uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.suggested.UploadedDraftOrder.builder()
                            .suggestedDraftOrderDocument(CaseDocument.builder().documentFilename("suggested1.doc").build())
                            .build())
                        .build(),
                    UploadSuggestedDraftOrderCollection.builder()
                        .value(uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.suggested.UploadedDraftOrder.builder()
                            .suggestedDraftOrderDocument(CaseDocument.builder().documentFilename("suggested2.doc").build())
                            .build())
                        .build()
                ), false
            ),
            // Case 2: One null document value
            Arguments.of(
                List.of(
                    UploadSuggestedDraftOrderCollection.builder()
                        .value(null) // Null document
                        .build(),
                    UploadSuggestedDraftOrderCollection.builder()
                        .value(uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.suggested.UploadedDraftOrder.builder()
                            .suggestedDraftOrderDocument(CaseDocument.builder().documentFilename("suggested.doc").build())
                            .build())
                        .build()
                ), false
            ),
            // Case 3: Invalid file type
            Arguments.of(
                List.of(
                    UploadSuggestedDraftOrderCollection.builder()
                        .value(uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.suggested.UploadedDraftOrder.builder()
                            .suggestedDraftOrderDocument(CaseDocument.builder().documentFilename("suggested.pdf").build())
                            .build())
                        .build()
                ), true
            )
        );
    }
}
