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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.BaseManageDocumentsHandlerTest;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

@RunWith(MockitoJUnitRunner.class)
public class RespondentStatementsExhibitsCollectionServiceTest extends BaseManageDocumentsHandlerTest {

    @InjectMocks
    RespondentStatementsExhibitsHandler collectionService;

    @Test
    public void givenAddedDocOnScreenCollectionWhenAddNewOrMovedDocumentToCollectionThenAddScreenDocsToCollectionType() {
        screenUploadDocumentList.add(createContestedUploadDocumentItem(CaseDocumentType.STATEMENT_AFFIDAVIT,
            CaseDocumentParty.RESPONDENT, YesOrNo.NO, YesOrNo.NO, null));
        screenUploadDocumentList.add(createContestedUploadDocumentItem(CaseDocumentType.WITNESS_STATEMENT_AFFIDAVIT,
            CaseDocumentParty.RESPONDENT, YesOrNo.NO, YesOrNo.NO, null));


        caseDetails.getData().setManageCaseDocumentCollection(screenUploadDocumentList);

        collectionService.addManagedDocumentToSelectedCollection(
            FinremCallbackRequest.builder().caseDetails(caseDetails).caseDetailsBefore(caseDetails).build(),
            screenUploadDocumentList);

        assertThat(caseDetails.getData().getUploadCaseDocumentWrapper().getRespStatementsExhibitsCollection(),
            hasSize(2));
        assertThat(caseData.getUploadCaseDocumentWrapper()
                .getDocumentCollectionPerType(CaseDocumentCollectionType.RESP_STATEMENTS_EXHIBITS_COLLECTION),
            hasSize(2));
        assertThat(caseData.getManageCaseDocumentCollection(),
            hasSize(0));
    }

    @Test
    public void givenRemovedDocFromScreenCollectionWhenDeleteRemovedDocumentFromCollectionThenRemoveScreenDocsFromCollectionType() {
        List<UploadCaseDocumentCollection> beforeEventDocList = new ArrayList<>();
        UploadCaseDocumentCollection removedDoc = createContestedUploadDocumentItem(CaseDocumentType.STATEMENT_AFFIDAVIT,
            CaseDocumentParty.RESPONDENT, YesOrNo.NO, YesOrNo.NO, null);
        beforeEventDocList.add(removedDoc);
        beforeEventDocList.add(createContestedUploadDocumentItem(CaseDocumentType.WITNESS_STATEMENT_AFFIDAVIT,
            CaseDocumentParty.RESPONDENT, YesOrNo.NO, YesOrNo.NO, null));
        caseData.getUploadCaseDocumentWrapper()
            .getDocumentCollectionPerType(CaseDocumentCollectionType.RESP_STATEMENTS_EXHIBITS_COLLECTION)
            .addAll(beforeEventDocList);
        caseDetailsBefore.getData().getUploadCaseDocumentWrapper()
            .getDocumentCollectionPerType(CaseDocumentCollectionType.RESP_STATEMENTS_EXHIBITS_COLLECTION)
            .addAll(beforeEventDocList);
        screenUploadDocumentList.addAll(beforeEventDocList);
        screenUploadDocumentList.remove(removedDoc);

        caseDetails.getData().setManageCaseDocumentCollection(screenUploadDocumentList);

        collectionService.removeManagedDocumentFromOriginalCollection(
            FinremCallbackRequest.builder().caseDetails(caseDetails).caseDetailsBefore(caseDetailsBefore).build()
        );

        assertThat(caseData.getUploadCaseDocumentWrapper()
                .getDocumentCollectionPerType(CaseDocumentCollectionType.RESP_STATEMENTS_EXHIBITS_COLLECTION),
            hasSize(1));
    }
}