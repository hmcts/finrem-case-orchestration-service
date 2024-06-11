package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.UploadedGeneralDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadGeneralDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadGeneralDocumentCollection;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;

@ExtendWith(SpringExtension.class)
class UploadedGeneralDocumentServiceTest extends BaseServiceTest {

    @Autowired
    private UploadedGeneralDocumentService uploadedGeneralDocumentService;

    @Test
    void givenContestedCaseWithExistingGeneralDocument_whenNewGeneralDocumentUploaded_thenReturnNewGeneralDocument() {
        final List<UploadGeneralDocumentCollection> existingGeneralDocument = List.of(
            UploadGeneralDocumentCollection.builder()
                .value(UploadGeneralDocument.builder()
                    .documentLink(buildCaseDocument("url1", "binaryUrl1", "filename1"))
                    .build())
                .build()
        );

        FinremCaseData caseDataBefore = FinremCaseData.builder()
            .uploadGeneralDocuments(existingGeneralDocument)
            .build();
        FinremCaseData caseData = caseDataBefore.toBuilder()
            .uploadGeneralDocuments(Stream.concat(existingGeneralDocument.stream(), Stream.of(
                UploadGeneralDocumentCollection.builder()
                    .value(UploadGeneralDocument.builder()
                        .documentLink(buildCaseDocument("newUrl", "newBinaryUrl1", "newFilename"))
                        .build())
                    .build()
            )).toList())
            .build();

        List<UploadGeneralDocumentCollection> actual = uploadedGeneralDocumentService.getNewlyUploadedDocuments(caseData, caseDataBefore);
        List<UploadGeneralDocumentCollection> expected = List.of(UploadGeneralDocumentCollection.builder()
            .value(UploadGeneralDocument.builder()
                .documentLink(buildCaseDocument("newUrl", "newBinaryUrl1", "newFilename"))
                .build())
            .build());

        assertEquals(expected, actual);
    }

    @Test
    void givenContestedCaseWithExistingGeneralDocument_whenNoNewGeneralDocumentUploaded_thenReturnAnEmptyList() {
        final List<UploadGeneralDocumentCollection> existingGeneralDocument = List.of(
            UploadGeneralDocumentCollection.builder()
                .value(UploadGeneralDocument.builder()
                    .documentLink(buildCaseDocument("url1", "binaryUrl1", "filename1"))
                    .build())
                .build()
        );

        FinremCaseData caseDataBefore = FinremCaseData.builder()
            .uploadGeneralDocuments(existingGeneralDocument)
            .build();
        FinremCaseData caseData = caseDataBefore.toBuilder()
            .uploadGeneralDocuments(existingGeneralDocument)
            .build();

        List<UploadGeneralDocumentCollection> actual = uploadedGeneralDocumentService.getNewlyUploadedDocuments(caseData, caseDataBefore);
        List<UploadGeneralDocumentCollection> expected = List.of();

        assertEquals(expected, actual);
    }

    @ParameterizedTest
    @NullAndEmptySource
    void givenContestedCaseWithoutExistingGeneralDocument_whenNewGeneralDocumentUploaded_thenReturnNewGeneralDocument(
        List<UploadGeneralDocumentCollection> existing) {
        FinremCaseData caseDataBefore = FinremCaseData.builder()
            .uploadGeneralDocuments(existing)
            .build();
        FinremCaseData caseData = caseDataBefore.toBuilder()
            .uploadGeneralDocuments(List.of(
                UploadGeneralDocumentCollection.builder()
                    .value(UploadGeneralDocument.builder()
                        .documentLink(buildCaseDocument("newUrl1", "newBinaryUrl1", "newFilename1"))
                        .build())
                    .build()
            ))
            .build();

        List<UploadGeneralDocumentCollection> actual = uploadedGeneralDocumentService.getNewlyUploadedDocuments(caseData, caseDataBefore);
        List<UploadGeneralDocumentCollection> expected = List.of(UploadGeneralDocumentCollection.builder()
            .value(UploadGeneralDocument.builder()
                .documentLink(buildCaseDocument("newUrl1", "newBinaryUrl1", "newFilename1"))
                .build())
            .build());

        assertEquals(expected, actual);
    }

    @ParameterizedTest
    @NullAndEmptySource
    void givenContestedCaseWithoutExistingGeneralDocument_whenNewGeneralDocumentsUploaded_thenReturnMultipleNewGeneralDocument(
        List<UploadGeneralDocumentCollection> existing) {
        FinremCaseData caseDataBefore = FinremCaseData.builder()
            .uploadGeneralDocuments(existing)
            .build();
        FinremCaseData caseData = caseDataBefore.toBuilder()
            .uploadGeneralDocuments(List.of(
                UploadGeneralDocumentCollection.builder()
                    .value(UploadGeneralDocument.builder()
                        .documentLink(buildCaseDocument("newUrl1", "newBinaryUrl1", "newFilename1"))
                        .build())
                    .build(),
                UploadGeneralDocumentCollection.builder()
                    .value(UploadGeneralDocument.builder()
                        .documentLink(buildCaseDocument("newUrl2", "newBinaryUrl2", "newFilename2"))
                        .build())
                    .build()
            ))
            .build();

        List<UploadGeneralDocumentCollection> actual = uploadedGeneralDocumentService.getNewlyUploadedDocuments(caseData, caseDataBefore);
        List<UploadGeneralDocumentCollection> expected = List.of(
            UploadGeneralDocumentCollection.builder()
                .value(UploadGeneralDocument.builder()
                    .documentLink(buildCaseDocument("newUrl1", "newBinaryUrl1", "newFilename1"))
                    .build())
                .build(),
            UploadGeneralDocumentCollection.builder()
                .value(UploadGeneralDocument.builder()
                    .documentLink(buildCaseDocument("newUrl2", "newBinaryUrl2", "newFilename2"))
                    .build())
                .build());

        assertEquals(expected, actual);
    }
}
