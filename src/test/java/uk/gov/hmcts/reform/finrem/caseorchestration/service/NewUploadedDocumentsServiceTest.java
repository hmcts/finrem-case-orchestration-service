package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadGeneralDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadGeneralDocumentCollection;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

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
                List.of(UploadGeneralDocumentCollection.builder()
                    .value(UploadGeneralDocument.builder()
                        .documentLink(caseDocument("newUrl1", "newBinaryUrl1", "newFilename1"))
                        .build())
                    .build())
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
                List.of(UploadGeneralDocumentCollection.builder()
                    .value(UploadGeneralDocument.builder()
                        .documentLink(caseDocument("newUrl1", "newBinaryUrl1", "newFilename1"))
                        .build())
                    .build())
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
                List.of(UploadGeneralDocumentCollection.builder()
                    .value(UploadGeneralDocument.builder()
                        .documentLink(caseDocument("newUrl1", "newBinaryUrl1", "newFilename1"))
                        .build())
                    .build())
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
                List.of(UploadGeneralDocumentCollection.builder()
                    .value(UploadGeneralDocument.builder()
                        .documentLink(caseDocument("newUrl1", "newBinaryUrl1", "newFilename1"))
                        .build())
                    .build(),
                    UploadGeneralDocumentCollection.builder()
                        .value(UploadGeneralDocument.builder()
                            .documentLink(caseDocument("newUrl2", "newBinaryUrl2", "newFilename2"))
                            .build())
                        .build())
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
                List.of(
                    UploadGeneralDocumentCollection.builder()
                        .value(UploadGeneralDocument.builder()
                            .documentLink(caseDocument("newUrl1", "newBinaryUrl1", "newFilename1"))
                            .build())
                        .build()
                )
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
                List.of(UploadDocumentCollection.builder()
                    .value(UploadDocument.builder()
                        .documentLink(caseDocument("newUrl", "newBinaryUrl1", "newFilename"))
                        .build())
                    .build())
            )
        );
    }

    @ParameterizedTest
    @MethodSource("provideArguments")
    void givenCaseWithExistingDocument_whenDocumentUploadedOrNot_thenReturnExpectedDocument(
        Function<FinremCaseData.FinremCaseDataBuilder, FinremCaseData.FinremCaseDataBuilder> caseDataBeforeModifier,
        Function<FinremCaseData.FinremCaseDataBuilder, FinremCaseData.FinremCaseDataBuilder> caseDataModifier,
        Function<FinremCaseData, List<CaseDocumentCollection<?>>> accessor,
        List<CaseDocumentCollection<?>> expectedReturn) {
        FinremCaseData caseDataBefore = caseDataBeforeModifier.apply(FinremCaseData.builder()).build();
        FinremCaseData caseData = caseDataModifier.apply(FinremCaseData.builder()).build();
        assertEquals(expectedReturn, underTest.getNewUploadDocuments(caseData, caseDataBefore, accessor));
    }
}
