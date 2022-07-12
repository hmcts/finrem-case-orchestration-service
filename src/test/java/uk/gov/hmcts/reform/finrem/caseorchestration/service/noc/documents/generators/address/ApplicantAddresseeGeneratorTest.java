
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
import uk.gov.hmcts.reform.finrem.ccd.domain.ChangedRepresentative;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@RunWith(MockitoJUnitRunner.class)
public class ApplicantAddresseeGeneratorTest {

    protected static final String APPLICANT_FIRST_MIDDLE_NAME_VALUE = "Applicant Respondent first middle name";
    protected static final String APPLICANT_LAST_NAME_VALUE = "Applicant last name";
    protected static final Address APPLICANT_ADDRESS_VALUE = Address.builder().addressLine1("Applicant address line 1").build();
    protected static final String FORMATTED_ADDRESS = "formattedAddress";
    protected static final String APPLICANT_FULL_NAME = "Applicant address line 1\n";

    @Mock
    private CaseDataService caseDataService;
    @Mock
    private DocumentHelper documentHelper;

    @InjectMocks
    ApplicantAddresseeGenerator applicantAddresseeGenerator;

    FinremCaseDetails caseDetails;
    private FinremCaseData caseData;

    @Before
    public void setUpData() {
        caseData.getContactDetailsWrapper().setApplicantFmName(APPLICANT_FIRST_MIDDLE_NAME_VALUE);
        caseData.getContactDetailsWrapper().setApplicantLname(APPLICANT_LAST_NAME_VALUE);
        caseData.getContactDetailsWrapper().setApplicantAddress(APPLICANT_ADDRESS_VALUE);
        caseDetails = FinremCaseDetails.builder().caseData(caseData).build();
    }

    @Test
    public void shouldBuildApplicantAddressee() {

        Addressee addressee = applicantAddresseeGenerator.generate(caseDetails,
            ChangedRepresentative.builder().build(), "applicant");

        assertThat(addressee.getName(), is(APPLICANT_FULL_NAME));
        assertThat(addressee.getFormattedAddress(), is(FORMATTED_ADDRESS));
    }
}
