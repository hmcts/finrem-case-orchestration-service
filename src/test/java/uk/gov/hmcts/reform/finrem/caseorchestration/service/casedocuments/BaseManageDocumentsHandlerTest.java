package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.UploadCaseDocumentWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;

public abstract class BaseManageDocumentsHandlerTest<H extends DocumentHandler> {

    public static final String AUTH_TOKEN = "AuthTokien";
    protected List<UploadCaseDocumentCollection> screenUploadDocumentList = new ArrayList<>();
    protected FinremCaseDetails caseDetails;
    protected FinremCaseDetails caseDetailsBefore;
    protected FinremCaseData caseData;
    protected H handler;

    @Mock
    protected FeatureToggleService featureToggleService;

    @Before
    public void setUp() {
        caseDetails = buildCaseDetails();
        caseDetailsBefore = buildCaseDetails();
        caseData = caseDetails.getData();
        setUpscreenUploadDocumentList();
        caseDetails.getData().setManageCaseDocumentCollection(screenUploadDocumentList);
        handler = getDocumentHandler();
    }

    public abstract void setUpscreenUploadDocumentList();

    public abstract H getDocumentHandler();

    public abstract void assertExpectedCollectionType();

    protected abstract List<UploadCaseDocumentCollection> getDocumentCollection();


    @Test
    public abstract void assertCorrectCategoryAssignedFromDocumentType();


    private void assertDocumentCategoryIdAppliedForDocumentCollection() {
        if (featureToggleService.isCaseFileViewEnabled()) {
            for (UploadCaseDocumentCollection collection : getDocumentCollection()) {
                assertThat(collection.getUploadCaseDocument().getCaseDocuments().getCategoryId(), not(nullValue()));
            }
        } else {
            for (UploadCaseDocumentCollection collection : getDocumentCollection()) {
                assertThat(collection.getUploadCaseDocument().getCaseDocuments().getCategoryId(), nullValue());
            }
        }
    }

    private void handleDocumentCollectionsCorrectly(Boolean cfvSwitch) {

        when(featureToggleService.isCaseFileViewEnabled()).thenReturn(cfvSwitch);

        handler.replaceManagedDocumentsInCollectionType(
            FinremCallbackRequest.builder().caseDetails(caseDetails).caseDetailsBefore(caseDetails).build(),
            screenUploadDocumentList, true);

        assertExpectedCollectionType();
        assertDocumentCategoryIdAppliedForDocumentCollection();
    }


    private void handleAssignDocumentCatergoryForUploadDocumentCollections(Boolean cfvSwitch) {

        when(featureToggleService.isCaseFileViewEnabled()).thenReturn(cfvSwitch);

        handler.assignDocumentCategoryToUploadDocumentsCollection(caseData);

        assertDocumentCategoryIdAppliedForDocumentCollection();
    }

    @Test
    public void handleDocumentCollectionCorrectlyCvfOn() {
        handleDocumentCollectionsCorrectly(true);
    }

    @Test
    public void handleDocumentCollectionCorrectlyCfvOff() {
        handleDocumentCollectionsCorrectly(false);
    }

    @Test
    public void handleAssignDocumentCategoryForDocumentCollectionCorrectlyCvfOn() {
        handleAssignDocumentCatergoryForUploadDocumentCollections(true);
    }

    @Test
    public void handleAssignDocumentCategoryForDocumentCollectionCorrectlyCvfOff() {
        handleAssignDocumentCatergoryForUploadDocumentCollections(false);
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
                .caseDocumentConfidentiality(isConfidential)
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
        return FinremCaseDetails.builder().id(123L).caseType(CaseType.CONTESTED)
            .data(finremCaseData).build();
    }
}
