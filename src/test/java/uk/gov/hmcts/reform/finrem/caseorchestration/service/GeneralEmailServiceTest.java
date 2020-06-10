package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralEmail;

import java.io.InputStream;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

public class GeneralEmailServiceTest {

    private DocumentHelper documentHelper;
    private ObjectMapper mapper = new ObjectMapper();
    private GeneralEmailService generalEmailService;

    @Before
    public void setUp() {
        documentHelper = new DocumentHelper(mapper);
        generalEmailService = new GeneralEmailService(documentHelper, mapper);
    }

    @Test
    public void generateGeneralEmailConsented() throws Exception {
        CaseDetails caseDetails = generalEmailService.storeGeneralEmail(caseDetailsConsented());
        List<GeneralEmail> generalEmailList = (List<GeneralEmail>)caseDetails.getData().get("generalEmailCollection");
        caseDetails.getData();
        assertThat(generalEmailList, hasSize(2));

        GeneralEmail originalEmail = generalEmailList.get(0);
        assertThat(originalEmail.getId(), notNullValue());
        assertThat(originalEmail.getGeneralEmailData().getGeneralEmailRecipientAddress(), is("a1@a.com"));
        assertThat(originalEmail.getGeneralEmailData().getGeneralEmailCreatedByName(), is("first user"));
        assertThat(originalEmail.getGeneralEmailData().getGeneralEmailBody(), is("original email body"));

        GeneralEmail addedEmail = generalEmailList.get(1);
        assertThat(addedEmail.getId(), notNullValue());
        assertThat(addedEmail.getGeneralEmailData().getGeneralEmailRecipientAddress(), is("b1@b.com"));
        assertThat(addedEmail.getGeneralEmailData().getGeneralEmailCreatedByName(), is("Test user"));
        assertThat(addedEmail.getGeneralEmailData().getGeneralEmailBody(), is("Test email body"));
    }

    @Test
    public void generateGeneralEmailContested() throws Exception {
        CaseDetails caseDetails = generalEmailService.storeGeneralEmail(caseDetailsContested());
        List<GeneralEmail> generalEmailList = (List<GeneralEmail>)caseDetails.getData().get("generalEmailCollection");
        caseDetails.getData();
        assertThat(generalEmailList, hasSize(2));

        GeneralEmail originalEmail = generalEmailList.get(0);
        assertThat(originalEmail.getId(), notNullValue());
        assertThat(originalEmail.getGeneralEmailData().getGeneralEmailRecipientAddress(), is("a1@a.com"));
        assertThat(originalEmail.getGeneralEmailData().getGeneralEmailCreatedByName(), is("first user"));
        assertThat(originalEmail.getGeneralEmailData().getGeneralEmailBody(), is("original email body"));

        GeneralEmail addedEmail = generalEmailList.get(1);
        assertThat(addedEmail.getId(), notNullValue());
        assertThat(addedEmail.getGeneralEmailData().getGeneralEmailRecipientAddress(), is("b1@b.com"));
        assertThat(addedEmail.getGeneralEmailData().getGeneralEmailCreatedByName(), is("Test user"));
        assertThat(addedEmail.getGeneralEmailData().getGeneralEmailBody(), is("Test email body"));
    }

    private CaseDetails caseDetailsConsented() throws Exception {
        try (InputStream resourceAsStream = getClass().getResourceAsStream("/fixtures/general-email-consented.json")) {
            return mapper.readValue(resourceAsStream, CallbackRequest.class).getCaseDetails();
        }
    }

    private CaseDetails caseDetailsContested() throws Exception {
        try (InputStream resourceAsStream = getClass().getResourceAsStream("/fixtures/contested/general-email-contested.json")) {
            return mapper.readValue(resourceAsStream, CallbackRequest.class).getCaseDetails();
        }
    }


}
