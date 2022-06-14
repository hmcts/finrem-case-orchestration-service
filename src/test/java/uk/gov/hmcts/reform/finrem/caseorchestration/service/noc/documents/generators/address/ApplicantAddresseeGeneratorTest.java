
package uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.documents.generators.address;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.bsp.common.model.document.Addressee;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangedRepresentative;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_FIRST_MIDDLE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_LAST_NAME;

@RunWith(MockitoJUnitRunner.class)
public class ApplicantAddresseeGeneratorTest {

    protected static final String APPLICANT_FIRST_MIDDLE_NAME_VALUE = "Applicant Respondent first middle name";
    protected static final String APPLICANT_LAST_NAME_VALUE = "Applicant last name";
    protected static final Map APPLICANT_ADDRESS_VALUE = Map.of("AddressLine1", "Applicant address line 1");
    protected static final String FORMATTED_ADDRESS = "formattedAddress";
    protected static final String APPLICANT_FULL_NAME = "applicantFullName";

    @Mock
    private CaseDataService caseDataService;
    @Mock
    private DocumentHelper documentHelper;

    @InjectMocks
    ApplicantAddresseeGenerator applicantAddresseeGenerator;

    CaseDetails caseDetails;
    private Map<String, Object> caseData;

    @Before
    public void setUpData() {
        caseData = Map.of(APPLICANT_FIRST_MIDDLE_NAME, APPLICANT_FIRST_MIDDLE_NAME_VALUE, APPLICANT_LAST_NAME,
            APPLICANT_LAST_NAME_VALUE, APPLICANT_ADDRESS,
            APPLICANT_ADDRESS_VALUE);
        caseDetails = CaseDetails.builder().data(caseData).build();
    }

    @Test
    public void shouldBuildApplicantAddressee() {

        when(caseDataService.buildFullName(caseData, APPLICANT_FIRST_MIDDLE_NAME, APPLICANT_LAST_NAME)).thenReturn(
            APPLICANT_FULL_NAME);
        when(documentHelper.formatAddressForLetterPrinting(APPLICANT_ADDRESS_VALUE)).thenReturn(FORMATTED_ADDRESS);

        Addressee addressee = applicantAddresseeGenerator.generate(caseDetails,
            ChangedRepresentative.builder().build(), "applicant");

        assertThat(addressee.getName(), is(APPLICANT_FULL_NAME));
        assertThat(addressee.getFormattedAddress(), is(FORMATTED_ADDRESS));

    }

}
