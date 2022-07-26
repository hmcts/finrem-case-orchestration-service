package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import org.junit.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.evidence.FileUploadResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.EvidenceManagementUploadService;

import java.util.Collections;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EvidenceManagementClientController.class)
public class EvidenceManagementClientControllerTest extends BaseControllerTest {

    private static final String UPLOADED_FILE_URL = "http://localhost:8080/documents/6";
    private static final String AUTH_TOKEN = "AAAAAAA";
    private static final String REQUEST_ID = "1234";
    private static final String AUTHORIZATION_TOKEN_HEADER = "Authorization";
    private static final String REQUEST_ID_HEADER = "requestId";
    private static final String CONTENT_TYPE_HEADER = "content-type";
    private List<MultipartFile> multipartFileList;
    private static final String EM_CLIENT_UPLOAD_URL = "http://localhost/case-orchestration/emclientapi/upload";

    @MockBean private EvidenceManagementUploadService emUploadService;

    @Test
    public void shouldUploadFileTokenWhenHandleFileUploadIsInvokedWithValidInputs() throws Exception {
        MockMultipartFile file = jpegMultipartFile();
        multipartFileList = Collections.singletonList(file);
        given(emUploadService.upload(any(), any(), any()))
            .willReturn(prepareFileUploadResponse());

        mvc.perform(multipart(EM_CLIENT_UPLOAD_URL)
                .file(file)
                .header(AUTHORIZATION_TOKEN_HEADER, AUTH_TOKEN)
                .header(REQUEST_ID_HEADER, REQUEST_ID)
                .header(CONTENT_TYPE_HEADER, MediaType.MULTIPART_FORM_DATA))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].fileUrl", is("http://localhost:8080/documents/6")))
            .andExpect(jsonPath("$[0].fileName", is("test.txt")))
            .andExpect(jsonPath("$[0].createdBy", is("testuser")))
            .andExpect(jsonPath("$[0].createdOn", is("2017-09-01T13:12:36.862")))
            .andExpect(jsonPath("$[0].lastModifiedBy", is("testuser")))
            .andExpect(jsonPath("$[0].modifiedOn", is("2017-09-01T13:12:36.862")))
            .andExpect(jsonPath("$[0].mimeType", is(MediaType.TEXT_PLAIN_VALUE)))
            .andExpect(jsonPath("$[0].status", is("OK")));

        verify(emUploadService).upload(multipartFileList, AUTH_TOKEN, REQUEST_ID);
    }

    private List<FileUploadResponse> prepareFileUploadResponse() {
        FileUploadResponse fileUploadResponse;
        fileUploadResponse = FileUploadResponse.builder().status(HttpStatus.OK)
            .fileUrl(UPLOADED_FILE_URL)
            .fileName("test.txt")
            .createdBy("testuser")
            .createdOn("2017-09-01T13:12:36.862")
            .modifiedOn("2017-09-01T13:12:36.862")
            .lastModifiedBy("testuser")
            .mimeType(MediaType.TEXT_PLAIN_VALUE).build();

        return singletonList(fileUploadResponse);
    }

    private MockMultipartFile textMultipartFile() {
        return new MockMultipartFile("file", "test.txt", "multipart/form-data",
            "This is a test file".getBytes());
    }

    private MockMultipartFile jpegMultipartFile() {
        return new MockMultipartFile("file", "image.jpeg", "image/jpeg", new byte[0]);
    }
}
