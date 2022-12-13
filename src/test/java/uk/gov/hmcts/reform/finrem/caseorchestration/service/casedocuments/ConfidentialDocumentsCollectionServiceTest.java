package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadConfidentialDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadConfidentialDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.UploadCaseDocumentWrapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

@RunWith(MockitoJUnitRunner.class)
public class ConfidentialDocumentsCollectionServiceTest {

    public static final String AUTH_TOKEN = "AuthTokien";
    private final List<UploadCaseDocumentCollection> screenUploadDocumentList = new ArrayList<>();
    ConfidentialDocumentsCollectionService collectionService =
        new ConfidentialDocumentsCollectionService(null);
    private FinremCaseDetails caseDetails;
    private FinremCaseDetails caseDetailsBefore;
    private FinremCaseData caseData;

    @Before
    public void setUp() {
        caseDetails = buildCaseDetails();
        caseDetailsBefore = buildCaseDetails();
        caseData = caseDetails.getData();
    }

    @Test
    public void givenAddedDocOnScreenCollectionWhenAddNewOrMovedDocumentToCollectionThenAddScreenDocsToCollectionType() {
        screenUploadDocumentList.add(createContestedUploadDocumentItem(CaseDocumentType.OTHER,
            CaseDocumentParty.RESPONDENT, YesOrNo.YES, YesOrNo.NO, "Other Example"));

        caseDetails.getData().setManageCaseDocumentCollection(screenUploadDocumentList);

        collectionService.addManagedDocumentToCollection(
            FinremCallbackRequest.builder().caseDetails(caseDetails).caseDetailsBefore(caseDetails).build(),
            screenUploadDocumentList);

        assertThat(caseData.getConfidentialDocumentsUploaded(), hasSize(1));
        assertThat(caseData.getManageCaseDocumentCollection(),
            hasSize(0));
    }

    @Test
    public void shouldNotAddConfidentialDocumentsFiltered() {

        List<UploadConfidentialDocumentCollection> confidentialUploadedDocumentData = new ArrayList<>();
        confidentialUploadedDocumentData.add(createConfidentialUploadedDocumentDataItem());
        caseDetails.getData().setConfidentialDocumentsUploaded(confidentialUploadedDocumentData);
        caseDetails.getData().getUploadCaseDocumentWrapper().setUploadCaseDocument(screenUploadDocumentList);

        collectionService.addManagedDocumentToCollection(
            FinremCallbackRequest.builder().caseDetails(caseDetails).caseDetailsBefore(caseDetails).build(),
            screenUploadDocumentList);

        assertThat(caseData.getConfidentialDocumentsUploaded(), hasSize(1));
    }

    protected UploadConfidentialDocumentCollection createConfidentialUploadedDocumentDataItem() {
        return UploadConfidentialDocumentCollection.builder().value(
            (UploadConfidentialDocument
                .builder()
                .documentType(CaseDocumentType.OTHER)
                .documentLink(CaseDocument.builder().documentUrl("url").documentFilename("filename").build())
                .documentComment("Comment")
                .build())).build();
    }

    protected UploadCaseDocumentCollection createContestedUploadDocumentItem(CaseDocumentType type,
                                                                             CaseDocumentParty party,
                                                                             YesOrNo isConfidential,
                                                                             YesOrNo isFdr,
                                                                             String other) {
        UUID uuid = UUID.randomUUID();

        return UploadCaseDocumentCollection.builder()
            .id(uuid.toString())
            .uploadCaseDocument(UploadCaseDocument
                .builder()
                .caseDocuments(new CaseDocument())
                .caseDocumentType(type)
                .caseDocumentParty(party)
                .caseDocumentConfidential(isConfidential)
                .caseDocumentOther(other)
                .caseDocumentFdr(isFdr)
                .hearingDetails(null)
                .caseDocumentUploadDateTime(LocalDateTime.now())
                .build())
            .build();
    }

    protected FinremCaseDetails buildCaseDetails() {
        FinremCaseData finremCaseData = FinremCaseData.builder()
            .uploadCaseDocumentWrapper(UploadCaseDocumentWrapper.builder().build())
            .build();
        return FinremCaseDetails.builder().id(Long.valueOf(123)).caseType(CaseType.CONTESTED)
            .data(finremCaseData).build();
    }
}