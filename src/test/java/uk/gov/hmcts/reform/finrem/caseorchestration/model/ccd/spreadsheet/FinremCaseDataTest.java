package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.spreadsheet;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ClevelandCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Region;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RegionNorthEastFrc;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.AllocatedRegionWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DefaultCourtListWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.RegionWrapper;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assume.assumeTrue;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
public class FinremCaseDataTest {

    ClassLoader classLoader = this.getClass().getClassLoader();

    public static String CCD_CONFIG_CONTESTED_XLSX;

    public static String CCD_CONFIG_CONSENTED_XLSX;

    public static final String CCD_CONFIG_PROD_CONTESTED_XLSX = "ccd-config-prod-contested.xlsx";
    public static final String CCD_CONFIG_PROD_CONSENTED_XLSX = "ccd-config-prod-consented.xlsx";
    public static final String  CCD_CONFIG_LOCAL_CONSENTED_XLSX = "ccd-config-local-consented-base.xlsx";
    public static final String  CCD_CONFIG_LOCAL_CONTESTED_XLSX = "ccd-config-local-contested-base.xlsx";
    public static final String DEFINITION_FILES_DEFINITIONS_CONSENTED_XLSX = "./definition_files/definitions/consented/xlsx";
    public static final String DEFINITION_FILES_DEFINITIONS_CONTESTED_XLSX = "./definition_files/definitions/contested/xlsx";

    private String consentedFileNameWithPath = null;
    private String contestedFileNameWithPath = null;
    private boolean localMode = System.getenv("JENKINS_BRANCH") == null;
    private boolean testEnabled = true;

    @Before
    public void setUpDefinitionFiles() {
        log.info("Starting FinremCaseDataTest...");
        String branch = System.getenv("JENKINS_BRANCH");
        if (isMaster(branch)) {
            testEnabled = false;
        }
        if (!localMode) {
            if (branch != null && branch.contains("preview")) {
                CCD_CONFIG_CONSENTED_XLSX = "ccd-config-preview-consented.xlsx";
                CCD_CONFIG_CONTESTED_XLSX = "ccd-config-preview-contested.xlsx";
            } else {
                CCD_CONFIG_CONSENTED_XLSX = CCD_CONFIG_PROD_CONSENTED_XLSX;
                CCD_CONFIG_CONTESTED_XLSX = CCD_CONFIG_PROD_CONTESTED_XLSX;
            }
            consentedFileNameWithPath = retrieveFileName(CCD_CONFIG_CONSENTED_XLSX, DEFINITION_FILES_DEFINITIONS_CONSENTED_XLSX);
            contestedFileNameWithPath = retrieveFileName(CCD_CONFIG_CONTESTED_XLSX, DEFINITION_FILES_DEFINITIONS_CONTESTED_XLSX);
        } else {
            CCD_CONFIG_CONSENTED_XLSX = CCD_CONFIG_LOCAL_CONSENTED_XLSX;
            CCD_CONFIG_CONTESTED_XLSX = CCD_CONFIG_LOCAL_CONTESTED_XLSX;
            consentedFileNameWithPath = retrieveFileName(CCD_CONFIG_CONSENTED_XLSX, "build/definitionsToBeImported");
            contestedFileNameWithPath = retrieveFileName(CCD_CONFIG_CONTESTED_XLSX, "build/definitionsToBeImported");
        }
    }

    private static boolean isMaster(String branch) {
        return branch != null && branch.equals("master");
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
        log.error("contestedFileNameWithPath: {}", contestedFileNameWithPath);
        log.error("CCD_CONFIG_CONTESTED_XLSX: {}", CCD_CONFIG_CONTESTED_XLSX);
        System.out.println("contestedFileNameWithPath: " + contestedFileNameWithPath);
        System.out.println("CCD_CONFIG_CONTESTED_XLSX: " + CCD_CONFIG_CONTESTED_XLSX);
        List<File> configFiles = Arrays.asList(getFile(contestedFileNameWithPath),
            getFile(consentedFileNameWithPath));
        validateConfig(configFiles);
    }

    @Test
    public void testConsentedConfigFinRemCaseData() throws IOException, InvalidFormatException {
        assumeTrue(testEnabled);
        List<File> configFiles = Arrays.asList(getFile(consentedFileNameWithPath),
            getFile(contestedFileNameWithPath));
        validateConfig(configFiles);
    }

    @Test
    public void testConsentedStateData() throws IOException, InvalidFormatException {
        assumeTrue(testEnabled);
        File configFile = getFile(consentedFileNameWithPath);
        validateState(configFile);
    }

    @Test
    public void testContestedStateData() throws IOException, InvalidFormatException {
        assumeTrue(testEnabled);
        File configFile = getFile(contestedFileNameWithPath);
        validateState(configFile);
    }

    @Test
    public void testAllFieldsWithWrapperShouldHaveJsonIncludeNonNull() {
        List<Field> allFields = getAllFields(FinremCaseData.class);
        List<Field> wrapperFields = allFields.stream()
            .filter(field -> field.getName().contains("Wrapper"))
            .filter(field -> !field.getName().contains("uploadCaseDocumentWrapper"))
            .toList();

        for (Field field : wrapperFields) {
            Class<?> fieldType = field.getType();
            boolean hasAnnotation = hasJsonIncludeAnnotation(fieldType);
            String fieldDeclaration = field.toString();
            assertTrue(hasAnnotation,
                "Field '" + fieldDeclaration + "' should have @JsonInclude(JsonInclude.Include.NON_NULL) inside the file");
        }
    }

    @Test
    public void testGetSelectedAllocatedCourt() {
        DefaultCourtListWrapper courtListWrapper = DefaultCourtListWrapper.builder()
            .cleavelandCourtList(ClevelandCourt.FR_CLEVELAND_HC_LIST_1)
            .build();
        FinremCaseData data = FinremCaseData.builder()
            .regionWrapper(RegionWrapper.builder()
                .allocatedRegionWrapper(
                    AllocatedRegionWrapper.builder()
                        .regionList(Region.NORTHEAST)
                        .northEastFrcList(RegionNorthEastFrc.CLEAVELAND)
                        .courtListWrapper(courtListWrapper)
                        .build())
                .build())
            .build();

        assertEquals(ClevelandCourt.FR_CLEVELAND_HC_LIST_1.getSelectedCourtId(), data.getSelectedAllocatedCourt());
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

    private File getFile(String fileNameWithPath) {
        return new File(fileNameWithPath);
    }

    private List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>(Arrays.asList(clazz.getDeclaredFields()));
        Class<?> superclass = clazz.getSuperclass();
        if (superclass != null) {
            fields.addAll(getAllFields(superclass));
        }
        return  fields;
    }

    private boolean hasJsonIncludeAnnotation(Class<?> clazz) {
        Class<?> superClass;
        while ((superClass = clazz.getSuperclass()) != null && !superClass.equals(clazz)) {
            if (clazz.isAnnotationPresent(JsonInclude.class)) {
                JsonInclude jsonIncludeAnnotation = clazz.getAnnotation(JsonInclude.class);
                return  jsonIncludeAnnotation.value() == JsonInclude.Include.NON_NULL;
            }
            clazz = superClass;
        }
        return false;
    }
}

