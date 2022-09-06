package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.CaseFlagsConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;

import java.io.InputStream;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MINI_FORM_A;

@RunWith(MockitoJUnitRunner.class)
public class CreateCaseServiceTest {

    public static final String CREATE_CASE_SERVICE_TEST_DOCUMENT = "CreateCaseServiceTestDocument";
    public static final String A_VALID_SERVICE_TOKEN = "A valid TOKEN";
    @Mock
    private OnlineFormDocumentService onlineFormDocumentService;

    @Mock
    private IdamService idamService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;
    @Mock
    private CoreCaseDataApi coreCaseDataApi;
    @Mock
    private CaseFlagsConfiguration caseFlagsConfiguration;

    @InjectMocks
    private CreateCaseService createCaseService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Before
    public void setUp() {
        when(authTokenGenerator.generate()).thenReturn(A_VALID_SERVICE_TOKEN);
        when(caseFlagsConfiguration.getHmctsId()).thenReturn("financial-remedy");
        when(coreCaseDataApi.submitSupplementaryData(any(), any(), any(), any())).thenReturn(getCase());
    }


    @Test
    public void givenCallbackRequestUserNotAdmin_whenDraftContestedMiniFormA_thenMiniFormAPresentAndAppIsRepresented() {
        CallbackRequest callbackRequest =
            CallbackRequest.builder().caseDetails(getCase()).build();
        callbackRequest.getCaseDetails().getData().put(APPLICANT_REPRESENTED, null);
        CaseDocument testDocument = CaseDocument.builder().documentFilename(CREATE_CASE_SERVICE_TEST_DOCUMENT).build();
        when(onlineFormDocumentService.generateDraftContestedMiniFormA(any(), any()))
            .thenReturn(testDocument);
        when(idamService.isUserRoleAdmin(AUTH_TOKEN)).thenReturn(false);

        createCaseService.draftContestedMiniFormA(callbackRequest, AUTH_TOKEN);

        Map<String, Object> data = callbackRequest.getCaseDetails().getData();
        assertThat(((CaseDocument) data.get(MINI_FORM_A)).getDocumentFilename(),
            is(CREATE_CASE_SERVICE_TEST_DOCUMENT));
        assertThat(data.get(APPLICANT_REPRESENTED), is(YES_VALUE));
    }

    @Test
    public void givenCallbackRequestUserAdmin_whenDraftContestedMiniFormA_thenMiniFormAPresentAndAppRepresentedEmpty() {
        CallbackRequest callbackRequest =
            CallbackRequest.builder().caseDetails(getCase()).build();
        callbackRequest.getCaseDetails().getData().put(APPLICANT_REPRESENTED, null);
        CaseDocument testDocument = CaseDocument.builder().documentFilename(CREATE_CASE_SERVICE_TEST_DOCUMENT).build();
        when(onlineFormDocumentService.generateDraftContestedMiniFormA(any(), any()))
            .thenReturn(testDocument);
        when(idamService.isUserRoleAdmin(AUTH_TOKEN)).thenReturn(true);

        createCaseService.draftContestedMiniFormA(callbackRequest, AUTH_TOKEN);

        Map<String, Object> data = callbackRequest.getCaseDetails().getData();
        assertThat(((CaseDocument) data.get(MINI_FORM_A)).getDocumentFilename(),
            is(CREATE_CASE_SERVICE_TEST_DOCUMENT));
        assertThat(data.get(APPLICANT_REPRESENTED), is(nullValue()));
    }

    @Test
    public void givenCallbackRequest_whenSetSupplementaryData_thenCallCcdSubmitSupplementaryData() {
        CallbackRequest callbackRequest =
            CallbackRequest.builder().caseDetails(getCase()).build();

        createCaseService.setSupplementaryData(callbackRequest, AUTH_TOKEN);

        verify(coreCaseDataApi,
            times(1)).submitSupplementaryData(any(), any(), any(), any());
    }


    private CaseDetails getCase() {
        try (InputStream resourceAsStream = getClass()
            .getResourceAsStream("/fixtures/contested/validate-hearing-with-fastTrackDecision.json")) {
            return objectMapper.readValue(resourceAsStream, CallbackRequest.class).getCaseDetails();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}