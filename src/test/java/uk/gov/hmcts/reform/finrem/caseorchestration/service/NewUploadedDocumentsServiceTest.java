package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HasUploadingDocuments;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadGeneralDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadGeneralDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.agreed.AgreedPensionSharingAnnex;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.agreed.AgreedPensionSharingAnnexCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.agreed.UploadAgreedDraftOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.agreed.UploadAgreedDraftOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.agreed.UploadedDraftOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.suggested.SuggestedPensionSharingAnnex;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.suggested.SuggestedPensionSharingAnnexCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.suggested.UploadSuggestedDraftOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.suggested.UploadSuggestedDraftOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DraftOrdersWrapper;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;
import static java.util.stream.Stream.concat;
import static org.apache.commons.collections4.ListUtils.emptyIfNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;

class NewUploadedDocumentsServiceTest {

    private final NewUploadedDocumentsService underTest = new NewUploadedDocumentsService();

    @ParameterizedTest
    @MethodSource("provideArguments")
    void givenCaseWithExistingDocument_whenDocumentUploadedOrNot_thenReturnExpectedDocument(
        Function<FinremCaseData.FinremCaseDataBuilder, FinremCaseData.FinremCaseDataBuilder> caseDataBeforeModifier,
        Function<FinremCaseData.FinremCaseDataBuilder, FinremCaseData.FinremCaseDataBuilder> caseDataModifier,
        Function<FinremCaseData, List<HasUploadingDocuments>> getDocumentsFromCaseData,
        List<CaseDocument> expectedReturn) {
        FinremCaseData caseDataBefore = caseDataBeforeModifier.apply(FinremCaseData.builder()).build();
        FinremCaseData caseData = caseDataModifier.apply(FinremCaseData.builder()).build();
        assertEquals(expectedReturn, underTest.getNewUploadDocuments(caseData, caseDataBefore, getDocumentsFromCaseData));
    }

    private static List<UploadGeneralDocumentCollection> listOfUploadGeneralDocumentCollectionWithNullValue() {
        return List.of(UploadGeneralDocumentCollection.builder().value(null).build());
    }

    private static List<UploadGeneralDocumentCollection> listOfUploadGeneralDocumentCollectionWithNullDocumentLink() {
        return List.of(UploadGeneralDocumentCollection.builder().value(UploadGeneralDocument.builder().documentLink(null).build()).build());
    }

    private static Function<FinremCaseData, ?> uploadGeneralDocumentGetDocumentsFromCaseData() {
        return data -> emptyIfNull(data.getUploadGeneralDocuments()).stream().map(UploadGeneralDocumentCollection::getValue).toList();
    }

    private static Function<FinremCaseData, ?> uploadDocumentGetDocumentsFromCaseData() {
        return data -> emptyIfNull(data.getUploadDocuments()).stream().map(UploadDocumentCollection::getValue).toList();
    }

    private static Function<FinremCaseData, ?> uploadAgreedDraftOrderGetDocumentsFromCaseData() {
        return data -> concat(
            emptyIfNull(ofNullable(data.getDraftOrdersWrapper().getUploadAgreedDraftOrder()).orElse(UploadAgreedDraftOrder.builder().build())
                .getUploadAgreedDraftOrderCollection()
            ).stream().map(UploadAgreedDraftOrderCollection::getValue),
            emptyIfNull(ofNullable(data.getDraftOrdersWrapper().getUploadAgreedDraftOrder()).orElse(UploadAgreedDraftOrder.builder().build())
                .getAgreedPsaCollection()
            ).stream().map(AgreedPensionSharingAnnexCollection::getValue)
        ).toList();
    }

    private static Function<FinremCaseData, ?> uploadSuggestedDraftOrderGetDocumentsFromCaseData() {
        return data -> concat(
            emptyIfNull(ofNullable(data.getDraftOrdersWrapper().getUploadSuggestedDraftOrder()).orElse(UploadSuggestedDraftOrder.builder().build())
                .getUploadSuggestedDraftOrderCollection()
            ).stream().map(UploadSuggestedDraftOrderCollection::getValue),
            emptyIfNull(ofNullable(data.getDraftOrdersWrapper().getUploadSuggestedDraftOrder()).orElse(UploadSuggestedDraftOrder.builder().build())
                .getSuggestedPsaCollection()
            ).stream().map(SuggestedPensionSharingAnnexCollection::getValue)
        ).toList();
    }

    private static UploadGeneralDocumentCollection uploadGeneralDocumentCollection(String documentName) {
        return UploadGeneralDocumentCollection.builder()
            .value(UploadGeneralDocument.builder().documentLink(toCaseDocument(documentName)).build())
            .build();
    }

    private static UploadDocumentCollection uploadDocumentCollection(String documentName) {
        return UploadDocumentCollection.builder()
            .value(UploadDocument.builder().documentLink(toCaseDocument(documentName)).build())
            .build();
    }

