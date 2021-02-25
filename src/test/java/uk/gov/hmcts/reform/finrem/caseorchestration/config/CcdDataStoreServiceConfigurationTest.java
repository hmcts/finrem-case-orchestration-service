package uk.gov.hmcts.reform.finrem.caseorchestration.config;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;

import static org.junit.Assert.assertEquals;

public class CcdDataStoreServiceConfigurationTest extends BaseServiceTest {
    @Autowired private CcdDataStoreServiceConfiguration ccdDataStoreServiceConfiguration;

    @Test
    public void shouldReturnTheConfiguration() {
        assertEquals("http://localhost:4452/case-users", ccdDataStoreServiceConfiguration.getCaseUsersUrl());
    }
}