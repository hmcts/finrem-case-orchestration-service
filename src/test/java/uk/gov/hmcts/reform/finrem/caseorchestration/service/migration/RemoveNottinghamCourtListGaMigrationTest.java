package uk.gov.hmcts.reform.finrem.caseorchestration.service.migration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils;

import java.util.Map;

import static org.junit.Assert.assertNull;

public class RemoveNottinghamCourtListGaMigrationTest extends BaseServiceTest {

    @Autowired
    private ObjectMapper mapper;

    @Test
    public void shouldRemove_nottinghamCourtListGA_fromCase() {
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(
            "/fixtures/migration/removeNottinghamCourtListGAMigration/ccd-migrate-remove-nottingham-court-list-ga.json",
            mapper
        );
        RemoveNottinghamCourtListGaMigration classUnderTest = new RemoveNottinghamCourtListGaMigration();

        Map<String, Object> migratedCaseData = classUnderTest.migrate(caseDetails);

        assertNull(migratedCaseData.get("nottinghamCourtListGA"));
    }
}
