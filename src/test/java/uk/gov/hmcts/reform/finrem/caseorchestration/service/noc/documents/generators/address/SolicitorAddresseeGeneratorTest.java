package uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.documents.generators.address;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.bsp.common.model.document.Addressee;
import uk.gov.hmcts.reform.finrem.ccd.domain.Address;
import uk.gov.hmcts.reform.finrem.ccd.domain.CaseType;
import uk.gov.hmcts.reform.finrem.ccd.domain.ChangedRepresentative;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.Organisation;
import uk.gov.hmcts.reform.finrem.ccd.domain.YesOrNo;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@RunWith(MockitoJUnitRunner.class)
public class SolicitorAddresseeGeneratorTest {

    protected static final Address SOLICITOR_ADDRESS_VALUE = Address.builder().addressLine1("Consented address line 1").build();
    protected static final Address SOLICITOR_ADDRESS_VALUE_CONTESTED = Address.builder().addressLine1("Contested address line 1").build();
    protected static final String FORMATTED_ADDRESS_CONSENTED = "Consented address line 1";
    protected static final String FORMATTED_ADDRESS_CONTESTED = "Contested address line 1";
    protected static final String REPRESENTATIVE_NAME = "representativeName";
    protected static final String ORGANISATION_ID = "organisationId";
    protected static final String ORGANISATION_NAME = "organisationName";

    @InjectMocks
    SolicitorAddresseeGenerator solicitorAddresseeGenerator;

    FinremCaseDetails caseDetails;
    private ChangedRepresentative changedRepresentative;

    @Before
    public void setUpData() {
        FinremCaseData caseData = new FinremCaseData();
        caseData.getContactDetailsWrapper().setSolicitorAddress(SOLICITOR_ADDRESS_VALUE);
        caseData.getContactDetailsWrapper().setApplicantRepresented(YesOrNo.YES);
        caseData.setCcdCaseType(CaseType.CONSENTED);
        caseDetails = FinremCaseDetails.builder().caseType(CaseType.CONSENTED).caseData(caseData).build();
        changedRepresentative = ChangedRepresentative.builder().name(REPRESENTATIVE_NAME)
            .organisation(Organisation.builder().organisationID(ORGANISATION_ID).organisationName(ORGANISATION_NAME).build()).build();
    }

    @Test
    public void whenConsentedCaseShouldBuildContestedSolicitorsAddressee() {
        caseDetails.getCaseData().getContactDetailsWrapper().setSolicitorName(REPRESENTATIVE_NAME);
        Addressee addressee = solicitorAddresseeGenerator.generate(caseDetails, changedRepresentative, "applicant");
        assertThat(addressee.getName(), is(REPRESENTATIVE_NAME));
        assertThat(addressee.getFormattedAddress(), is(FORMATTED_ADDRESS_CONSENTED));

    }

    @Test
    public void whenContestedCaseShouldBuildContestedSolicitorsAddressee() {
        caseDetails.getCaseData().getContactDetailsWrapper().setApplicantSolicitorName(REPRESENTATIVE_NAME);
        caseDetails.getCaseData().getContactDetailsWrapper().setApplicantSolicitorAddress(SOLICITOR_ADDRESS_VALUE_CONTESTED);
        caseDetails.getCaseData().setCcdCaseType(CaseType.CONTESTED);
        Addressee addressee = solicitorAddresseeGenerator.generate(caseDetails, changedRepresentative, "applicant");
        assertThat(addressee.getName(), is(REPRESENTATIVE_NAME));
        assertThat(addressee.getFormattedAddress(), is(FORMATTED_ADDRESS_CONTESTED));

    }

    @Test
    public void whenRespondentSolicitorShouldBuildRespSolicitorAddressee() {
        caseDetails.getCaseData().getContactDetailsWrapper().setRespondentSolicitorName(REPRESENTATIVE_NAME);
        caseDetails.getCaseData().getContactDetailsWrapper().setRespondentSolicitorAddress(SOLICITOR_ADDRESS_VALUE);
        caseDetails.getCaseData().getContactDetailsWrapper().setConsentedRespondentRepresented(YesOrNo.YES);

        Addressee addressee = solicitorAddresseeGenerator.generate(caseDetails, changedRepresentative, "respondent");
        assertThat(addressee.getName(), is(REPRESENTATIVE_NAME));
        assertThat(addressee.getFormattedAddress(), is(FORMATTED_ADDRESS_CONSENTED));
    }
}
