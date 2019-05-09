package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseTest;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

abstract class BaseControllerTest extends BaseTest {

    @Autowired
    protected WebApplicationContext applicationContext;

    protected MockMvc mvc;

    protected JsonNode requestContent;

    @Before
    public void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(applicationContext).build();
    }


    void doEmtpyCaseDataSetUp() throws IOException, URISyntaxException {
        ObjectMapper objectMapper = new ObjectMapper();
        requestContent = objectMapper.readTree(new File(getClass()
                .getResource("/fixtures/empty-casedata.json").toURI()));
    }

    void doValidCaseDataSetUp() throws IOException, URISyntaxException {
        ObjectMapper objectMapper = new ObjectMapper();
        requestContent = objectMapper.readTree(new File(getClass()
                .getResource("/fixtures/pba-validate.json").toURI()));
    }
}
