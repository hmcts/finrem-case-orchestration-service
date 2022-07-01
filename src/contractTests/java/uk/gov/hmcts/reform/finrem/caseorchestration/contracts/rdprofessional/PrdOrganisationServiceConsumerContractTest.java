package uk.gov.hmcts.reform.finrem.caseorchestration.contracts.rdprofessional;

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
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.PrdOrganisationConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.organisation.OrganisationContactInformation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.organisation.OrganisationsResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PrdOrganisationService;

import java.util.List;

import static io.pactfoundation.consumer.dsl.LambdaDsl.newJsonBody;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@SpringBootTest
public class PrdOrganisationServiceConsumerContractTest extends BaseTest {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String AUTHORIZATION_TOKEN = "Bearer some-access-token";
    private static final String SERVICE_AUTHORIZATION_HEADER = "ServiceAuthorization";
    private static final String ASSIGNEE_ID = "0a5874a4-3f38-4bbd-ba4c";
    private final String someServiceAuthToken = "someServiceAuthToken";

    @MockBean
    IdamService idamService;

    @Autowired
    PrdOrganisationService prdOrganisationService;

    @MockBean
    PrdOrganisationConfiguration prdOrganisationConfiguration;

    @Rule
    public PactProviderRule mockProvider = new PactProviderRule("referenceData_organisationalExternalUsers", "localhost", 8889, this);

    @Pact(provider = "referenceData_organisationalExternalUsers", consumer = "fr_caseOrchestratorService")
    public RequestResponsePact generatePactFragment(PactDslWithProvider builder) {
        // @formatter:off
        return builder
            .given("Organisation with Id exists")
            .uponReceiving("A Request to get the details of the Organisation")
            .method("GET")
            .headers(SERVICE_AUTHORIZATION_HEADER, someServiceAuthToken, AUTHORIZATION_HEADER, AUTHORIZATION_TOKEN)
            .path("/refdata/external/v1/organisations")
            .willRespondWith()
            .body(buildOrganisationResponseDsl())
            .status(HttpStatus.SC_OK)
            .toPact();
    }

    @Test
    @PactVerification()
    public void verifyRetrieveOrgansation() throws  JSONException {

        given(idamService.getIdamUserId(anyString())).willReturn(ASSIGNEE_ID);
        given(prdOrganisationConfiguration.getOrganisationsUrl()).willReturn("http://localhost:8889/refdata/external/v1/organisations");
        given(authTokenGenerator.generate()).willReturn(someServiceAuthToken);

        OrganisationsResponse response = prdOrganisationService.retrieveOrganisationsData(AUTHORIZATION_TOKEN);
        assertThat(response.getName(), is("theKCompany"));
        assertThat(response.getOrganisationIdentifier(), is("BJMSDFDS80808"));

        assertOrganisationResponse(response);
    }

    private DslPart buildOrganisationResponseDsl() {
        return newJsonBody(o -> {
            o.stringType("name", "theKCompany")
                .stringType("organisationIdentifier", "BJMSDFDS80808")
                .minArrayLike("contactInformation", 1, 1,
                    sh -> {
                        sh.stringType("addressLine1", "addressLine1")
                            .stringType("addressLine2", "addressLine2")
                            .stringType("country", "UK")
                            .stringType("postCode", "SM12SX");

                    });
        }).build();
    }

    private void assertOrganisationResponse(final OrganisationsResponse response) {
        List<OrganisationContactInformation> contactInformationList = response.getContactInformation();
        assertThat(contactInformationList.get(0).getAddressLine1(), is("addressLine1"));
        assertThat(contactInformationList.get(0).getAddressLine2(), is("addressLine2"));
        assertThat(contactInformationList.get(0).getCountry(), is("UK"));
        assertThat(contactInformationList.get(0).getPostcode(), is("SM12SX"));
    }
}