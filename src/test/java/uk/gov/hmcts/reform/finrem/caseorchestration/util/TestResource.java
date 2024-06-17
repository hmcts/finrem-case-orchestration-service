package uk.gov.hmcts.reform.finrem.caseorchestration.util;

import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.Document;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.evidence.FileUploadResponse;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

import static java.lang.String.format;

public class TestResource {

    public static final String FILE_URL = "http://dm-store:8080/documents/d607c045-878e-475f-ab8e-b2f667d8af64";
    public static final String BINARY_URL = format("%s/binary", FILE_URL);
    public static final String FILE_NAME = "app_docs.pdf";
    public static final String CREATED_ON = "20th October 2018";
    public static final String MIME_TYPE = "app/pdf";
    public static final String CREATED_BY = "user";
    public static final String CASE_ID = "1234567890";
    public static final String AUTH_TOKEN = "token:)";

    public static List<FileUploadResponse> fileUploadResponse() {
        FileUploadResponse response = new FileUploadResponse();
        response.setStatus(HttpStatus.OK);
        response.setFileUrl(FILE_URL);
        response.setFileName(FILE_NAME);
        response.setMimeType(MIME_TYPE);
        response.setCreatedOn(CREATED_ON);
        response.setLastModifiedBy(CREATED_BY);
        response.setModifiedOn(CREATED_ON);
        response.setCreatedBy(CREATED_BY);
        return Collections.singletonList(response);
    }

    public static Document testDocument() {
        return Document.builder()
            .url(FILE_URL)
            .fileName(FILE_NAME)
            .binaryUrl(BINARY_URL)
            .build();
    }

    public static FinremCaseDetails.FinremCaseDetailsBuilder getContestedFinremCaseDetailsBuilder(
        FinremCaseData.FinremCaseDataBuilder finremCaseDataBuilder) {
        return getFinremCaseDetailsBuilder(finremCaseDataBuilder, CaseType.CONTESTED);
    }

    public static FinremCaseDetails.FinremCaseDetailsBuilder getConsentedFinremCaseDetailsBuilder(
        FinremCaseData.FinremCaseDataBuilder finremCaseDataBuilder) {
        return getFinremCaseDetailsBuilder(finremCaseDataBuilder, CaseType.CONSENTED);
    }

    private static FinremCaseDetails.FinremCaseDetailsBuilder getFinremCaseDetailsBuilder(FinremCaseData.FinremCaseDataBuilder finremCaseDataBuilder,
                                                                                          CaseType caseType) {
        FinremCaseData finremCaseData = finremCaseDataBuilder.build();
        return FinremCaseDetails.builder().id(Long.valueOf(CASE_ID))
            .caseType(caseType)
            .data(finremCaseData);
    }

    public static CaseDocument buildCaseDocument() {
        return buildCaseDocument("url", "binaryUrl", "filename");
    }

    public static CaseDocument buildCaseDocument(String url, String binaryUrl, String filename) {
        return buildCaseDocument(url, binaryUrl, filename, null);
    }

    public static CaseDocument buildCaseDocument(String url, String binaryUrl, String filename, String categoryId) {
        CaseDocument document = new CaseDocument();
        document.setDocumentUrl(url);
        document.setDocumentBinaryUrl(binaryUrl);
        document.setDocumentFilename(filename);
        document.setCategoryId(categoryId);
        return document;
    }

    public static Path getPathFromResources(String fileName) {
        return Paths.get("src/test/resources/" + fileName);
    }
}
