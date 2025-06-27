package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.spreadsheet;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ClevelandCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Region;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RegionNorthEastFrc;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.AllocatedRegionWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
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

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@Slf4j
class FinremCaseDataTest {

    static final String CCD_CONFIG_AAT_CONTESTED_XLSX = "ccd-config-aat-contested";
    static final String CCD_CONFIG_AAT_CONSENTED_XLSX = "ccd-config-aat-consented";

    static final String CCD_CONFIG_PREVIEW_CONTESTED_XLSX = "ccd-config-preview-contested";
    static final String CCD_CONFIG_PREVIEW_CONSENTED_XLSX = "ccd-config-preview-consented";
    static final String CCD_CONFIG_PROD_CONTESTED_XLSX = "ccd-config-prod-contested.xlsx";
    static final String CCD_CONFIG_PROD_CONSENTED_XLSX = "ccd-config-prod-consented.xlsx";
    static final String CCD_CONFIG_LOCAL_CONSENTED_XLSX = "ccd-config-local-consented-base.xlsx";
    static final String CCD_CONFIG_LOCAL_CONTESTED_XLSX = "ccd-config-local-contested-base.xlsx";
    static final String DEFINITION_FILES_DEFINITIONS_CONSENTED_XLSX = "./definition_files/definitions/consented/xlsx";
    static final String DEFINITION_FILES_DEFINITIONS_CONTESTED_XLSX = "./definition_files/definitions/contested/xlsx";

    String consentedFileNameWithPath = null;
    String contestedFileNameWithPath = null;
    final boolean localMode = System.getenv("JENKINS_BRANCH") == null;
    boolean testEnabled = true;

    @BeforeEach
    void setUpDefinitionFiles() {
        String branch = System.getenv("JENKINS_BRANCH");
        if (isMaster(branch)) {
            testEnabled = false;
        }
        if (!localMode) {
            consentedFileNameWithPath = retrieveFirstAvailableFile(
                new String[] {
                    CCD_CONFIG_AAT_CONSENTED_XLSX,
                    CCD_CONFIG_PREVIEW_CONSENTED_XLSX,
                    CCD_CONFIG_PROD_CONSENTED_XLSX
                },
                DEFINITION_FILES_DEFINITIONS_CONSENTED_XLSX
            );

            contestedFileNameWithPath = retrieveFirstAvailableFile(
                new String[] {
                    CCD_CONFIG_AAT_CONTESTED_XLSX,
                    CCD_CONFIG_PREVIEW_CONTESTED_XLSX,
                    CCD_CONFIG_PROD_CONTESTED_XLSX
                },
                DEFINITION_FILES_DEFINITIONS_CONTESTED_XLSX
            );
        } else {
            String localPath = "build/definitionsToBeImported";
            consentedFileNameWithPath = retrieveFileName(CCD_CONFIG_LOCAL_CONSENTED_XLSX, localPath);
            contestedFileNameWithPath = retrieveFileName(CCD_CONFIG_LOCAL_CONTESTED_XLSX, localPath);
        }
    }

    private String retrieveFirstAvailableFile(String[] configNames, String path) {
        for (String config : configNames) {
            String result = retrieveFileName(config, path);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    private static boolean isMaster(String branch) {
        return branch != null && branch.equals("master");
    }

    private String retrieveFileName(String filePrefix, String filePath) {
        Path dirPath = Paths.get(filePath).toAbsolutePath();
        File directoryPath = dirPath.toFile();
        String[] list = directoryPath.list();
        assertNotNull(list);
        for (String s : list) {
            if (s.startsWith(filePrefix)) {
                return filePath + "/" + s;
            }
        }
        return null;
    }

    @Test
    void testContestedConfigFinRemCaseData() throws IOException, InvalidFormatException {
        assumeTrue(testEnabled);
        List<File> configFiles = Arrays.asList(getFile(contestedFileNameWithPath),
            getFile(consentedFileNameWithPath));
        validateConfig(configFiles);
    }

    @Test
    void testConsentedConfigFinRemCaseData() throws IOException, InvalidFormatException {
        assumeTrue(testEnabled);
        List<File> configFiles = Arrays.asList(getFile(consentedFileNameWithPath),
            getFile(contestedFileNameWithPath));
        validateConfig(configFiles);
    }

    @Test
    void testConsentedStateData() throws IOException, InvalidFormatException {
        assumeTrue(testEnabled);
        File configFile = getFile(consentedFileNameWithPath);
        validateState(configFile);
    }

    @Test
    void testContestedStateData() throws IOException, InvalidFormatException {
        assumeTrue(testEnabled);
        File configFile = getFile(contestedFileNameWithPath);
        validateState(configFile);
    }

    @Test
    void testAllFieldsWithWrapperShouldHaveJsonIncludeNonNull() {
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
    void testGetSelectedAllocatedCourt() {
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

    /**
     * Checks that getRespondentSolicitorEmailForContested get the email correctly.
     * Checks that a blank email string us returned when no email is set.
     */
    @Test
    void testGetRespondentSolicitorEmail() {

        FinremCaseData finremCaseData = new FinremCaseData();
        assertThat(finremCaseData.getRespondentSolicitorEmailForContested()).isEqualTo("");

        ContactDetailsWrapper wrapper = ContactDetailsWrapper.builder().respondentSolicitorEmail("respondent@testemail.com").build();
        finremCaseData.setContactDetailsWrapper(wrapper);
        assertThat(finremCaseData.getRespondentSolicitorEmailForContested()).isEqualTo("respondent@testemail.com");
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
