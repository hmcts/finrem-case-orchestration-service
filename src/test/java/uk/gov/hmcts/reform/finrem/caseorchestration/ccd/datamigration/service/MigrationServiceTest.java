package uk.gov.hmcts.reform.finrem.caseorchestration.ccd.datamigration.service;

import org.hamcrest.core.Is;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.datamigration.model.prod.ProdCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.datamigration.model.prod.ProdCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.datamigration.model.v1.CaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.datamigration.model.v1.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Address;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class MigrationServiceTest {

    private MigrationService underTest;

    @Before
    public void setUp() {
        underTest = new MigrationService();
    }

    @Test
    public void shouldRemoveRedundantAddressLines() {
        ProdCaseDetails prodCaseDetails = setProdCaseDetails();
        CaseDetails caseDetails = underTest.migrateTov1(prodCaseDetails);
        assertThat(caseDetails.getCaseId(), is(prodCaseDetails.getCaseId()));
        assertThat(caseDetails.getJurisdiction(), is(prodCaseDetails.getJurisdiction()));
        assertThat(caseDetails.getState(), is(prodCaseDetails.getState()));
        ProdCaseData prodCaseData = prodCaseDetails.getCaseData();
        CaseData caseData = caseDetails.getCaseData();
        assertThat(caseDetails.getCaseId(), is(prodCaseDetails.getCaseId()));
        assertThat(caseDetails.getCaseId(), is(prodCaseDetails.getCaseId()));
        assertThat(caseDetails.getCaseId(), is(prodCaseDetails.getCaseId()));

        assertThat(caseData.getApplicantFMName(), is(prodCaseData.getApplicantFMName()));
        assertThat(caseData.getAppRespondentLName(), is(prodCaseData.getAppRespondentLName()));
        assertThat(caseData.getD81Question(), is(prodCaseData.getD81Question()));

        Address solicitorAddress = caseData.getSolicitorAddress();
        Address prodSolicitorAddress = prodCaseData.getSolicitorAddress();
        assertThat(solicitorAddress.getAddressLine1(), is(prodSolicitorAddress.getAddressLine1()));
        assertThat(solicitorAddress.getAddressLine2(), is(prodSolicitorAddress.getAddressLine2()));
        assertThat(solicitorAddress.getAddressLine3(), is(prodSolicitorAddress.getAddressLine3()));
        assertThat(solicitorAddress.getPostTown(), is(prodSolicitorAddress.getPostTown()));
        assertThat(solicitorAddress.getPostCode(), is(prodSolicitorAddress.getPostCode()));

        Address respondantAddress = caseData.getRespondentAddress();
        Address prodRespSolicitorAddress = prodCaseData.getRespondentSolicitorAddress();
        assertThat(respondantAddress.getAddressLine1(), is(prodRespSolicitorAddress.getAddressLine1()));
        assertThat(respondantAddress.getAddressLine2(), is(prodRespSolicitorAddress.getAddressLine2()));
        assertThat(respondantAddress.getAddressLine3(), is(prodRespSolicitorAddress.getAddressLine3()));
        assertThat(respondantAddress.getPostTown(), is(prodRespSolicitorAddress.getPostTown()));
        assertThat(respondantAddress.getPostCode(), is(prodRespSolicitorAddress.getPostCode()));
    }

    private ProdCaseDetails setProdCaseDetails() {
        ProdCaseDetails prodCaseDetails = new ProdCaseDetails();
        prodCaseDetails.setCaseId("11111");
        prodCaseDetails.setJurisdiction("Divorce");
        prodCaseDetails.setState("Application Drafted");
        ProdCaseData prodCaseData = new ProdCaseData();
        prodCaseData.setD81Question("D81 Joint");
        prodCaseData.setApplicantFMName("Name");
        prodCaseData.setApplicantLName("Applicant Last Name");
        Address solicitorAddress = new Address();
        solicitorAddress.setAddressLine1("Flat 199");
        solicitorAddress.setAddressLine2("Ilford");
        solicitorAddress.setPostCode("IG11NL");
        solicitorAddress.setPostTown("Ilford");

        Address respondentAddress = new Address();
        respondentAddress.setAddressLine1("Flat 197");
        respondentAddress.setAddressLine2("Ilford");
        respondentAddress.setPostCode("IG11NL");
        respondentAddress.setPostTown("Ilford");
        prodCaseData.setSolicitorAddress(respondentAddress);
        prodCaseData.setRespondentAddress(respondentAddress);

        prodCaseData.setSolicitorAddress1("Flat 199");
        prodCaseData.setSolicitorAddress2("Ilford");
        prodCaseData.setSolicitorAddress3("Ilford");
        prodCaseData.setSolicitorAddress4("Ilford");
        prodCaseData.setSolicitorAddress5("Ilford");
        prodCaseData.setSolicitorAddress6("Ilford");

        prodCaseData.setRespondantSolicitorAddress1("Flat 197");
        prodCaseData.setRespondantSolicitorAddress2("Ilford");
        prodCaseData.setRespondantSolicitorAddress3("Ilford");
        prodCaseData.setRespondantSolicitorAddress4("Ilford");
        prodCaseData.setRespondantSolicitorAddress5("Ilford");
        prodCaseData.setRespondantSolicitorAddress6("Ilford");

        prodCaseDetails.setCaseData(prodCaseData);
        return prodCaseDetails;
    }
}