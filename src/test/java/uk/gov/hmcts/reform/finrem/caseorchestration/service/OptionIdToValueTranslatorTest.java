package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.CaseOrchestrationApplication;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FixedListOption;

import java.io.File;
import java.net.URISyntaxException;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = CaseOrchestrationApplication.class)
@TestPropertySource(locations = "/application.properties")
public class OptionIdToValueTranslatorTest {

    @Autowired
    private OptionIdToValueTranslator translator;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void translateOptions() throws Exception {
        translator.translateFixedListOptions(caseDetails());
    }

    private String jsonFixture() {
        return "/fixtures/contested/contested-case-details.json";
    }

    private CaseDetails caseDetails() throws Exception {
        File file = new File(getClass().getResource(jsonFixture()).toURI());
        return objectMapper.readValue(file, CaseDetails.class);
    }
}