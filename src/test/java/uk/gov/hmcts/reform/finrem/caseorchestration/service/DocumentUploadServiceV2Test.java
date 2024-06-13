package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadGeneralDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadGeneralDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.util.TestResource;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
class DocumentUploadServiceV2Test extends BaseServiceTest {

    @Autowired
    private DocumentUploadServiceV2 documentUploadService;

    private static Stream<Arguments> provideArguments() {
        final List<UploadGeneralDocumentCollection> existingUploadGeneralDocument = List.of(
            UploadGeneralDocumentCollection.builder()
                .value(UploadGeneralDocument.builder()
                    .documentLink(TestResource.buildCaseDocument("url1", "binaryUrl1", "filename1"))
                    .build())
                .build()
        );
        final List<UploadDocumentCollection> existingUploadDocument = List.of(
            UploadDocumentCollection.builder()
                .value(UploadDocument.builder()
                    .documentLink(TestResource.buildCaseDocument("url1", "binaryUrl1", "filename1"))
                    .build())
                .build()
        );

        return Stream.of(
            // 1. uploadGeneralDocuments
            // 1.1 with new doc
            Arguments.of((Function<FinremCaseData.FinremCaseDataBuilder, FinremCaseData.FinremCaseDataBuilder>) finremCaseDataBuilder -> {
                    finremCaseDataBuilder.uploadGeneralDocuments(existingUploadGeneralDocument);
                    return finremCaseDataBuilder;
                },
                (Function<FinremCaseData.FinremCaseDataBuilder, FinremCaseData.FinremCaseDataBuilder>) finremCaseDataBuilder -> {
                    finremCaseDataBuilder.uploadGeneralDocuments(Stream.concat(existingUploadGeneralDocument.stream(), Stream.of(
                        UploadGeneralDocumentCollection.builder()
                            .value(UploadGeneralDocument.builder()
                                .documentLink(TestResource.buildCaseDocument("newUrl1", "newBinaryUrl1", "newFilename1"))
                                .build())
                            .build()
                    )).toList());
                    return finremCaseDataBuilder;
                },
                (Function<FinremCaseData, ?>) FinremCaseData::getUploadGeneralDocuments,
                List.of(UploadGeneralDocumentCollection.builder()
                    .value(UploadGeneralDocument.builder()
                        .documentLink(TestResource.buildCaseDocument("newUrl1", "newBinaryUrl1", "newFilename1"))
                        .build())
                    .build())
            ),
            // 1.2 without new doc
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
            // 1.3 with multiple new docs
            Arguments.of((Function<FinremCaseData.FinremCaseDataBuilder, FinremCaseData.FinremCaseDataBuilder>) finremCaseDataBuilder -> {
                    finremCaseDataBuilder.uploadGeneralDocuments(existingUploadGeneralDocument);
                    return finremCaseDataBuilder;
                },
                (Function<FinremCaseData.FinremCaseDataBuilder, FinremCaseData.FinremCaseDataBuilder>) finremCaseDataBuilder -> {
                    finremCaseDataBuilder.uploadGeneralDocuments(Stream.concat(existingUploadGeneralDocument.stream(), Stream.of(
                        UploadGeneralDocumentCollection.builder()
                            .value(UploadGeneralDocument.builder()
                                .documentLink(TestResource.buildCaseDocument("newUrl1", "newBinaryUrl1", "newFilename1"))
                                .build())
                            .build(),

                        UploadGeneralDocumentCollection.builder()
                            .value(UploadGeneralDocument.builder()
                                .documentLink(TestResource.buildCaseDocument("newUrl2", "newBinaryUrl2", "newFilename2"))
                                .build())
                            .build()
                    )).toList());
                    return finremCaseDataBuilder;
                },
                (Function<FinremCaseData, ?>) FinremCaseData::getUploadGeneralDocuments,
                List.of(UploadGeneralDocumentCollection.builder()
                    .value(UploadGeneralDocument.builder()
                        .documentLink(TestResource.buildCaseDocument("newUrl1", "newBinaryUrl1", "newFilename1"))
                        .build())
                    .build(),
                    UploadGeneralDocumentCollection.builder()
                        .value(UploadGeneralDocument.builder()
                            .documentLink(TestResource.buildCaseDocument("newUrl2", "newBinaryUrl2", "newFilename2"))
                            .build())
                        .build())
            ),
            // 1.4 no existing doc with a new doc
            Arguments.of((Function<FinremCaseData.FinremCaseDataBuilder, FinremCaseData.FinremCaseDataBuilder>) finremCaseDataBuilder ->
                    finremCaseDataBuilder,
                (Function<FinremCaseData.FinremCaseDataBuilder, FinremCaseData.FinremCaseDataBuilder>) finremCaseDataBuilder -> {
                    finremCaseDataBuilder.uploadGeneralDocuments(List.of(
                        UploadGeneralDocumentCollection.builder()
                            .value(UploadGeneralDocument.builder()
                                .documentLink(TestResource.buildCaseDocument("newUrl1", "newBinaryUrl1", "newFilename1"))
                                .build())
                            .build()
                    ));
                    return finremCaseDataBuilder;
                },
                (Function<FinremCaseData, ?>) FinremCaseData::getUploadGeneralDocuments,
                List.of(
                    UploadGeneralDocumentCollection.builder()
                        .value(UploadGeneralDocument.builder()
                            .documentLink(TestResource.buildCaseDocument("newUrl1", "newBinaryUrl1", "newFilename1"))
                            .build())
                        .build()
                )
            ),

            // 2. uploadDocuments
            Arguments.of((Function<FinremCaseData.FinremCaseDataBuilder, FinremCaseData.FinremCaseDataBuilder>) finremCaseDataBuilder -> {
                    finremCaseDataBuilder.uploadDocuments(existingUploadDocument);
                    return finremCaseDataBuilder;
                },
                (Function<FinremCaseData.FinremCaseDataBuilder, FinremCaseData.FinremCaseDataBuilder>) finremCaseDataBuilder -> {
                    finremCaseDataBuilder.uploadDocuments(Stream.concat(existingUploadDocument.stream(), Stream.of(
                        UploadDocumentCollection.builder()
                            .value(UploadDocument.builder()
                                .documentLink(TestResource.buildCaseDocument("newUrl", "newBinaryUrl1", "newFilename"))
                                .build())
                            .build()
                    )).toList());
                    return finremCaseDataBuilder;
                },
                (Function<FinremCaseData, ?>) FinremCaseData::getUploadDocuments,
                List.of(UploadDocumentCollection.builder()
                    .value(UploadDocument.builder()
                        .documentLink(TestResource.buildCaseDocument("newUrl", "newBinaryUrl1", "newFilename"))
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
        FinremCaseData caseData = caseDataModifier.apply(caseDataBefore.toBuilder()).build();
        assertEquals(expectedReturn, documentUploadService.getNewUploadDocuments(caseData, caseDataBefore, accessor));
    }
}
