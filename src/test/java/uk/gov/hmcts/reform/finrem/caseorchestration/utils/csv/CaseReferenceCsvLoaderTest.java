package uk.gov.hmcts.reform.finrem.caseorchestration.utils.csv;

import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class CaseReferenceCsvLoaderTest {

    CaseReferenceCsvLoader caseReferenceCsvLoader = new CaseReferenceCsvLoader();

    @Test
    public void shouldLoadCaseRefsCsvFile() {
        List<CaseReference> caseReferences = caseReferenceCsvLoader.loadCaseReferenceList("caserefs-test.csv");

        assertThat(caseReferences.isEmpty(), equalTo(Boolean.FALSE));
        assertThat(caseReferences.size(), equalTo(4));
    }

    @Test
    public void shouldReturnEmptyCollectionWhenFileNotFound() {
        List<CaseReference> caseReferences = caseReferenceCsvLoader.loadCaseReferenceList("caserefs-test-file-does-not-exist.csv");

        assertThat(caseReferences.isEmpty(), equalTo(Boolean.TRUE));
    }
}