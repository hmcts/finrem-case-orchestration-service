package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;


@RunWith(MockitoJUnitRunner.class)
public class ConfidentialDocumentsHandlerTest extends BaseManageDocumentsHandlerTest {

    @InjectMocks
    ConfidentialDocumentsHandler handler;

    @Test
    public void givenAddedDocOnScreenCollectionWhenAddNewOrMovedDocumentToCollectionThenAddScreenDocsToCollectionType() {
        screenUploadDocumentList.add(createContestedUploadDocumentItem(CaseDocumentType.OTHER,
            CaseDocumentParty.RESPONDENT, YesOrNo.YES, YesOrNo.YES, "Other Example"));

        caseDetails.getData().setManageCaseDocumentCollection(screenUploadDocumentList);

        handler.addManagedDocumentToSelectedCollection(
            FinremCallbackRequest.builder().caseDetails(caseDetails).caseDetailsBefore(caseDetails).build(),
            screenUploadDocumentList);

        assertThat(caseData.getUploadCaseDocumentWrapper()
                .getDocumentCollectionPerType(CaseDocumentCollectionType.CONFIDENTIAL_DOCS_COLLECTION),
            hasSize(1));
        assertThat(caseData.getManageCaseDocumentCollection(),
            hasSize(0));
    }

    @Test
    public void givenRemovedDocFromScreenCollectionWhenDeleteRemovedDocumentFromCollectionThenRemoveScreenDocsFromCollectionType() {
        List<UploadCaseDocumentCollection> beforeEventDocList = new ArrayList<>();
        UploadCaseDocumentCollection removedDoc = createContestedUploadDocumentItem(CaseDocumentType.OTHER,
            CaseDocumentParty.APPLICANT, YesOrNo.YES, YesOrNo.YES, "Other Example");
        beforeEventDocList.add(removedDoc);
        caseData.getUploadCaseDocumentWrapper()
            .getDocumentCollectionPerType(CaseDocumentCollectionType.CONTESTED_FDR_CASE_DOCUMENT_COLLECTION)
            .addAll(beforeEventDocList);
        caseDetailsBefore.getData().getUploadCaseDocumentWrapper()
            .getDocumentCollectionPerType(CaseDocumentCollectionType.CONTESTED_FDR_CASE_DOCUMENT_COLLECTION)
            .addAll(beforeEventDocList);
        screenUploadDocumentList.addAll(beforeEventDocList);
        screenUploadDocumentList.remove(removedDoc);

        caseDetails.getData().setManageCaseDocumentCollection(screenUploadDocumentList);

        handler.removeManagedDocumentFromOriginalCollection(
            FinremCallbackRequest.builder().caseDetails(caseDetails).caseDetailsBefore(caseDetailsBefore).build()
        );

        assertThat(caseData.getUploadCaseDocumentWrapper()
                .getDocumentCollectionPerType(CaseDocumentCollectionType.CONFIDENTIAL_DOCS_COLLECTION),
            hasSize(0));
    }
}