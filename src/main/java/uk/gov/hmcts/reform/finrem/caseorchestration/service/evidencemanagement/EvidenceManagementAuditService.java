package uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;
import uk.gov.hmcts.reform.ccd.document.am.model.Document;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.evidence.FileUploadResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamAuthService;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class EvidenceManagementAuditService {

    public static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";
    private final IdamAuthService idamAuthService;
    private final CaseDocumentClient caseDocumentClient;

    public List<FileUploadResponse> audit(List<String> fileUrls, String auth) {
        log.info("Audit requested for documents: fileUrls={}", fileUrls);

        String idamToken = idamAuthService.getIdamToken(auth).getIdamOauth2Token();
        String serviceToken = idamAuthService.getIdamToken(auth).getServiceAuthorization();

        List<FileUploadResponse> filesAuditDetails = new ArrayList<>();
        fileUrls.forEach(fileUrl -> {
            Document document = caseDocumentClient.getMetadataForDocument(
                idamToken,
                serviceToken,
                fileUrl);

            FileUploadResponse fileAuditResponse = createUploadResponse(document);
            filesAuditDetails.add(fileAuditResponse);

            log.info("Audit for {}: {}", fileUrl, fileAuditResponse);
        });

        return filesAuditDetails;
    }

    private FileUploadResponse createUploadResponse(Document document) {
        return FileUploadResponse.builder()
            .status(HttpStatus.OK)
            .fileUrl(document.links.self.href)
            .fileName(document.originalDocumentName)
            .createdBy(document.createdBy)
            .createdOn(DateFormatUtils.format(document.createdOn, DATE_FORMAT))
            .lastModifiedBy(document.lastModifiedBy)
            .modifiedOn(DateFormatUtils.format(document.modifiedOn, DATE_FORMAT))
            .mimeType(document.mimeType)
            .build();
    }
}
