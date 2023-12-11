package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenerthree;

import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.BaseManageDocumentsHandlerTest;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

@RunWith(MockitoJUnitRunner.Silent.class)
public class IntervenerThreeChronologiesStatementHandlerTest extends BaseManageDocumentsHandlerTest {
    @InjectMocks
    IntervenerThreeChronologiesStatementHandler handler;

    @Override
    public void setUpscreenUploadDocumentList() {
        screenUploadDocumentList.add(createContestedUploadDocumentItem(CaseDocumentType.STATEMENT_OF_ISSUES,
            CaseDocumentParty.INTERVENER_THREE, YesOrNo.NO, YesOrNo.NO, null));
        screenUploadDocumentList.add(createContestedUploadDocumentItem(CaseDocumentType.CHRONOLOGY,
            CaseDocumentParty.INTERVENER_THREE, YesOrNo.NO, YesOrNo.NO, null));
        screenUploadDocumentList.add(createContestedUploadDocumentItem(CaseDocumentType.FORM_G,
            CaseDocumentParty.INTERVENER_THREE, YesOrNo.NO, YesOrNo.NO, null));
    }

    @Override
    public IntervenerThreeChronologiesStatementHandler getDocumentHandler() {
        return handler;
    }

    @Override
    public void assertExpectedCollectionType() {
        assertThat(getDocumentCollection(),
            hasSize(3));
        assertThat(caseData.getManageCaseDocumentCollection(),
            hasSize(0));
    }

    @Override
    protected List<UploadCaseDocumentCollection> getDocumentCollection() {
        return caseData.getUploadCaseDocumentWrapper()
            .getDocumentCollectionPerType(CaseDocumentCollectionType.INTERVENER_THREE_CHRONOLOGIES_STATEMENTS_COLLECTION);
    }

    @Override
    public void assertCorrectCategoryAssignedFromDocumentType() {
        assertThat(
            handler.getDocumentCategoryFromDocumentType(CaseDocumentType.STATEMENT_OF_ISSUES),
            is(DocumentCategory.HEARING_DOCUMENTS_INTERVENER_3_CONCISE_STATEMENT_OF_ISSUES)
        );

        assertThat(
            handler.getDocumentCategoryFromDocumentType(CaseDocumentType.CHRONOLOGY),
            is(DocumentCategory.HEARING_DOCUMENTS_INTERVENER_3_CHRONOLOGY)
        );

        assertThat(
            handler.getDocumentCategoryFromDocumentType(CaseDocumentType.FORM_G),
            is(DocumentCategory.INTERVENER_DOCUMENTS_INTERVENER_3_FORM_G)
        );
    }
}
