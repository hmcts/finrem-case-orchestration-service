package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.spreadsheet;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.junit.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Slf4j
//@Ignore
public class FinremCaseDataTest {

    ClassLoader classLoader = this.getClass().getClassLoader();

    @Test
    public void testDefinitionFilesSavedInCorrectLocation() throws IOException {
        Path dirPath = Paths.get("./definition_files").toAbsolutePath();
        File directoryPath = dirPath.toFile();
        String contents[] = directoryPath.list();
        for (int i=0; i<contents.length; i++) {
            System.out.println(contents[i]);
        }
    }

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

    @Test
    public void testConsentedStateData() throws IOException, InvalidFormatException {
        File configFile = new File(classLoader.getResource("ccd-config-prod-consented.xlsx").getFile());
        validateState(configFile);
    }


    @Test
    public void testContestedStateData() throws IOException, InvalidFormatException {
        File configFile = new File(classLoader.getResource("ccd-config-prod-contested.xlsx").getFile());
        validateState(configFile);
    }

    private void validateConfig(File configFile) throws IOException, InvalidFormatException {
        CCDConfigValidator ccdConfigValidator = new CCDConfigValidator();
        List<String> errors = ccdConfigValidator.validateCaseFieldsAgainstClassStructure(configFile, FinremCaseData.class);
        if (!errors.isEmpty()) {
            log.error("Errors found when validating config file: {}", configFile.getName());
            errors.forEach(log::error);
        }
        assert errors.isEmpty();
    }


//  @Test
//  public void testUnzip() throws IOException {
//
//      getFile(
//          "https://build.platform.hmcts.net/view/FinRem/job/HMCTS_d_to_i/job/finrem-ccd-definitions/job/master/lastSuccessfulBuild/artifact/definitions/*zip*/definitions.zip",
//          "defs.zip");
//      unzipFile();
//      assert true;
//  }

    public void unzipFile() throws IOException {

        String fileZip = "defs.zip";
        File destDir = new File("src/main/resources/unzipTest");

        byte[] buffer = new byte[1024];
        ZipInputStream zis = new ZipInputStream(new FileInputStream(fileZip));
        ZipEntry zipEntry = zis.getNextEntry();
        while (zipEntry != null) {
            File newFile = newFile(destDir, zipEntry);
            if (zipEntry.isDirectory()) {
                if (!newFile.isDirectory() && !newFile.mkdirs()) {
                    throw new IOException("Failed to create directory " + newFile);
                }
            } else {
                // fix for Windows-created archives
                File parent = newFile.getParentFile();
                if (!parent.isDirectory() && !parent.mkdirs()) {
                    throw new IOException("Failed to create directory " + parent);
                }

                // write file content
                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
            }
            zipEntry = zis.getNextEntry();
        }

        zis.closeEntry();
        zis.close();

    }

    public static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }

    public void getFile(String fileUrl, String fileName) throws IOException {
        URL url = new URL(fileUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        InputStream in = connection.getInputStream();
        ZipInputStream zipIn = new ZipInputStream(in);
        ZipEntry entry = zipIn.getNextEntry();

        while (entry != null) {

            System.out.println(entry.getName());
            if (!entry.isDirectory()) {
                // if the entry is a file, extracts it
                System.out.println("===File===");

            } else {
                System.out.println("===Directory===");
            }
            zipIn.closeEntry();
            entry = zipIn.getNextEntry();

        }
    }

    private void validateState(File configFile) throws IOException, InvalidFormatException {
        CCDConfigValidator ccdConfigValidator = new CCDConfigValidator();
        List<String> errors = ccdConfigValidator.validateStatesAgainstClassStructure(configFile);
        if (!errors.isEmpty()) {
            log.error("Errors found when validating state: {}", configFile.getName());
            errors.forEach(log::error);
        }
        assert errors.isEmpty();
    }
}



