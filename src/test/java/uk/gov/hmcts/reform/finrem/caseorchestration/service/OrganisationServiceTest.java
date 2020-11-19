package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.OrganisationClient;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.organisation.Organisation;

import java.util.HashMap;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CREATOR_ORGANISATION;

public class OrganisationServiceTest  extends BaseServiceTest {
    @MockBean
    OrganisationClient organisationClient;


    OrganisationService organisationService;

    @Before
    public void setup() {
        organisationService = new OrganisationService(organisationClient);
    }

    @Test
    public void setsCreatorOrg() throws Exception {
        when(organisationClient.findUserOrganisation(anyString(), anyString()))
            .thenReturn(Organisation.builder().organisationIdentifier("123").build());
        CaseDetails details = CaseDetails.builder().data(new HashMap<>()).build();
        details = organisationService.setCreatorOrganisation("abc", details);
        assertThat(details.getData().get(CREATOR_ORGANISATION).toString(), is("SWNAP0V"));
    }
}
