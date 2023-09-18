package uk.gov.hmcts.reform.finrem.caseorchestration.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CfcCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.AllocatedCourtWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralApplicationCourtWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.InterimCourtWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.FrcCourtDetails;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CourtDetailsMapperTest {

    private CourtDetailsMapper courtDetailsMapper;

    @Before
    public void setUp() {
        courtDetailsMapper = new CourtDetailsMapper(new ObjectMapper());
    }

    @Test
    public void givenDefaultCourtListAndValidFieldDeclarations_whenGetCourtDetails_thenReturnExpectedCourtDetails() {
        AllocatedCourtWrapper courtList = new AllocatedCourtWrapper();
        courtList.setCfcCourtList(CfcCourt.BROMLEY_COUNTY_COURT_AND_FAMILY_COURT);

        FrcCourtDetails courtDetails = courtDetailsMapper.getCourtDetails(courtList);

        assertThat(courtDetails.getCourtName(), is("Bromley County Court And Family Court"));
        assertThat(courtDetails.getCourtAddress(), is("Bromley County Court, College Road, Bromley, BR1 3PX"));
        assertThat(courtDetails.getEmail(), is("family.bromley.countycourt@justice.gov.uk"));
        assertThat(courtDetails.getPhoneNumber(), is("0208 290 9620"));
    }

    @Test
    public void givenGeneralApplicationCourtListAndValidFieldDeclarations_whenGetCourtDetails_thenReturnExpectedCourtDetails() {
        GeneralApplicationCourtWrapper courtList = new GeneralApplicationCourtWrapper();
        courtList.setGeneralApplicationDirectionsCfcCourtList(CfcCourt.CROYDON_COUNTY_COURT_AND_FAMILY_COURT);

        FrcCourtDetails courtDetails = courtDetailsMapper.getCourtDetails(courtList);

        assertThat(courtDetails.getCourtName(), is("Croydon County Court And Family Court"));
        assertThat(courtDetails.getCourtAddress(), is("Croydon County Court, Altyre Road, Croydon, CR9 5AB"));
        assertThat(courtDetails.getEmail(), is("family.croydon.countycourt@justice.gov.uk"));
        assertThat(courtDetails.getPhoneNumber(), is("0300 123 5577"));
    }

    @Test
    public void givenInterimCourtListAndValidFieldDeclarations_whenGetCourtDetails_thenReturnExpectedCourtDetails() {
        InterimCourtWrapper courtList = new InterimCourtWrapper();
        courtList.setInterimCfcCourtList(CfcCourt.BARNET_CIVIL_AND_FAMILY_COURTS_CENTRE);

        FrcCourtDetails courtDetails = courtDetailsMapper.getCourtDetails(courtList);

        assertThat(courtDetails.getCourtName(), is("Barnet Civil And Family Courts Centre"));
        assertThat(courtDetails.getCourtAddress(), is("Barnet County Court, St Marys Court, "
            + "Regents Park Road, Finchley Central, London, N3 1BQ"));
        assertThat(courtDetails.getEmail(), is("family.barnet.countycourt@justice.gov.uk"));
        assertThat(courtDetails.getPhoneNumber(), is("0208 371 7111"));
    }

    @Test
    public void givenCourtListAndInvalidFieldDeclarations_whenGetCourtDetails_thenThrowIllegalStateException() {
        try {
            courtDetailsMapper.getCourtDetails(new AllocatedCourtWrapper());
        } catch (IllegalStateException ise) {
            String expectedMessage = "There must be exactly one court selected in case data";
            assertTrue(ise.getMessage().contains(expectedMessage));
        }
    }
}