package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseLocation;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CASE_MANAGEMENT_LOCATION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CFC_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LONDON;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LONDON_CFC;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LONDON_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.REGION;

@RunWith(MockitoJUnitRunner.class)
public class CaseManagementLocationServiceTest {

    public static final long CASE_ID = 1234567890L;
    public static final String CENTRAL_FAMILY_COURT = "FR_s_CFCList_9";
    public static final int FAMILY_COURT_EPIMMS_ID = 356855;
    public static final int FAMILY_COURT_REGION_ID = 2;

    private CaseManagementLocationService caseManagementLocationService;
    private ObjectMapper objectMapper;

    private CallbackRequest callbackRequest;
    private CaseDetails caseDetails;
    private Map<String, Object> caseData;

    @Before
    public void setUp() {
        objectMapper = new ObjectMapper();
        caseManagementLocationService = new CaseManagementLocationService(objectMapper);
        caseData = new HashMap<>();
        caseDetails = CaseDetails.builder().id(CASE_ID).data(caseData).build();
        callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();
    }

    @Test
    public void givenValidCourtDataExists_whenSetCaseManagementLocation_thenSetCaseManagementLocation() {
        caseData.put(REGION, LONDON);
        caseData.put(LONDON_FRC_LIST, LONDON_CFC);
        caseData.put(CFC_COURTLIST, CENTRAL_FAMILY_COURT);

        Map<String, Object> responseData = caseManagementLocationService.setCaseManagementLocation(callbackRequest).getData();

        CaseLocation caseManagementLocation = objectMapper.convertValue(responseData.get(CASE_MANAGEMENT_LOCATION), CaseLocation.class);

        assertThat(caseManagementLocation.getBaseLocation(), is(String.valueOf(FAMILY_COURT_EPIMMS_ID)));
        assertThat(caseManagementLocation.getRegion(), is(String.valueOf(FAMILY_COURT_REGION_ID)));
    }
}