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
import java.util.Arrays;
import java.util.List;
import static org.junit.Assume.assumeTrue;

@Slf4j
public class FinremCaseDataTest {

    ClassLoader classLoader = this.getClass().getClassLoader();

    public static final String CCD_CONFIG_PROD_CONTESTED_XLSX = "ccd-config-prod-contested.xlsx";
    public static final String CCD_CONFIG_PROD_CONSENTED_XLSX = "ccd-config-prod-consented.xlsx";
    public static final String DEFINITION_FILES_DEFINITIONS_CONSENTED_XLSX = "./definition_files/definitions/consented/xlsx";
    public static final String DEFINITION_FILES_DEFINITIONS_CONTESTED_XLSX = "./definition_files/definitions/contested/xlsx";

    private String consentedFileNameWithPath = null;
    private String contestedFileNameWithPath = null;
    private boolean localMode = System.getenv("JENKINS_BRANCH") == null;
    private boolean testEnabled = true;

    @Before
    public void setUpDefinitionFiles() {
        String branch = System.getenv("JENKINS_BRANCH");
        if (branch != null && branch.equals("master")) {
            testEnabled = false;
        }
        if (localMode == false) {
            consentedFileNameWithPath = retrieveFileName("ccd-config-prod-consented", DEFINITION_FILES_DEFINITIONS_CONSENTED_XLSX);
            if (consentedFileNameWithPath == null) {
                consentedFileNameWithPath = retrieveFileName("ccd-config-preview-consented", DEFINITION_FILES_DEFINITIONS_CONSENTED_XLSX);
            }
            contestedFileNameWithPath = retrieveFileName("ccd-config-prod-contested", DEFINITION_FILES_DEFINITIONS_CONTESTED_XLSX);
            if (contestedFileNameWithPath == null) {
                contestedFileNameWithPath = retrieveFileName("ccd-config-preview-contested", DEFINITION_FILES_DEFINITIONS_CONTESTED_XLSX);
            }
        }
    }

    private String retrieveFileName(String filePrefix, String filePath) {
        Path dirPath = Paths.get(filePath).toAbsolutePath();
        File directoryPath = dirPath.toFile();
        String[] list = directoryPath.list();
        for (int i = 0; i < list.length; i++) {
            if (list[i].startsWith(filePrefix)) {
                return filePath + "/" + list[i];
            }
        }
        return null;
    }

    @Test
    public void testContestedConfigFinRemCaseData() throws IOException, InvalidFormatException {
        assumeTrue(testEnabled);
        List<File> configFiles = Arrays.asList(getFile(CCD_CONFIG_PROD_CONTESTED_XLSX, contestedFileNameWithPath),
            getFile(CCD_CONFIG_PROD_CONSENTED_XLSX, consentedFileNameWithPath));
        validateConfig(configFiles);
    }

    @Test
    public void testConsentedConfigFinRemCaseData() throws IOException, InvalidFormatException {
        assumeTrue(testEnabled);
        List<File> configFiles = Arrays.asList(getFile(CCD_CONFIG_PROD_CONSENTED_XLSX, consentedFileNameWithPath),
            getFile(CCD_CONFIG_PROD_CONTESTED_XLSX, contestedFileNameWithPath));
        validateConfig(configFiles);
    }

    @Test
    public void testConsentedStateData() throws IOException, InvalidFormatException {
        assumeTrue(testEnabled);
        File configFile = getFile(CCD_CONFIG_PROD_CONSENTED_XLSX, consentedFileNameWithPath);
        validateState(configFile);
    }

    @Test
    public void testContestedStateData() throws IOException, InvalidFormatException {
        assumeTrue(testEnabled);
        File configFile = getFile(CCD_CONFIG_PROD_CONTESTED_XLSX, contestedFileNameWithPath);
        validateState(configFile);
    }

    private void validateConfig(List<File> configFiles) throws IOException, InvalidFormatException {
        CCDConfigValidator ccdConfigValidator = new CCDConfigValidator();
        List<String> errors = ccdConfigValidator.validateCaseFields(configFiles, FinremCaseData.class);
        if (!errors.isEmpty()) {
            log.error("Errors found when validating config files: %s and %s".formatted(configFiles.get(0).getName(), configFiles.get(1).getName()));
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

