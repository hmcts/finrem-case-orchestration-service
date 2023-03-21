package uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.wrapper.IdamToken;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamAuthService;

import java.net.URI;


@Service
@RequiredArgsConstructor
@Slf4j
public class EvidenceManagementDownloadService {
    private final IdamAuthService idamAuthService;
    private final CaseDocumentClient caseDocumentClient;

    public byte[] download(String binaryFileUrl, String auth) throws HttpClientErrorException {
        ResponseEntity<Resource> responseEntity = downloadResource(binaryFileUrl, idamAuthService.getIdamToken(auth));
        ByteArrayResource resource = (ByteArrayResource) responseEntity.getBody();

        return (resource != null) ? resource.getByteArray() : new byte[0];
    }

    private ResponseEntity<Resource> downloadResource(String binaryFileUrl, IdamToken idamTokens) {
        String documentHref = URI.create(binaryFileUrl).getPath().replaceFirst("/", "");
        log.info("EMSDocStore Download file: {} with user: {}", documentHref, idamTokens.getEmail());

        return caseDocumentClient.getDocumentBinary(idamTokens.getIdamOauth2Token(),
            idamTokens.getServiceAuthorization(), documentHref);
    }
}