    private static UploadAgreedDraftOrderCollection uploadAgreedDraftOrderCollection(String documentName) {
        return UploadAgreedDraftOrderCollection.builder()
            .value(UploadedDraftOrder.builder().agreedDraftOrderDocument(toCaseDocument(documentName)).build())
            .build();
    }

    private static UploadSuggestedDraftOrderCollection uploadSuggestedDraftOrderCollection(String documentName) {
        return UploadSuggestedDraftOrderCollection.builder()
            .value(uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.suggested.UploadedDraftOrder.builder()
                .suggestedDraftOrderDocument(toCaseDocument(documentName)).build())
            .build();
    }

    private static AgreedPensionSharingAnnexCollection uploadAgreedPensionSharingAnnexCollection(String documentName) {
        return AgreedPensionSharingAnnexCollection.builder()
            .value(AgreedPensionSharingAnnex.builder().agreedPensionSharingAnnexes(toCaseDocument(documentName)).build())
            .build();
    }

    private static SuggestedPensionSharingAnnexCollection uploadSuggestedPensionSharingAnnexCollection(String documentName) {
        return SuggestedPensionSharingAnnexCollection.builder()
            .value(SuggestedPensionSharingAnnex.builder().suggestedPensionSharingAnnexes(toCaseDocument(documentName)).build())
            .build();
    }

    private static CaseDocument toCaseDocument(String documentName) {
        return caseDocument(format("http://fakeurl/%s", documentName), documentName);
    }

