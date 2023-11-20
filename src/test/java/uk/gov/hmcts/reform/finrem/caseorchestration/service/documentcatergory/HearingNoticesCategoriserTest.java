package uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AdditionalHearingDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AdditionalHearingDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HearingNoticesCategoriserTest {

    HearingNoticesCategoriser hearingNoticesCategoriser;
    FeatureToggleService featureToggleService;

    @Before
    public void setUpTest() {
        featureToggleService = mock(FeatureToggleService.class);
        when(featureToggleService.isCaseFileViewEnabled()).thenReturn(true);
        hearingNoticesCategoriser = new HearingNoticesCategoriser(featureToggleService);
    }

    @Test
    public void categoriseDocuments() {
        FinremCaseData finremCaseData = getFinremCaseData();
        hearingNoticesCategoriser.categorise(finremCaseData);

        assert finremCaseData.getHearingNoticeDocumentPack().get(0).getValue().getCategoryId().equals(
            DocumentCategory.HEARING_NOTICES.getDocumentCategoryId()
        );

        assert finremCaseData.getHearingNoticeDocumentPack().get(1).getValue().getCategoryId().equals(
            DocumentCategory.SYSTEM_DUPLICATES.getDocumentCategoryId()
        );

        assert finremCaseData.getAdditionalHearingDocuments().get(0).getValue().getDocument().getCategoryId().equals(
            DocumentCategory.HEARING_NOTICES.getDocumentCategoryId()
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
            .additionalHearingDocuments(additionalHearingDocuments)
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
