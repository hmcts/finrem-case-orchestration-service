package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenertwo;

import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.DocumentHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.HearingBundleHandlerTest;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

@RunWith(MockitoJUnitRunner.Silent.class)
public class IntervenerTwoHearingBundleHandlerTest extends HearingBundleHandlerTest {

    @InjectMocks
    IntervenerTwoHearingBundleHandler handler;

    @Override
    public void setUpscreenUploadDocumentList() {
        screenUploadDocumentList.add(createContestedUploadDocumentItem(CaseDocumentType.TRIAL_BUNDLE,
            CaseDocumentParty.INTERVENER_TWO, YesOrNo.NO, YesOrNo.NO, null));
    }

    @Override
    public DocumentHandler getDocumentHandler() {
        return handler;
    }

    @Override
    public void assertExpectedCollectionType() {
        assertThat(getDocumentCollection(),
            hasSize(1));
        assertThat(caseData.getManageCaseDocumentsWrapper().getManageCaseDocumentCollection(),
            hasSize(0));
    }

    @Override
    protected List<UploadCaseDocumentCollection> getDocumentCollection() {
        return caseData.getUploadCaseDocumentWrapper()
            .getDocumentCollectionPerType(CaseDocumentCollectionType.INTERVENER_TWO_HEARING_BUNDLES_COLLECTION);
    }
}
