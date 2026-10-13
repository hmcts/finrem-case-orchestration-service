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
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.UpdatingDisclosureHandlerTest;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

@RunWith(MockitoJUnitRunner.Silent.class)
public class IntervenerThreeUpdatingDisclosureHandlerTest
    extends UpdatingDisclosureHandlerTest {

    @InjectMocks
    private IntervenerThreeUpdatingDisclosureHandler intervenerThreeUpdatingDisclosureHandler;

    @Override
    public void setUpscreenUploadDocumentList() {
        screenUploadDocumentList.add(createContestedUploadDocumentItem(
            CaseDocumentType.VALUATION_REPORT,
            CaseDocumentParty.INTERVENER_THREE,
            YesOrNo.NO,
            YesOrNo.NO,
            null
        ));

        screenUploadDocumentList.add(createContestedUploadDocumentItem(
            CaseDocumentType.UPDATING_DISCLOSURE,
            CaseDocumentParty.INTERVENER_THREE,
            YesOrNo.NO,
            YesOrNo.NO,
            null
        ));
    }

    @Override
    public IntervenerThreeUpdatingDisclosureHandler getDocumentHandler() {
        return intervenerThreeUpdatingDisclosureHandler;
    }

    @Override
    public void assertExpectedCollectionType() {
        assertThat(getDocumentCollection(), hasSize(1));
        assertThat(
            caseData.getManageCaseDocumentsWrapper().getManageCaseDocumentCollection(),
            hasSize(1)
        );
    }

    @Override
    protected List<UploadCaseDocumentCollection> getDocumentCollection() {
        return caseData.getUploadCaseDocumentWrapper()
            .getDocumentCollectionPerType(
                CaseDocumentCollectionType.INTERVENER_THREE_UPDATING_DISCLOSURE_COLLECTION
            );
    }

    @Override
    protected CaseDocumentParty getCaseDocumentParty() {
        return CaseDocumentParty.INTERVENER_THREE;
    }

    @Override
    protected DocumentCategory getExpectedDocumentCategory() {
        return DocumentCategory.INTERVENER_DOCUMENTS_INTERVENER_3_SUPPORTING_DOCUMENTS;
    }
}
