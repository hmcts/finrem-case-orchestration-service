package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ConfidentialUploadedDocumentData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadConfidentialDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;

@RunWith(MockitoJUnitRunner.class)
public class LegacyConfidentialDocumentsServiceTest {

    public static final String LEGACY_CONFIDENTIAL_ID = "LEGACY_CONFIDENTIAL_ID";
    public static final String LEGACY_CONFIDENTIAL_COMMENT = "LEGACY_CONFIDENTIAL_COMMENT";
    public static final String LEGACY_CONFIDENTIAL_FILENAME = "LEGACY_CONFIDENTIAL_FILENAME";
    LegacyConfidentialDocumentsService service = new LegacyConfidentialDocumentsService();

    @Test
    public void givenLegacyConfidentialDocument_WhenGetUploadCaseDocumentCollection_ThenReturnCorrectUploadCaseDocument() {

        LocalDateTime now = LocalDateTime.now();
        ConfidentialUploadedDocumentData legacyConfidentialDocumentCollection =
            ConfidentialUploadedDocumentData.builder()
                .id(LEGACY_CONFIDENTIAL_ID)
                .value(UploadConfidentialDocument.builder()
                    .documentComment(LEGACY_CONFIDENTIAL_COMMENT)
                    .confidentialDocumentUploadDateTime(now)
                    .documentLink(CaseDocument.builder().documentFilename(LEGACY_CONFIDENTIAL_FILENAME).build())
                    .documentType(CaseDocumentType.CARE_PLAN)
                    .build())
                .build();

        List<UploadCaseDocumentCollection> uploadCaseDocumentCollections =
            service.mapLegacyConfidentialDocumentToConfidentialDocumentCollection(List.of(legacyConfidentialDocumentCollection));

        assertThat(uploadCaseDocumentCollections.get(0).getId(), equalTo(LEGACY_CONFIDENTIAL_ID));
        assertThat(uploadCaseDocumentCollections.get(0).getUploadCaseDocument().getHearingDetails(),
            equalTo(LEGACY_CONFIDENTIAL_COMMENT));
        assertThat(uploadCaseDocumentCollections.get(0).getUploadCaseDocument().getCaseDocumentConfidential(),
            is(YesOrNo.YES));
        assertThat(uploadCaseDocumentCollections.get(0).getUploadCaseDocument().getCaseDocumentType(),
            is(CaseDocumentType.CARE_PLAN));
        assertThat(uploadCaseDocumentCollections.get(0).getUploadCaseDocument().getCaseDocuments().getDocumentFilename(),
            equalTo(LEGACY_CONFIDENTIAL_FILENAME));
    }
}