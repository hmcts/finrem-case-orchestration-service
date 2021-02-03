package uk.gov.hmcts.reform.finrem.caseorchestration.contracts.rdprofessional;

import static io.pactfoundation.consumer.dsl.LambdaDsl.newJsonBody;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

import uk.gov.hmcts.reform.finrem.caseorchestration.BaseTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.PrdOrganisationConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.organisation.OrganisationContactInformation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.organisation.OrganisationsResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PrdOrganisationService;
import java.util.List;

import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit.PactProviderRule;
import au.com.dius.pact.consumer.junit.PactVerification;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
public class PrdOrganisationServiceConsumerContractTest extends BaseTest {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String authorizationToken = "Bearer some-access-token";
    private static final String SERVICE_AUTHORIZATION_HEADER = "ServiceAuthorization";
    private static final String ASSIGNEE_ID = "0a5874a4-3f38-4bbd-ba4c";
    private final String someServiceAuthToken = "someServiceAuthToken";

    @MockBean
    IdamService idamService;

    @Autowired
    PrdOrganisationService prdOrganisationService;

    @MockBean
    PrdOrganisationConfiguration prdOrganisationConfiguration;

    @Rule // TODO check provider Name.
    public PactProviderRule mockProvider = new PactProviderRule("referenceData_organisationalInternal", "localhost", 8889, this);

    // TODO check provider Name.
    @Pact(provider = "referenceData_organisationalInternal", consumer = "fr_caseOrchestratorService")
    public RequestResponsePact generatePactFragment(PactDslWithProvider builder) throws JSONException{
        // @formatter:off
        return builder
            .given("An Organisation exists")
            .uponReceiving("A Request to get the details of the Organisation")
            .method("GET")
            .headers(SERVICE_AUTHORIZATION_HEADER, someServiceAuthToken, AUTHORIZATION_HEADER, authorizationToken)
            .path("/refdata/external/v1/organisations")
            .willRespondWith()
            .body(buildOrganisationResponseDsl())
            .status(HttpStatus.SC_CREATED)
            .toPact();
    }

    @Test
    @PactVerification()
    public void verifyRetrieveOrgansation() throws  JSONException {

        given(idamService.getIdamUserId(anyString())).willReturn(ASSIGNEE_ID);
        given(prdOrganisationConfiguration.getOrganisationsUrl()).willReturn("http://localhost:8889/refdata/external/v1/organisations");
        given(authTokenGenerator.generate()).willReturn(someServiceAuthToken);

        OrganisationsResponse response = prdOrganisationService.retrieveOrganisationsData(authorizationToken);
        assertThat(response.getName(), is("theKCompany"));
        assertThat(response.getOrganisationIdentifier(), is("BJMSDFDS80808"));

        assertOrganisationResponse(response);
    }

    private DslPart buildOrganisationResponseDsl(){
        return newJsonBody((o) -> {
            o.stringType("name", "theKCompany")
                .stringType("organisationIdentifier", "BJMSDFDS80808")
                .minArrayLike("contactInformation", 1, 1,
                    (sh) -> {
                        sh.stringType("addressLine1", "addressLine1")
                            .stringType("addressLine2", "addressLine2")
                            .stringType("addressLine3", "addressLine3")
                            .stringType("country", "UK")
                            .stringType("county", "Surrey")
                            .stringType("postCode", "SM12SX")
                            .stringType("townCity", "Sutton");
                    });
        }).build();
    }

    private void assertOrganisationResponse(final OrganisationsResponse response) {
        List<OrganisationContactInformation> contactInformationList = response.getContactInformation();
        assertThat(contactInformationList.get(0).getAddressLine1(), is("addressLine1"));
        assertThat(contactInformationList.get(0).getAddressLine2(), is("addressLine2"));
        assertThat(contactInformationList.get(0).getAddressLine3(), is("addressLine3"));
        assertThat(contactInformationList.get(0).getCountry(), is("UK"));
        assertThat(contactInformationList.get(0).getCounty(), is("Surrey"));
        assertThat(contactInformationList.get(0).getPostcode(), is("SM12SX"));
        assertThat(contactInformationList.get(0).getTownCity(), is("Sutton"));
    }
}