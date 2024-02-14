package uk.gov.hmcts.reform.finrem.caseorchestration.contracts.rdprofessional;

import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit.PactProviderRule;
import au.com.dius.pact.consumer.junit.PactVerification;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import org.apache.http.HttpStatus;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.OrganisationApi;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.organisation.OrganisationsResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.SERVICE_AUTHORISATION_HEADER;

@SpringBootTest(properties = {"prd.organisations.url=http://localhost:8982"})
public class OrganisationalInternalGetOrganisationByIdContractTest extends BaseTest {

    private static final String SERVICE_AUTH_TOKEN = "some-service-auth-token";
    private static final String AUTH_TOKEN = "some-auth-token";

    @Autowired
    OrganisationApi organisationApi;

    @Rule
    public PactProviderRule mockProvider = new PactProviderRule("referenceData_organisationalInternal", "localhost",
        8982, this);

    @Pact(provider = "referenceData_organisationalInternal", consumer = "fr_caseOrchestratorService")
    public RequestResponsePact generatePactFragmentForGetOrganisationById(PactDslWithProvider builder) {
        return builder
            .given("Organisation exists for given Id")
            .uponReceiving("a request to get an organisation by id")
            .method("GET")
            .headers(
                SERVICE_AUTHORISATION_HEADER, SERVICE_AUTH_TOKEN,
                AUTHORIZATION_HEADER, AUTH_TOKEN
            )
            .path("/refdata/internal/v1/organisations")
            .query("id=orgId")
            .willRespondWith()
            .body(ReferenceDataDsl.buildOrganisationResponseDsl())
            .status(HttpStatus.SC_OK)
            .toPact();
    }

    @Test
    @PactVerification(fragment = "generatePactFragmentForGetOrganisationById")
    public void verifyGetOrganisationById() {
        OrganisationsResponse response = organisationApi.findOrganisationByOrgId(AUTH_TOKEN, SERVICE_AUTH_TOKEN,
            "orgId");
        assertThat(response.getOrganisationIdentifier()).isNotEmpty();
        assertThat(response.getName()).isNotEmpty();
        assertThat(response.getContactInformation().size()).isEqualTo(1);
    }
}
