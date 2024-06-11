package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.UploadedGeneralDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadGeneralDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadGeneralDocumentCollection;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;

public class UploadedGeneralDocumentServiceTest extends BaseServiceTest {

    @Autowired
    private UploadedGeneralDocumentService uploadedGeneralDocumentService;

    @Test
    public void givenContestedCaseWithExistingGeneralDocument_whenNewGeneralDocumentUploaded_thenReturnNewGeneralDocument() {
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
    public void givenContestedCaseWithoutExistingGeneralDocument_whenNewGeneralDocumentUploaded_thenReturnNewGeneralDocument() {
        FinremCaseData caseDataBefore = FinremCaseData.builder()
            .uploadGeneralDocuments(List.of())
            .build();
        FinremCaseData caseData = caseDataBefore.toBuilder()
            .uploadGeneralDocuments(List.of(
                UploadGeneralDocumentCollection.builder()
                    .value(UploadGeneralDocument.builder()
                        .documentLink(buildCaseDocument("newUrl", "newBinaryUrl1", "newFilename"))
                        .build())
                    .build()
            ))
            .build();

        List<UploadGeneralDocumentCollection> actual = uploadedGeneralDocumentService.getNewlyUploadedDocuments(caseData, caseDataBefore);
        List<UploadGeneralDocumentCollection> expected = List.of(UploadGeneralDocumentCollection.builder()
            .value(UploadGeneralDocument.builder()
                .documentLink(buildCaseDocument("newUrl", "newBinaryUrl1", "newFilename"))
                .build())
            .build());

        assertEquals(expected, actual);
    }
}