package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.spreadsheet;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Slf4j
//@Ignore
public class FinremCaseDataTest {

    ClassLoader classLoader = this.getClass().getClassLoader();
    public static final String DEFINITION_FILES_DEFINITIONS_CONSENTED_XLSX = "./definition_files/definitions/consented/xlsx";
    public static final String DEFINITION_FILES_DEFINITIONS_CONTESTED_XLSX = "./definition_files/definitions/contested/xlsx";
    private String consentedFileNameWithPath = null;
    private String contestedFileNameWithPath = null;
    private Boolean localMode = false;

    @Before
    public void setUpDefinitionFiles() {
        if (localMode.booleanValue() == Boolean.FALSE) {
            consentedFileNameWithPath = retrieveFileName("ccd-config-prod-consented", DEFINITION_FILES_DEFINITIONS_CONSENTED_XLSX);
            if (consentedFileNameWithPath == null) {
                consentedFileNameWithPath = retrieveFileName("ccd-config-preview-consented", DEFINITION_FILES_DEFINITIONS_CONSENTED_XLSX);
            }
            contestedFileNameWithPath = retrieveFileName("ccd-config-prod-contested", DEFINITION_FILES_DEFINITIONS_CONTESTED_XLSX);
            if (contestedFileNameWithPath == null) {
                contestedFileNameWithPath = retrieveFileName("ccd-config-preview-contested", DEFINITION_FILES_DEFINITIONS_CONTESTED_XLSX);
            }
            log.info("consentedFileNameWithPath : {}", consentedFileNameWithPath);
            log.info("contestedFileNameWithPath : {}", contestedFileNameWithPath);
        }

    }

    private String retrieveFileName(String filePrefix, String filePath) {
        Path dirPath = Paths.get(filePath).toAbsolutePath();
        File directoryPath = dirPath.toFile();
        String[] list = directoryPath.list();
        assert list != null;
        for (String fileName : list) {
            if (fileName.startsWith(filePrefix)) {
                return "%s/%s".formatted(filePath, fileName);
            }
        }
        return null;
    }

    @Test
    public void testContestedConfigFinRemCaseData() throws IOException, InvalidFormatException {
        File configFile = getFile("ccd-config-prod-contested.xlsx", contestedFileNameWithPath);
        validateConfig(configFile);
    }

    @Test
    public void testConsentedConfigFinRemCaseData() throws IOException, InvalidFormatException {
        File configFile = getFile("ccd-config-prod-consented.xlsx", consentedFileNameWithPath);
        validateConfig(configFile);
    }

    @Test
    public void testConsentedStateData() throws IOException, InvalidFormatException {
        File configFile = getFile("ccd-config-prod-consented.xlsx", consentedFileNameWithPath);
        validateState(configFile);
    }


    @Test
    public void testContestedStateData() throws IOException, InvalidFormatException {
        File configFile = getFile("ccd-config-prod-contested.xlsx", contestedFileNameWithPath);
        validateState(configFile);
    }

    private void validateConfig(File configFile) throws IOException, InvalidFormatException {
        CCDConfigValidator ccdConfigValidator = new CCDConfigValidator();
        List<String> errors = ccdConfigValidator.validateCaseFields(configFile, FinremCaseData.class);
        if (!errors.isEmpty()) {
            log.error("Errors found when validating config file: {}", configFile.getName());
            errors.forEach(log::error);
        }
        assert errors.isEmpty();
    }

    private void validateState(File configFile) throws IOException, InvalidFormatException {
        CCDConfigValidator ccdConfigValidator = new CCDConfigValidator();
        List<String> errors = ccdConfigValidator.validateStates(configFile);
        if (!errors.isEmpty()) {
            log.error("Errors found when validating state: {}", configFile.getName());
            errors.forEach(log::error);
        }
        assert errors.isEmpty();
    }

    private File getFile(String name, String fileNameWithPath) {
        File configFile = localMode ? new File(classLoader.getResource(name).getFile())
            : new File(fileNameWithPath);
        return configFile;
    }
}

