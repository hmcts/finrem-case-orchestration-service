package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.ShareDocumentsService.APPLICANT_DOCUMENT_COLLECTIONS_SHARING_MAP;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.ShareDocumentsService.RESPONDENT_DOCUMENT_COLLECTIONS_SHARING_MAP;

public class ShareDocumentsServiceTest extends BaseServiceTest {

    public static final String ANY_NON_NULL_VALUE = "any non-null value";
    @Autowired
    private ShareDocumentsService shareDocumentsService;

    @MockBean
    private CaseDataService caseDataService;

    @Test
    public void whenApplicantClearsSharedDocuments_thenRelevantCollectionsAreCleared() {
        CaseDetails caseDetails = buildCaseDetails();
        APPLICANT_DOCUMENT_COLLECTIONS_SHARING_MAP.values().forEach(sharedCollectionFieldName -> caseDetails.getData().put(
            sharedCollectionFieldName, ANY_NON_NULL_VALUE));

        shareDocumentsService.clearSharedDocumentsVisibleToRespondent(caseDetails);

        APPLICANT_DOCUMENT_COLLECTIONS_SHARING_MAP.values().forEach(sharedCollectionFieldName ->
            assertThat(caseDetails.getData().get(sharedCollectionFieldName), is(nullValue())));
    }

    @Test
    public void whenRespondentClearsSharedDocuments_thenRelevantCollectionsAreCleared() {
        CaseDetails caseDetails = buildCaseDetails();
        RESPONDENT_DOCUMENT_COLLECTIONS_SHARING_MAP.values().forEach(sharedCollectionFieldName -> caseDetails.getData().put(
            sharedCollectionFieldName, ANY_NON_NULL_VALUE));

        shareDocumentsService.clearSharedDocumentsVisibleToApplicant(caseDetails);

        RESPONDENT_DOCUMENT_COLLECTIONS_SHARING_MAP.values().forEach(sharedCollectionFieldName ->
            assertThat(caseDetails.getData().get(sharedCollectionFieldName), is(nullValue())));
    }

    @Test
    public void whenApplicantSharesDocuments_thenRelevantCollectionsAreCopied() {
        CaseDetails caseDetails = buildCaseDetails();

        shareDocumentsService.shareDocumentsWithRespondent(caseDetails);

        APPLICANT_DOCUMENT_COLLECTIONS_SHARING_MAP.entrySet().forEach(sourceToDestinationEntry ->
            verify(caseDataService).copyCollection(any(), eq(sourceToDestinationEntry.getKey()), eq(sourceToDestinationEntry.getValue())));
    }

    @Test
    public void whenRespondentSharesDocuments_thenRelevantCollectionsAreCopied() {
        CaseDetails caseDetails = buildCaseDetails();

        shareDocumentsService.shareDocumentsWithApplicant(caseDetails);

        RESPONDENT_DOCUMENT_COLLECTIONS_SHARING_MAP.entrySet().forEach(sourceToDestinationEntry ->
            verify(caseDataService).copyCollection(any(), eq(sourceToDestinationEntry.getKey()), eq(sourceToDestinationEntry.getValue())));
    }
}
