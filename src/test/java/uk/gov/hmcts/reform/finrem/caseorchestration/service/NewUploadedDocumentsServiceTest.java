package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadGeneralDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadGeneralDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadingDocumentsHolder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.agreed.AgreedPensionSharingAnnex;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.agreed.AgreedPensionSharingAnnexCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.agreed.UploadAgreedDraftOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.agreed.UploadAgreedDraftOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.agreed.UploadedDraftOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DraftOrdersWrapper;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;

class NewUploadedDocumentsServiceTest {

    private final NewUploadedDocumentsService underTest = new NewUploadedDocumentsService();

    private static Stream<Arguments> provideArguments() {
        final List<UploadGeneralDocumentCollection> existingUploadGeneralDocument = List.of(
            UploadGeneralDocumentCollection.builder()
                .value(UploadGeneralDocument.builder()
                    .documentLink(caseDocument("url1", "binaryUrl1", "filename1"))
                    .build())
                .build()
        );
        final List<UploadGeneralDocumentCollection> existingUploadGeneralDocumentWithNullValue = List.of(
            UploadGeneralDocumentCollection.builder()
                .value(null)
                .build()
        );
        final List<UploadGeneralDocumentCollection> existingUploadGeneralDocumentWithNullDocumentLink = List.of(
            UploadGeneralDocumentCollection.builder()
                .value(UploadGeneralDocument.builder()
                    .documentLink(null)
                    .build())
                .build()
        );
        final List<UploadDocumentCollection> existingUploadDocument = List.of(
            UploadDocumentCollection.builder()
                .value(UploadDocument.builder()
                    .documentLink(caseDocument("url1", "binaryUrl1", "filename1"))
                    .build())
                .build()
        );

        return Stream.of(
            // 1. uploadGeneralDocuments
            // 1.1.1 with null value in the existing docs
            Arguments.of((Function<FinremCaseData.FinremCaseDataBuilder, FinremCaseData.FinremCaseDataBuilder>) finremCaseDataBuilder -> {
                    finremCaseDataBuilder.uploadGeneralDocuments(existingUploadGeneralDocumentWithNullValue);
                    return finremCaseDataBuilder;
                },
                (Function<FinremCaseData.FinremCaseDataBuilder, FinremCaseData.FinremCaseDataBuilder>) finremCaseDataBuilder -> {
                    finremCaseDataBuilder.uploadGeneralDocuments(Stream.concat(existingUploadGeneralDocumentWithNullValue.stream(), Stream.of(
                        UploadGeneralDocumentCollection.builder()
                            .value(UploadGeneralDocument.builder()
                                .documentLink(caseDocument("newUrl1", "newBinaryUrl1", "newFilename1"))
                                .build())
                            .build()
                    )).toList());
                    return finremCaseDataBuilder;
                },
                (Function<FinremCaseData, ?>) FinremCaseData::getUploadGeneralDocuments,
                List.of(caseDocument("newUrl1", "newBinaryUrl1", "newFilename1"))
            ),
            // 1.1.2 with null document link in the existing docs
            Arguments.of((Function<FinremCaseData.FinremCaseDataBuilder, FinremCaseData.FinremCaseDataBuilder>) finremCaseDataBuilder -> {
                    finremCaseDataBuilder.uploadGeneralDocuments(existingUploadGeneralDocumentWithNullDocumentLink);
                    return finremCaseDataBuilder;
                },
                (Function<FinremCaseData.FinremCaseDataBuilder, FinremCaseData.FinremCaseDataBuilder>) finremCaseDataBuilder -> {
                    finremCaseDataBuilder.uploadGeneralDocuments(Stream.concat(existingUploadGeneralDocumentWithNullDocumentLink.stream(), Stream.of(
                        UploadGeneralDocumentCollection.builder()
                            .value(UploadGeneralDocument.builder()
                                .documentLink(caseDocument("newUrl1", "newBinaryUrl1", "newFilename1"))
                                .build())
                            .build()
                    )).toList());
                    return finremCaseDataBuilder;
                },
                (Function<FinremCaseData, ?>) FinremCaseData::getUploadGeneralDocuments,
                List.of(caseDocument("newUrl1", "newBinaryUrl1", "newFilename1"))
            ),
            // 1.1.2 with null document link in the new doc
            Arguments.of((Function<FinremCaseData.FinremCaseDataBuilder, FinremCaseData.FinremCaseDataBuilder>) finremCaseDataBuilder -> {
                    finremCaseDataBuilder.uploadGeneralDocuments(existingUploadGeneralDocument);
                    return finremCaseDataBuilder;
                },
                (Function<FinremCaseData.FinremCaseDataBuilder, FinremCaseData.FinremCaseDataBuilder>) finremCaseDataBuilder -> {
                    finremCaseDataBuilder.uploadGeneralDocuments(Stream.concat(existingUploadGeneralDocument.stream(), Stream.of(
                        UploadGeneralDocumentCollection.builder()
                            .value(UploadGeneralDocument.builder()
                                .documentLink(null)
                                .build())
                            .build()
                    )).toList());
                    return finremCaseDataBuilder;
                },
                (Function<FinremCaseData, ?>) FinremCaseData::getUploadGeneralDocuments,
                List.of()
            ),
            // 1.2 with new doc
            Arguments.of((Function<FinremCaseData.FinremCaseDataBuilder, FinremCaseData.FinremCaseDataBuilder>) finremCaseDataBuilder -> {
                    finremCaseDataBuilder.uploadGeneralDocuments(existingUploadGeneralDocument);
                    return finremCaseDataBuilder;
                },
                (Function<FinremCaseData.FinremCaseDataBuilder, FinremCaseData.FinremCaseDataBuilder>) finremCaseDataBuilder -> {
                    finremCaseDataBuilder.uploadGeneralDocuments(Stream.concat(existingUploadGeneralDocument.stream(), Stream.of(
                        UploadGeneralDocumentCollection.builder()
                            .value(UploadGeneralDocument.builder()
                                .documentLink(caseDocument("newUrl1", "newBinaryUrl1", "newFilename1"))
                                .build())
                            .build()
                    )).toList());
                    return finremCaseDataBuilder;
                },
                (Function<FinremCaseData, ?>) FinremCaseData::getUploadGeneralDocuments,
                List.of(caseDocument("newUrl1", "newBinaryUrl1", "newFilename1"))
            ),
            // 1.3 without new doc - no change
            Arguments.of((Function<FinremCaseData.FinremCaseDataBuilder, FinremCaseData.FinremCaseDataBuilder>) finremCaseDataBuilder -> {
                    finremCaseDataBuilder.uploadGeneralDocuments(existingUploadGeneralDocument);
                    return finremCaseDataBuilder;
                },
                (Function<FinremCaseData.FinremCaseDataBuilder, FinremCaseData.FinremCaseDataBuilder>) finremCaseDataBuilder -> {
                    finremCaseDataBuilder.uploadGeneralDocuments(existingUploadGeneralDocument);
                    return finremCaseDataBuilder;
                },
                (Function<FinremCaseData, ?>) FinremCaseData::getUploadGeneralDocuments,
                List.of()
            ),
            // 1.4 with multiple new docs
            Arguments.of((Function<FinremCaseData.FinremCaseDataBuilder, FinremCaseData.FinremCaseDataBuilder>) finremCaseDataBuilder -> {
                    finremCaseDataBuilder.uploadGeneralDocuments(existingUploadGeneralDocument);
                    return finremCaseDataBuilder;
                },
                (Function<FinremCaseData.FinremCaseDataBuilder, FinremCaseData.FinremCaseDataBuilder>) finremCaseDataBuilder -> {
                    finremCaseDataBuilder.uploadGeneralDocuments(Stream.concat(existingUploadGeneralDocument.stream(), Stream.of(
                        UploadGeneralDocumentCollection.builder()
                            .value(UploadGeneralDocument.builder()
                                .documentLink(caseDocument("newUrl1", "newBinaryUrl1", "newFilename1"))
                                .build())
                            .build(),

                        UploadGeneralDocumentCollection.builder()
                            .value(UploadGeneralDocument.builder()
                                .documentLink(caseDocument("newUrl2", "newBinaryUrl2", "newFilename2"))
                                .build())
                            .build()
                    )).toList());
                    return finremCaseDataBuilder;
                },
                (Function<FinremCaseData, ?>) FinremCaseData::getUploadGeneralDocuments,
                List.of(caseDocument("newUrl1", "newBinaryUrl1", "newFilename1"),
                    caseDocument("newUrl2", "newBinaryUrl2", "newFilename2"))
            ),
            // 1.5 no existing doc with a new doc
            Arguments.of((Function<FinremCaseData.FinremCaseDataBuilder, FinremCaseData.FinremCaseDataBuilder>) finremCaseDataBuilder ->
                    finremCaseDataBuilder,
                (Function<FinremCaseData.FinremCaseDataBuilder, FinremCaseData.FinremCaseDataBuilder>) finremCaseDataBuilder -> {
                    finremCaseDataBuilder.uploadGeneralDocuments(List.of(
                        UploadGeneralDocumentCollection.builder()
                            .value(UploadGeneralDocument.builder()
                                .documentLink(caseDocument("newUrl1", "newBinaryUrl1", "newFilename1"))
                                .build())
                            .build()
                    ));
                    return finremCaseDataBuilder;
                },
                (Function<FinremCaseData, ?>) FinremCaseData::getUploadGeneralDocuments,
                List.of(caseDocument("newUrl1", "newBinaryUrl1", "newFilename1"))
            ),
            // 1.6 removing existing doc
            Arguments.of((Function<FinremCaseData.FinremCaseDataBuilder, FinremCaseData.FinremCaseDataBuilder>) finremCaseDataBuilder -> {
                    finremCaseDataBuilder.uploadGeneralDocuments(existingUploadGeneralDocument);
                    return finremCaseDataBuilder;
                },
                (Function<FinremCaseData.FinremCaseDataBuilder, FinremCaseData.FinremCaseDataBuilder>) finremCaseDataBuilder ->
                    finremCaseDataBuilder,
                (Function<FinremCaseData, ?>) FinremCaseData::getUploadGeneralDocuments,
                List.of()
            ),
            // 2. uploadDocuments
            // 2.1 with new doc
            Arguments.of((Function<FinremCaseData.FinremCaseDataBuilder, FinremCaseData.FinremCaseDataBuilder>) finremCaseDataBuilder -> {
                    finremCaseDataBuilder.uploadDocuments(existingUploadDocument);
                    return finremCaseDataBuilder;
                },
                (Function<FinremCaseData.FinremCaseDataBuilder, FinremCaseData.FinremCaseDataBuilder>) finremCaseDataBuilder -> {
                    finremCaseDataBuilder.uploadDocuments(Stream.concat(existingUploadDocument.stream(), Stream.of(
                        UploadDocumentCollection.builder()
                            .value(UploadDocument.builder()
                                .documentLink(caseDocument("newUrl", "newBinaryUrl1", "newFilename"))
                                .build())
                            .build()
                    )).toList());
                    return finremCaseDataBuilder;
                },
                (Function<FinremCaseData, ?>) FinremCaseData::getUploadDocuments,
                List.of(caseDocument("newUrl", "newBinaryUrl1", "newFilename"))
            ),
            // 3. uploadAgreedDraftOrder.draftOrder
            Arguments.of((Function<FinremCaseData.FinremCaseDataBuilder, FinremCaseData.FinremCaseDataBuilder>) f -> f,
                uploadAgreedDraftOrderModified(),
                (Function<FinremCaseData, ?>) data -> nullSafeUploadAgreedDraftOrder(data).getUploadAgreedDraftOrderCollection(),
                List.of(
                    caseDocument("http://fakeurl/draftOrder", "draftOrder.pdf"),
                    caseDocument("http://fakeurl/attachment1", "attachment1.pdf"),
                    caseDocument("http://fakeurl/attachment2", "attachment2.pdf")
                )
            ),
            // 3.1 uploadAgreedDraftOrder.pensionSharingAnnex
            Arguments.of((Function<FinremCaseData.FinremCaseDataBuilder, FinremCaseData.FinremCaseDataBuilder>) f -> f,
                uploadAgreedDraftOrderModified(),
                (Function<FinremCaseData, ?>) data -> nullSafeUploadAgreedDraftOrder(data).getAgreedPsaCollection(),
                List.of(
                    caseDocument("http://fakeurl/pensionSharingAnnex", "pensionSharingAnnex.pdf")
                )
            )
        );
    }

