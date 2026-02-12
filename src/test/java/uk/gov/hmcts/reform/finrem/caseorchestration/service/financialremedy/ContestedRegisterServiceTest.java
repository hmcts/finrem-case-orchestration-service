package uk.gov.hmcts.reform.finrem.caseorchestration.service.financialremedy;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CaseResource;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.CaseDataApiV2;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SystemUserService;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ContestedRegisterServiceTest extends BaseServiceTest {

    private static final String USER_TOKEN = "USER_TOKEN";
    private static final String SERVICE_TOKEN = "SERVICE_TOKEN";
    private static final String CASE_ID = "1111";

    @Mock
    private SystemUserService systemUserService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private CaseDataApiV2 caseDataApiV2;

    @InjectMocks
    private ContestedRegisterService contestedRegisterService;

    @Before
    public void setUp() throws IOException {
        when(systemUserService.getSysUserToken()).thenReturn(USER_TOKEN);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_TOKEN);
    }

    @Test
    public void shouldGetCaseDetails() {
        CaseResource caseResource = getCaseResource();

        when(caseDataApiV2.getCaseDetails(SERVICE_TOKEN, USER_TOKEN, false, CASE_ID))
            .thenReturn(caseResource);

        CaseResource caseDetails = contestedRegisterService.getCaseDetails(CASE_ID);

        assertEquals(caseResource.getCaseType(), caseDetails.getCaseType());
        assertEquals(caseResource.getJurisdiction(), caseDetails.getJurisdiction());

        verify(caseDataApiV2).getCaseDetails(SERVICE_TOKEN, USER_TOKEN, false, CASE_ID);
        verify(authTokenGenerator).generate();
        verify(systemUserService).getSysUserToken();
    }

    private CaseResource getCaseResource() {
        CaseResource caseResource = new CaseResource();
        caseResource.setCaseType("caseType");
        caseResource.setJurisdiction("jurisdiction");

        return caseResource;
    }
}
