package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.UploadCaseFilesAboutToSubmitHandler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@WebMvcTest(UploadContestedCaseDocumentController.class)
public class UploadContestedCaseDocumentControllerTest extends BaseControllerTest {

    @Autowired
    private UploadContestedCaseDocumentController controller;

    @MockBean
    private UploadCaseFilesAboutToSubmitHandler handler;

    @Test
    public void controllerShouldFilterDocumentsByParty() {
        controller.uploadCaseDocuments(buildCallbackRequest());

        verify(handler).handle(any());
    }
}