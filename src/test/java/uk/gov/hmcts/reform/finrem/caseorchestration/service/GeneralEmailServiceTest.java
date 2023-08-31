package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralEmailCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralEmailHolder;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

public class GeneralEmailServiceTest extends BaseServiceTest {

    @Autowired
    private GeneralEmailService generalEmailService;

    @Test
    public void generateGeneralEmailConsented() throws Exception {
        FinremCaseDetails caseDetails =
            buildFinremCallbackRequest("/fixtures/general-email-consented.json").getCaseDetails();
        generalEmailService.storeGeneralEmail(caseDetails);
        List<GeneralEmailCollection> generalEmailCollections = caseDetails.getData().getGeneralEmailWrapper().getGeneralEmailCollection();
        assertThat(generalEmailCollections, hasSize(2));

        GeneralEmailHolder originalEmail = generalEmailCollections.get(0).getValue();
        assertThat(originalEmail.getGeneralEmailRecipient(), is("a1@a.com"));
        assertThat(originalEmail.getGeneralEmailCreatedBy(), is("first user"));
        assertThat(originalEmail.getGeneralEmailBody(), is("original email body"));

        GeneralEmailHolder addedEmail = generalEmailCollections.get(1).getValue();
        ;
        assertThat(addedEmail.getGeneralEmailRecipient(), is("b1@b.com"));
        assertThat(addedEmail.getGeneralEmailCreatedBy(), is("Test user"));
        assertThat(addedEmail.getGeneralEmailBody(), is("Test email body"));
    }

    @Test
    public void generateGeneralEmailContested() throws Exception {
        FinremCaseDetails caseDetails =
            buildFinremCallbackRequest("/fixtures/contested/general-email-contested.json").getCaseDetails();
        generalEmailService.storeGeneralEmail(caseDetails);
        List<GeneralEmailCollection> generalEmailCollections = caseDetails.getData().getGeneralEmailWrapper().getGeneralEmailCollection();
        assertThat(generalEmailCollections, hasSize(2));

        GeneralEmailHolder originalEmail = generalEmailCollections.get(0).getValue();
        assertThat(originalEmail.getGeneralEmailRecipient(), is("a1@a.com"));
        assertThat(originalEmail.getGeneralEmailCreatedBy(), is("first user"));
        assertThat(originalEmail.getGeneralEmailBody(), is("original email body"));

        GeneralEmailHolder addedEmail = generalEmailCollections.get(1).getValue();
        assertThat(addedEmail.getGeneralEmailRecipient(), is("b1@b.com"));
        assertThat(addedEmail.getGeneralEmailCreatedBy(), is("Test user"));
        assertThat(addedEmail.getGeneralEmailBody(), is("Test email body"));
    }
}
