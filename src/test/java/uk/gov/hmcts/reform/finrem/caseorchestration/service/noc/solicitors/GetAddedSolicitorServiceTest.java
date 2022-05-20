package uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.solicitors;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;

@RunWith(MockitoJUnitRunner.class)
public class GetAddedSolicitorServiceTest {
    @Mock
    private CaseDataService caseDataService;

    @InjectMocks
    private GetAddedSolicitorService getAddedSolicitorService;

    private CaseDetails caseDetails;

    @Before
    public void setUp() {

    }
}
