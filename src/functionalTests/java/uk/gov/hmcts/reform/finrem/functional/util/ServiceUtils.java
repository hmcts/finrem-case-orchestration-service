package uk.gov.hmcts.reform.finrem.functional.util;

import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.evidence.FileUploadResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.EvidenceManagementUploadService;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.DOCUMENT_BINARY_URL;

@Component
public class ServiceUtils {

    @Autowired
    private EvidenceManagementUploadService evidenceManagementUploadService;

    @Autowired
    private FunctionalTestUtils functionalTestUtils;

    public Map<String, String> uploadFileToEmStore(String fileToUpload, String fileContentType) throws JSONException {
        File file = null;

        try {
            file = Paths.get(Objects.requireNonNull(getClass()
                .getClassLoader()
                .getResource(fileToUpload))
                .toURI())
                .toFile();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        MultipartFile multipartFile = new MockMultipartFile(fileToUpload, fileToUpload,
            fileContentType, file.getPath().getBytes());
        List<FileUploadResponse> fileUploadResponse =
            evidenceManagementUploadService.upload(Collections.singletonList(multipartFile),
                functionalTestUtils.getAuthToken(), "");

        Map<String, String> uploadedDocument = new HashMap<>();
        uploadedDocument.put("document_url", fileUploadResponse.get(0).getFileUrl());
        uploadedDocument.put("document_filename",fileUploadResponse.get(0).getFileName());
        uploadedDocument.put(DOCUMENT_BINARY_URL, fileUploadResponse.get(0).getFileUrl() + "/binary");

        return uploadedDocument;
    }
}
