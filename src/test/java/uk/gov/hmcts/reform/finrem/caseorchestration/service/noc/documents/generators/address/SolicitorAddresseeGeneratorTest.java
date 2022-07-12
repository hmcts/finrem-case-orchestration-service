package uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.documents.generators.address;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.bsp.common.model.document.Addressee;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.ccd.domain.Address;
import uk.gov.hmcts.reform.finrem.ccd.domain.CaseType;
import uk.gov.hmcts.reform.finrem.ccd.domain.ChangedRepresentative;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.Organisation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@RunWith(MockitoJUnitRunner.class)
public class SolicitorAddresseeGeneratorTest {

    protected static final Address SOLICITOR_ADDRESS_VALUE = Address.builder().addressLine1("Contested address line 1").build();
    protected static final String FORMATTED_ADDRESS = "formattedAddress";
    protected static final String REPRESENTATIVE_NAME = "representativeName";
    protected static final String ORGANISATION_ID = "organisationId";
    protected static final String ORGANISATION_NAME = "organisationName";

    @Mock
    private DocumentHelper documentHelper;

    @InjectMocks
    SolicitorAddresseeGenerator solicitorAddresseeGenerator;

    FinremCaseDetails caseDetails;
    private ChangedRepresentative changedRepresentative;

    @Before
    public void setUpData() {
        FinremCaseData caseData = new FinremCaseData();
        caseData.getContactDetailsWrapper().setSolicitorAddress(SOLICITOR_ADDRESS_VALUE);
        caseDetails = FinremCaseDetails.builder().caseType(CaseType.CONSENTED).caseData(caseData).build();
        changedRepresentative = ChangedRepresentative.builder().name(REPRESENTATIVE_NAME)
            .organisation(Organisation.builder().organisationID(ORGANISATION_ID).organisationName(ORGANISATION_NAME).build()).build();
    }

    @Test
    public void whenConsentedCaseShouldBuildContestedSolicitorsAddressee() {
        Addressee addressee = solicitorAddresseeGenerator.generate(caseDetails, changedRepresentative, "applicant");
        assertThat(addressee.getName(), is(REPRESENTATIVE_NAME));
        assertThat(addressee.getFormattedAddress(), is(FORMATTED_ADDRESS));

    }

}
