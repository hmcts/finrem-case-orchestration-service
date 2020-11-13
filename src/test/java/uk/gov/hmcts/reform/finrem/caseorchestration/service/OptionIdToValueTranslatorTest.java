package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.CaseOrchestrationApplication;

import java.io.File;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

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
        CaseDetails actual = caseDetailsWithOptions();
        translator.translateOptionsValues.accept(actual);

        assertThat(actual, is(equalTo(expectedCaseDetails())));
    }

    @Test
    public void nothingToTranslate() {
        CaseDetails actual = caseDetailsWithEmptyOptions();
        translator.translateOptionsValues.accept(actual);

        assertThat(actual, is(equalTo(caseDetailsWithEmptyOptions())));
    }

    @Test(expected = IllegalStateException.class)
    public void jsonFileNotFound() {
        new OptionIdToValueTranslator("random.json", new ObjectMapper()).initOptionValueMap();
    }

    private CaseDetails caseDetailsWithEmptyOptions() {
        return CaseDetails.builder().data(ImmutableMap.of()).build();
    }

    private CaseDetails expectedCaseDetails() throws Exception {
        File file = new File(getClass().getResource("/fixtures/contested/expected-contested-case-details.json").toURI());
        return objectMapper.readValue(file, CaseDetails.class);
    }

    private CaseDetails caseDetailsWithOptions() throws Exception {
        File file = new File(getClass().getResource("/fixtures/contested/contested-case-details-options-list.json").toURI());
        return objectMapper.readValue(file, CaseDetails.class);
    }
}