    private static Stream<Arguments> provideArguments() {
        return Stream.of(
            // 1. UploadGeneralDocuments
            // 1.1.1 with null value in the existing docs
            Arguments.of((
                Function<FinremCaseData.FinremCaseDataBuilder, FinremCaseData.FinremCaseDataBuilder>) finremCaseDataBuilder -> {
                    finremCaseDataBuilder.uploadGeneralDocuments(listOfUploadGeneralDocumentCollectionWithNullValue());
                    return finremCaseDataBuilder;
                },
                (Function<FinremCaseData.FinremCaseDataBuilder, FinremCaseData.FinremCaseDataBuilder>) finremCaseDataBuilder -> {
                    finremCaseDataBuilder.uploadGeneralDocuments(concat(listOfUploadGeneralDocumentCollectionWithNullValue().stream(),
                        Stream.of(uploadGeneralDocumentCollection("filename1.1.1"))).toList());
                    return finremCaseDataBuilder;
                },
                uploadGeneralDocumentGetDocumentsFromCaseData(),
                List.of(toCaseDocument("filename1.1.1"))
            ),
            // 1.1.2 with null document link in the existing docs
            Arguments.of(
                (Function<FinremCaseData.FinremCaseDataBuilder, FinremCaseData.FinremCaseDataBuilder>) finremCaseDataBuilder -> {
                    finremCaseDataBuilder.uploadGeneralDocuments(listOfUploadGeneralDocumentCollectionWithNullDocumentLink());
                    return finremCaseDataBuilder;
                },
                (Function<FinremCaseData.FinremCaseDataBuilder, FinremCaseData.FinremCaseDataBuilder>) finremCaseDataBuilder -> {
                    finremCaseDataBuilder.uploadGeneralDocuments(concat(listOfUploadGeneralDocumentCollectionWithNullDocumentLink().stream(),
                        Stream.of(uploadGeneralDocumentCollection("filename1.1.2"))).toList());
                    return finremCaseDataBuilder;
                },
                uploadGeneralDocumentGetDocumentsFromCaseData(),
                List.of(toCaseDocument("filename1.1.2"))
            ),
            // 1.1.2 with null document link in the new doc
            Arguments.of(
                (Function<FinremCaseData.FinremCaseDataBuilder, FinremCaseData.FinremCaseDataBuilder>) finremCaseDataBuilder -> {
                    finremCaseDataBuilder.uploadGeneralDocuments(List.of(uploadGeneralDocumentCollection("exFilename1.1.3")));
                    return finremCaseDataBuilder;
                },
                (Function<FinremCaseData.FinremCaseDataBuilder, FinremCaseData.FinremCaseDataBuilder>) finremCaseDataBuilder -> {
                    finremCaseDataBuilder.uploadGeneralDocuments(concat(Stream.of(uploadGeneralDocumentCollection("exFilename1.1.3")),
                        listOfUploadGeneralDocumentCollectionWithNullDocumentLink().stream()).toList());
                    return finremCaseDataBuilder;
                },
                uploadGeneralDocumentGetDocumentsFromCaseData(),
                List.of()
            ),
            // 1.2 with new doc
            Arguments.of(
                (Function<FinremCaseData.FinremCaseDataBuilder, FinremCaseData.FinremCaseDataBuilder>) finremCaseDataBuilder -> {
                    finremCaseDataBuilder.uploadGeneralDocuments(List.of(uploadGeneralDocumentCollection("exFilename1.2")));
                    return finremCaseDataBuilder;
                },
                (Function<FinremCaseData.FinremCaseDataBuilder, FinremCaseData.FinremCaseDataBuilder>) finremCaseDataBuilder -> {
                    finremCaseDataBuilder.uploadGeneralDocuments(concat(Stream.of(uploadGeneralDocumentCollection("exFilename1.2")),
                        Stream.of(uploadGeneralDocumentCollection("filename1.2"))).toList());
                    return finremCaseDataBuilder;
                },
                uploadGeneralDocumentGetDocumentsFromCaseData(),
                List.of(toCaseDocument("filename1.2"))
            ),

            // 1.3 without new doc - no change
            Arguments.of(
                (Function<FinremCaseData.FinremCaseDataBuilder, FinremCaseData.FinremCaseDataBuilder>) finremCaseDataBuilder -> {
                    finremCaseDataBuilder.uploadGeneralDocuments(List.of(uploadGeneralDocumentCollection("exFilename1.3")));
                    return finremCaseDataBuilder;
                },
                (Function<FinremCaseData.FinremCaseDataBuilder, FinremCaseData.FinremCaseDataBuilder>) finremCaseDataBuilder -> {
                    finremCaseDataBuilder.uploadGeneralDocuments(List.of(uploadGeneralDocumentCollection("exFilename1.3")));
                    return finremCaseDataBuilder;
                },
                uploadGeneralDocumentGetDocumentsFromCaseData(),
                List.of()
            ),
            // 1.4 with multiple new docs
            Arguments.of(
                (Function<FinremCaseData.FinremCaseDataBuilder, FinremCaseData.FinremCaseDataBuilder>) finremCaseDataBuilder -> {
                    finremCaseDataBuilder.uploadGeneralDocuments(List.of(uploadGeneralDocumentCollection("exFilename1.4")));
                    return finremCaseDataBuilder;
                },
                (Function<FinremCaseData.FinremCaseDataBuilder, FinremCaseData.FinremCaseDataBuilder>) finremCaseDataBuilder -> {
                    finremCaseDataBuilder.uploadGeneralDocuments(concat(Stream.of(uploadGeneralDocumentCollection("exFilename1.4")),
                        Stream.of(uploadGeneralDocumentCollection("filename1.4-1"),
                            uploadGeneralDocumentCollection("filename1.4-2"))).toList());
                    return finremCaseDataBuilder;
                },
                uploadGeneralDocumentGetDocumentsFromCaseData(),
                List.of(toCaseDocument("filename1.4-1"), toCaseDocument("filename1.4-2"))
            ),
            // 1.5 no existing doc with a new doc
            Arguments.of(
                Function.identity(),
                (Function<FinremCaseData.FinremCaseDataBuilder, FinremCaseData.FinremCaseDataBuilder>) finremCaseDataBuilder -> {
                    finremCaseDataBuilder.uploadGeneralDocuments(List.of(uploadGeneralDocumentCollection("filename1.5")));
                    return finremCaseDataBuilder;
                },
                uploadGeneralDocumentGetDocumentsFromCaseData(),
                List.of(toCaseDocument("filename1.5"))
            ),
            // 1.6 removing existing doc
            Arguments.of(
                (Function<FinremCaseData.FinremCaseDataBuilder, FinremCaseData.FinremCaseDataBuilder>) finremCaseDataBuilder -> {
                    finremCaseDataBuilder.uploadGeneralDocuments(List.of(uploadGeneralDocumentCollection("exFilename1.6")));
                    return finremCaseDataBuilder;
                },
                Function.identity(),
                uploadGeneralDocumentGetDocumentsFromCaseData(),
                List.of()
            ),
            // 2. UploadDocuments
            // 2.1 with multiple new docs
            Arguments.of(
                (Function<FinremCaseData.FinremCaseDataBuilder, FinremCaseData.FinremCaseDataBuilder>) finremCaseDataBuilder -> {
                    finremCaseDataBuilder.uploadDocuments(List.of(uploadDocumentCollection("exFilename2.1")));
                    return finremCaseDataBuilder;
                },
                (Function<FinremCaseData.FinremCaseDataBuilder, FinremCaseData.FinremCaseDataBuilder>) finremCaseDataBuilder -> {
                    finremCaseDataBuilder.uploadDocuments(concat(Stream.of(uploadDocumentCollection("exFilename2.1")),
                        Stream.of(uploadDocumentCollection("filename2.1-1"),
                            uploadDocumentCollection("filename2.1-2"))).toList());
                    return finremCaseDataBuilder;
                },
                uploadDocumentGetDocumentsFromCaseData(),
                List.of(toCaseDocument("filename2.1-1"), toCaseDocument("filename2.1-2"))
            ),
            // 3. UploadAgreedDraftOrder
            Arguments.of(
                (Function<FinremCaseData.FinremCaseDataBuilder, FinremCaseData.FinremCaseDataBuilder>) finremCaseDataBuilder -> {
                    finremCaseDataBuilder.draftOrdersWrapper(DraftOrdersWrapper.builder()
                            .uploadAgreedDraftOrder(UploadAgreedDraftOrder.builder()
                                .uploadAgreedDraftOrderCollection(List.of(
                                    uploadAgreedDraftOrderCollection("exFilename3.1")
                                ))
                                .build())
                        .build());
                    return finremCaseDataBuilder;
                },
                (Function<FinremCaseData.FinremCaseDataBuilder, FinremCaseData.FinremCaseDataBuilder>) finremCaseDataBuilder -> {
                    finremCaseDataBuilder.draftOrdersWrapper(DraftOrdersWrapper.builder()
                        .uploadAgreedDraftOrder(UploadAgreedDraftOrder.builder()
                            .uploadAgreedDraftOrderCollection(List.of(
                                uploadAgreedDraftOrderCollection("exFilename3.1"),
                                uploadAgreedDraftOrderCollection("filename3.1-1")
                            ))
                            .agreedPsaCollection(List.of(
                                uploadAgreedPensionSharingAnnexCollection("filename3.1-2")
                            ))
                            .build())
                        .build());
                    return finremCaseDataBuilder;
                },
                uploadAgreedDraftOrderGetDocumentsFromCaseData(),
                List.of(toCaseDocument("filename3.1-1"), toCaseDocument("filename3.1-2"))
            ),
            // 4. UploadSuggestedDraftOrder
            Arguments.of(
                (Function<FinremCaseData.FinremCaseDataBuilder, FinremCaseData.FinremCaseDataBuilder>) finremCaseDataBuilder -> {
                    finremCaseDataBuilder.draftOrdersWrapper(DraftOrdersWrapper.builder()
                        .uploadSuggestedDraftOrder(UploadSuggestedDraftOrder.builder()
                            .uploadSuggestedDraftOrderCollection(List.of(
                                uploadSuggestedDraftOrderCollection("exFilename4.1")
                            ))
                            .build())
                        .build());
                    return finremCaseDataBuilder;
                },
                (Function<FinremCaseData.FinremCaseDataBuilder, FinremCaseData.FinremCaseDataBuilder>) finremCaseDataBuilder -> {
                    finremCaseDataBuilder.draftOrdersWrapper(DraftOrdersWrapper.builder()
                        .uploadSuggestedDraftOrder(UploadSuggestedDraftOrder.builder()
                            .uploadSuggestedDraftOrderCollection(List.of(
                                uploadSuggestedDraftOrderCollection("exFilename4.1"),
                                uploadSuggestedDraftOrderCollection("filename4.1-1")
                            ))
                            .suggestedPsaCollection(List.of(
                                uploadSuggestedPensionSharingAnnexCollection("filename4.1-2")
                            ))
                            .build())
                        .build());
                    return finremCaseDataBuilder;
                },
                uploadSuggestedDraftOrderGetDocumentsFromCaseData(),
                List.of(toCaseDocument("filename4.1-1"), toCaseDocument("filename4.1-2"))
            ),
            // 4.2 without PSA
            Arguments.of(
                (Function<FinremCaseData.FinremCaseDataBuilder, FinremCaseData.FinremCaseDataBuilder>) finremCaseDataBuilder -> {
                    finremCaseDataBuilder.draftOrdersWrapper(DraftOrdersWrapper.builder()
                        .uploadSuggestedDraftOrder(UploadSuggestedDraftOrder.builder()
                            .uploadSuggestedDraftOrderCollection(List.of(
                                uploadSuggestedDraftOrderCollection("exFilename4.2")
                            ))
                            .build())
                        .build());
                    return finremCaseDataBuilder;
                },
                (Function<FinremCaseData.FinremCaseDataBuilder, FinremCaseData.FinremCaseDataBuilder>) finremCaseDataBuilder -> {
                    finremCaseDataBuilder.draftOrdersWrapper(DraftOrdersWrapper.builder()
                        .uploadSuggestedDraftOrder(UploadSuggestedDraftOrder.builder()
                            .uploadSuggestedDraftOrderCollection(List.of(
                                uploadSuggestedDraftOrderCollection("exFilename4.2"),
                                uploadSuggestedDraftOrderCollection("filename4.2-1")
                            ))
                            .build())
                        .build());
                    return finremCaseDataBuilder;
                },
                uploadSuggestedDraftOrderGetDocumentsFromCaseData(),
                List.of(toCaseDocument("filename4.2-1"))
            )
        );
    }
}