    @ParameterizedTest
    @MethodSource("provideArguments")
    void givenCaseWithExistingDocument_whenDocumentUploadedOrNot_thenReturnExpectedDocument(
        Function<FinremCaseData.FinremCaseDataBuilder, FinremCaseData.FinremCaseDataBuilder> caseDataBeforeModifier,
        Function<FinremCaseData.FinremCaseDataBuilder, FinremCaseData.FinremCaseDataBuilder> caseDataModifier,
        Function<FinremCaseData, List<UploadingDocumentsHolder<?>>> getDocumentsFromCaseData,
        List<CaseDocument> expectedReturn) {
        FinremCaseData caseDataBefore = caseDataBeforeModifier.apply(FinremCaseData.builder()).build();
        FinremCaseData caseData = caseDataModifier.apply(FinremCaseData.builder()).build();
        assertEquals(expectedReturn, underTest.getNewUploadDocuments(caseData, caseDataBefore, getDocumentsFromCaseData));
    }

    private static UploadAgreedDraftOrder nullSafeUploadAgreedDraftOrder(FinremCaseData data) {
        return ofNullable(data.getDraftOrdersWrapper().getUploadAgreedDraftOrder())
            .orElse(UploadAgreedDraftOrder.builder().build());
    }

    private static Function<FinremCaseData.FinremCaseDataBuilder, FinremCaseData.FinremCaseDataBuilder> uploadAgreedDraftOrderModified() {
        return  finremCaseDataBuilder -> {
            finremCaseDataBuilder.draftOrdersWrapper(DraftOrdersWrapper.builder()
                .uploadAgreedDraftOrder(UploadAgreedDraftOrder.builder()
                    .agreedPsaCollection(List.of(
                        AgreedPensionSharingAnnexCollection.builder()
                            .value(AgreedPensionSharingAnnex.builder()
                                .agreedPensionSharingAnnexes(caseDocument("http://fakeurl/pensionSharingAnnex", "pensionSharingAnnex.pdf"))
                                .build())
                            .build()
                    ))
                    .uploadAgreedDraftOrderCollection(List.of(
                        UploadAgreedDraftOrderCollection.builder()
                            .value(UploadedDraftOrder.builder()
                                .agreedDraftOrderDocument(caseDocument("http://fakeurl/draftOrder", "draftOrder.pdf"))
                                .agreedDraftOrderAdditionalDocumentsCollection(List.of(
                                    DocumentCollection.builder().value(caseDocument("http://fakeurl/attachment1", "attachment1.pdf")).build(),
                                    DocumentCollection.builder().value(caseDocument("http://fakeurl/attachment2", "attachment2.pdf")).build()
                                ))
                                .build())
                            .build()
                    ))
                    .build()
                ).build()
            );
            return finremCaseDataBuilder;
        };
    }
}
