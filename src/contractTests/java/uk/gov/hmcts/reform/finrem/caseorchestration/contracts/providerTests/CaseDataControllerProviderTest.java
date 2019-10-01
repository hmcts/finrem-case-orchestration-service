package uk.gov.hmcts.reform.finrem.caseorchestration.contracts.providerTests;


import au.com.dius.pact.provider.junit.PactRunner;
import au.com.dius.pact.provider.junit.Provider;
import au.com.dius.pact.provider.junit.State;
import au.com.dius.pact.provider.junit.loader.PactFolder;
import au.com.dius.pact.provider.junit.target.HttpTarget;
import au.com.dius.pact.provider.junit.target.Target;
import au.com.dius.pact.provider.junit.target.TestTarget;

import au.com.dius.pact.provider.junit5.HttpTestTarget;
import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import org.junit.After;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;

import java.util.concurrent.Executor;

@RunWith(PactRunner.class)
@ExtendWith(SpringExtension.class)
@PactFolder("target/pacts")
@Provider("Finrem_Cos_Service")

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
    properties = "server.port=8080"
)
public class CaseDataControllerProviderTest extends BaseProviderTest{


    @MockBean
    private IdamService idamService;

    @TestTarget
    public final Target target = new HttpTarget("http", "localhost", 8080, "/");

    @BeforeEach
    void setupTestTarget(PactVerificationContext context) {
        context.setTarget(new HttpTestTarget("localhost", 8080, "/"));
    }

    @State({"Finrem service provides default values for contested","Finrem service provides default values for consented"})
    public void testProviderState(){
        Mockito.when(idamService.isUserRoleAdmin("someAuthorizationToken")).thenReturn(false);

    }

}
