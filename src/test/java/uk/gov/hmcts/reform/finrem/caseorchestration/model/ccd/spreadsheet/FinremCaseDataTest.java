package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.spreadsheet;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.junit.Ignore;
import org.junit.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Slf4j
@Ignore
public class FinremCaseDataTest {

    ClassLoader classLoader = this.getClass().getClassLoader();

    @Test
    public void testContestedConfigFinRemCaseData() throws IOException, InvalidFormatException {
        File configFile = new File(classLoader.getResource("ccd-config-prod-contested.xlsx").getFile());
        validateConfig(configFile);
    }

    @Test
    public void testConsentedConfigFinRemCaseData() throws IOException, InvalidFormatException {
        File configFile = new File(classLoader.getResource("ccd-config-prod-consented.xlsx").getFile());
        validateConfig(configFile);
    }

    private void validateConfig(File configFile) throws IOException, InvalidFormatException {
        CCDConfigValidator ccdConfigValidator = new CCDConfigValidator();
        List<String> errors = ccdConfigValidator.validateCCDConfigAgainstClassStructure(configFile, FinremCaseData.class);
        if (!errors.isEmpty()) {
            log.error("Errors found when validating config file: {}", configFile.getName());
            errors.forEach(log::error);
        }
        assert errors.isEmpty();
    }
}


