package uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.documents.generators.address;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.bsp.common.model.document.Addressee;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.ccd.domain.Address;
import uk.gov.hmcts.reform.finrem.ccd.domain.CaseType;
import uk.gov.hmcts.reform.finrem.ccd.domain.ChangedRepresentative;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@RunWith(MockitoJUnitRunner.class)
public class RespondentAddresseeGeneratorTest {

    protected static final String CONSENTED_RESPONDENT_FIRST_MIDDLE_NAME_VALUE = "Consented Respondent first middle name";
    protected static final String CONSENTED_RESPONDENT_LAST_NAME_VALUE = "Consented Respondent last name";
    protected static final String CONTESTED_RESPONDENT_FIRST_MIDDLE_NAME_VALUE = "Contested Respondent first middle name";
    protected static final String CONTESTED_RESPONDENT_LAST_NAME_VALUE = "Contested Respondent last name";
    protected static final Address RESPONDENT_ADDRESS_VALUE = Address.builder().addressLine1("Consented address line 1").build();
    protected static final String FORMATTED_ADDRESS = "formattedAddress";
    protected static final String CONTESTED_FULL_NAME = CONTESTED_RESPONDENT_FIRST_MIDDLE_NAME_VALUE + " "
        + CONTESTED_RESPONDENT_LAST_NAME_VALUE;
    protected static final String CONSENTED_FULL_NAME = CONSENTED_RESPONDENT_FIRST_MIDDLE_NAME_VALUE + " "
        + CONSENTED_RESPONDENT_LAST_NAME_VALUE;

    @Mock
    private CaseDataService caseDataService;
    @Mock
    private DocumentHelper documentHelper;

    @InjectMocks
    RespondentAddresseeGenerator respondentAddresseeGenerator;

    FinremCaseDetails caseDetails;
    private FinremCaseData caseData;

    @Before
    public void setUpData() {
        caseData.getContactDetailsWrapper().setAppRespondentFmName(CONSENTED_RESPONDENT_FIRST_MIDDLE_NAME_VALUE);
        caseData.getContactDetailsWrapper().setAppRespondentLName(CONSENTED_RESPONDENT_LAST_NAME_VALUE);
        caseData.getContactDetailsWrapper().setRespondentFmName(CONTESTED_RESPONDENT_FIRST_MIDDLE_NAME_VALUE);
        caseData.getContactDetailsWrapper().setRespondentLname(CONTESTED_RESPONDENT_LAST_NAME_VALUE);
        caseData.getContactDetailsWrapper().setRespondentAddress(RESPONDENT_ADDRESS_VALUE);
        caseDetails = FinremCaseDetails.builder().caseData(caseData).build();
    }

    @Test
    public void whenConsentedCaseShouldBuildContestedRespondentAddressee() {

        caseData.setCcdCaseType(CaseType.CONTESTED);

        Addressee addressee = respondentAddresseeGenerator.generate(caseDetails,
            ChangedRepresentative.builder().build(), "respondent");

        assertThat(addressee.getName(), is(CONTESTED_FULL_NAME));
        assertThat(addressee.getFormattedAddress(), is(FORMATTED_ADDRESS));

    }

    @Test
    public void whenContestedCaseShouldBuildContestedRespondentAddressee() {

        caseData.setCcdCaseType(CaseType.CONSENTED);

        Addressee addressee = respondentAddresseeGenerator.generate(caseDetails,
            ChangedRepresentative.builder().build(),
            "respondent");

        assertThat(addressee.getName(), is(CONSENTED_FULL_NAME));
        assertThat(addressee.getFormattedAddress(), is(FORMATTED_ADDRESS));

    }
}
