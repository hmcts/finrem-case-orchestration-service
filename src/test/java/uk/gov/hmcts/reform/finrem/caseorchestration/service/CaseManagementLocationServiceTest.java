package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseLocation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CASE_MANAGEMENT_LOCATION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CFC_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LONDON;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LONDON_CFC;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LONDON_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LONDON_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.REGION;

@RunWith(MockitoJUnitRunner.class)
public class CaseManagementLocationServiceTest {

    public static final long CASE_ID = 1234567890L;
    public static final String CENTRAL_FAMILY_COURT = "FR_s_CFCList_9";
    public static final String CONSENTED_CENTRAL_FAMILY_COURT = "FR_londonList_1";
    public static final int FAMILY_COURT_EPIMMS_ID = 356855;
    public static final int FAMILY_COURT_REGION_ID = 2;
    public static final String COURT_ID_MISSING_ERROR = "Selected court data is missing from caseData";

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();
    @Mock
    private CaseDataService caseDataService;
    @InjectMocks
    private CaseManagementLocationService caseManagementLocationService;

    private CallbackRequest callbackRequest;
    private CaseDetails caseDetails;
    private Map<String, Object> caseData;

    @Before
    public void setUp() {
        caseData = new HashMap<>();
        caseDetails = CaseDetails.builder().id(CASE_ID).data(caseData).build();
        callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();
    }

    @Test
    public void givenContestedCaseAndValidRequest_whenSetCaseManagementLocation_thenSetCaseManagementLocation() {
        caseData.put(REGION, LONDON);
        caseData.put(LONDON_FRC_LIST, LONDON_CFC);
        caseData.put(CFC_COURTLIST, CENTRAL_FAMILY_COURT);

        when(caseDataService.isConsentedApplication(caseDetails)).thenReturn(false);

        Map<String, Object> responseData = caseManagementLocationService.setCaseManagementLocation(callbackRequest).getData();

        CaseLocation caseManagementLocation = objectMapper.convertValue(responseData.get(CASE_MANAGEMENT_LOCATION), CaseLocation.class);

        assertThat(caseManagementLocation.getBaseLocation(), is(String.valueOf(FAMILY_COURT_EPIMMS_ID)));
        assertThat(caseManagementLocation.getRegion(), is(String.valueOf(FAMILY_COURT_REGION_ID)));
    }

    @Test
    public void givenConsentedCaseAndValidRequest_whenSetCaseManagementLocation_thenSetCaseManagementLocation() {
        caseData.put(REGION, LONDON);
        caseData.put(LONDON_FRC_LIST, LONDON);
        caseData.put(LONDON_COURTLIST, CONSENTED_CENTRAL_FAMILY_COURT);

        when(caseDataService.isConsentedApplication(caseDetails)).thenReturn(true);

        Map<String, Object> responseData = caseManagementLocationService.setCaseManagementLocation(callbackRequest).getData();

        CaseLocation caseManagementLocation = objectMapper.convertValue(responseData.get(CASE_MANAGEMENT_LOCATION), CaseLocation.class);

        assertThat(caseManagementLocation.getBaseLocation(), is(String.valueOf(FAMILY_COURT_EPIMMS_ID)));
        assertThat(caseManagementLocation.getRegion(), is(String.valueOf(FAMILY_COURT_REGION_ID)));
    }

    @Test
    public void givenConsentedInvalidRequest_whenSetCaseManagementLocation_thenReturnError() {
        when(caseDataService.isConsentedApplication(caseDetails)).thenReturn(true);

        AboutToStartOrSubmitCallbackResponse response = caseManagementLocationService.setCaseManagementLocation(callbackRequest);

        List<String> errors = response.getErrors();
        assertThat(errors, hasSize(1));
        assertThat(errors, contains(COURT_ID_MISSING_ERROR));
    }

    @Test
    public void givenContestedInvalidRequest_whenSetCaseManagementLocation_thenReturnError() {
        when(caseDataService.isConsentedApplication(caseDetails)).thenReturn(false);

        AboutToStartOrSubmitCallbackResponse response = caseManagementLocationService.setCaseManagementLocation(callbackRequest);

        List<String> errors = response.getErrors();
        assertThat(errors, hasSize(1));
        assertThat(errors, contains(COURT_ID_MISSING_ERROR));
    }
}