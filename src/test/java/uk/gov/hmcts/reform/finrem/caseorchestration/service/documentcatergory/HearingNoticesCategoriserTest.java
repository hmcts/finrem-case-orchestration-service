package uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AdditionalHearingDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AdditionalHearingDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ListForHearingWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HearingNoticesCategoriserTest {

    HearingNoticesCategoriser hearingNoticesCategoriser;
    FeatureToggleService featureToggleService;

    @BeforeEach
    void setUp() {
        featureToggleService = mock(FeatureToggleService.class);
        when(featureToggleService.isCaseFileViewEnabled()).thenReturn(true);
        hearingNoticesCategoriser = new HearingNoticesCategoriser(featureToggleService);
    }

    @Test
    void testCategorise() {
        FinremCaseData finremCaseData = getFinremCaseData();
        hearingNoticesCategoriser.categorise(finremCaseData);
        List<DocumentCollection> hearingNoticeDocuments = finremCaseData.getHearingNoticeDocumentPack();

        assertThat(hearingNoticeDocuments.get(0).getValue().getCategoryId()).isEqualTo(
            DocumentCategory.HEARING_NOTICES.getDocumentCategoryId()
        );

        assertThat(hearingNoticeDocuments.get(1).getValue().getCategoryId()).isEqualTo(
            DocumentCategory.SYSTEM_DUPLICATES.getDocumentCategoryId()
        );
    }

    private FinremCaseData getFinremCaseData() {
        CaseDocument additionalHearingDocument = getAdditionalHearingDocument();
        CaseDocument hearingNoticeDocument = CaseDocument.builder().documentFilename("InterimHearingNotice.pdf")
            .documentUrl("http://dm-store/documents/b067a2dd-657a-4ed2-98c3-9c3159d1482e")
            .documentBinaryUrl("http://dm-store/documents/b067a2dd-657a-4ed2-98c3-9c3159d1482e/binary").build();

        List<DocumentCollection> hearingNoticeDocumentPack = List.of(
            DocumentCollection.builder()
                .value(hearingNoticeDocument)
                .build(),
            DocumentCollection.builder()
                .value(getAdditionalHearingDocument())
                .build()
        );

        List<AdditionalHearingDocumentCollection> additionalHearingDocuments = List.of(
            AdditionalHearingDocumentCollection.builder()
                .value(
                    AdditionalHearingDocument.builder()
                        .document(additionalHearingDocument)
                        .build()
                )

                .build()
        );

        return FinremCaseData.builder()
            .hearingNoticeDocumentPack(hearingNoticeDocumentPack)
            .listForHearingWrapper(ListForHearingWrapper.builder()
                .additionalHearingDocuments(additionalHearingDocuments)
                .build())
            .build();
    }

    private CaseDocument getAdditionalHearingDocument() {
        return CaseDocument.builder()
            .documentFilename("AdditionalHearingDocument.pdf")
            .documentUrl("http://dm-store/documents/b067a2dd-657a-4ed2-98c3-9c3159d1482e")
            .documentBinaryUrl("http://dm-store/documents/b067a2dd-657a-4ed2-98c3-9c3159d1482e/binary")
            .build();
    }
}
