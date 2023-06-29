package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.FinremMultipartFile;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.Document;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.evidence.FileUploadResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement.EvidenceManagementDeleteService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement.EvidenceManagementUploadService;

import java.util.Collections;
import java.util.Map;
import java.util.function.Function;

import static java.lang.String.format;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentManagementService {

    public static final String CONTENT_TYPE_APPLICATION_PDF = "application/pdf";
    private final DocmosisPdfGenerationService pdfGenerationService;
    private final EvidenceManagementUploadService evidenceManagementUploadService;

    private final EvidenceManagementDeleteService evidenceManagementDeleteService;
    public static final Function<FileUploadResponse, Document> CONVERTER = (response -> Document.builder()
        .fileName(response.getFileName())
        .url(response.getFileUrl())
        .binaryUrl(toBinaryUrl(response))
        .build());

    public byte[] generateDocumentFrom(String templateName, Map<String, Object> placeholders) {
        return pdfGenerationService.generateDocFrom(templateName, placeholders);
    }

    public void deleteDocument(String fileUrl, String authToken) {
        evidenceManagementDeleteService.delete(fileUrl, authToken);
    }

    public Document storeDocument(String templateName,
                                  String fileName,
                                  Map<String, Object> placeholders,
                                  String authorizationToken, String caseId) {
        log.info("Generate and Store Document requested with templateName [{}], placeholders of size [{}]",
            templateName, placeholders.size());

        return storeDocument(
            generateDocumentFrom(templateName, placeholders),
            fileName, authorizationToken, caseId);
    }

    public Document storeDocument(byte[] document, String fileName, String authorizationToken, String caseId) {
        log.info("Store document requested with document of size [{}]", document.length);

        FinremMultipartFile multipartFile = FinremMultipartFile.builder()
            .content(document).name(fileName).contentType(CONTENT_TYPE_APPLICATION_PDF).build();
        FileUploadResponse response = evidenceManagementUploadService
            .upload(Collections.singletonList(multipartFile), caseId, authorizationToken).get(0);

        return CONVERTER.apply(response);
    }

    private static String toBinaryUrl(FileUploadResponse response) {
        return format("%s/binary", response.getFileUrl());
    }
}
