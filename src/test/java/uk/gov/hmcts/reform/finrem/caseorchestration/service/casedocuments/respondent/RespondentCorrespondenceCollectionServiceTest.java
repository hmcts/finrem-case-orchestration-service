package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.respondent;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ManageCaseDocumentsCollectionType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.CaseDocumentCollectionsServiceTest;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

@RunWith(MockitoJUnitRunner.class)
public class RespondentCorrespondenceCollectionServiceTest extends CaseDocumentCollectionsServiceTest {

    @InjectMocks
    RespondentCorrespondenceCollectionService collectionService;

    @Test
    public void givenAddedDocOnScreenCollectionWhenAddNewOrMovedDocumentToCollectionThenAddScreenDocsToCollectionType() {
        screenUploadDocumentList.add(createContestedUploadDocumentItem(CaseDocumentType.OFFERS,
            CaseDocumentParty.RESPONDENT, YesOrNo.NO, YesOrNo.NO, null));
        screenUploadDocumentList.add(createContestedUploadDocumentItem(CaseDocumentType.LETTER_FROM_APPLICANT,
            CaseDocumentParty.RESPONDENT, YesOrNo.NO, YesOrNo.NO, null));

        caseDetails.getData().setManageCaseDocumentCollection(screenUploadDocumentList);

        collectionService.addManagedDocumentToCollection(
            FinremCallbackRequest.builder().caseDetails(caseDetails).caseDetailsBefore(caseDetails).build(),
            screenUploadDocumentList);

        assertThat(caseData.getUploadCaseDocumentWrapper()
                .getDocumentCollection(ManageCaseDocumentsCollectionType.RESP_CORRESPONDENCE_COLLECTION),
            hasSize(2));
        assertThat(caseData.getManageCaseDocumentCollection(),
            hasSize(0));
    }

    @Test
    public void givenRemovedDocFromScreenCollectionWhenDeleteRemovedDocumentFromCollectionThenRemoveScreenDocsFromCollectionType() {
        List<UploadCaseDocumentCollection> beforeEventDocList = new ArrayList<>();
        UploadCaseDocumentCollection removedDoc = createContestedUploadDocumentItem(CaseDocumentType.OFFERS,
            CaseDocumentParty.RESPONDENT, YesOrNo.NO, YesOrNo.NO, null);
        beforeEventDocList.add(removedDoc);
        beforeEventDocList.add(createContestedUploadDocumentItem(CaseDocumentType.LETTER_FROM_APPLICANT,
            CaseDocumentParty.RESPONDENT, YesOrNo.NO, YesOrNo.NO, null));
        caseData.getUploadCaseDocumentWrapper()
            .getDocumentCollection(ManageCaseDocumentsCollectionType.RESP_CORRESPONDENCE_COLLECTION)
            .addAll(beforeEventDocList);
        caseDetailsBefore.getData().getUploadCaseDocumentWrapper()
            .getDocumentCollection(ManageCaseDocumentsCollectionType.RESP_CORRESPONDENCE_COLLECTION)
            .addAll(beforeEventDocList);
        screenUploadDocumentList.addAll(beforeEventDocList);
        screenUploadDocumentList.remove(removedDoc);

        caseDetails.getData().setManageCaseDocumentCollection(screenUploadDocumentList);

        collectionService.deleteRemovedDocumentFromAllPlaces(
            FinremCallbackRequest.builder().caseDetails(caseDetails).caseDetailsBefore(caseDetailsBefore).build(),
            AUTH_TOKEN);

        assertThat(caseData.getUploadCaseDocumentWrapper()
                .getDocumentCollection(ManageCaseDocumentsCollectionType.RESP_CORRESPONDENCE_COLLECTION),
            hasSize(1));
    }
}