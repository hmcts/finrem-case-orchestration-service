package uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.wrapper.IdamToken;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamAuthService;

import java.util.UUID;


@Service
@RequiredArgsConstructor
@Slf4j
public class EvidenceManagementDeleteService {

    private static final int DOC_UUID_LENGTH = 36;
    private final CaseDocumentClient caseDocumentClient;
    private final IdamAuthService idamAuthService;

    public void delete(String fileUrl, String auth) throws HttpClientErrorException {
        IdamToken idamTokens = idamAuthService.getIdamToken(auth);
        log.info("EMSDocStore Delete file: {} with user: {} and docId: {}",
            fileUrl, idamTokens.getEmail(), getDocumentIdFromFileUrl(fileUrl));

        caseDocumentClient.deleteDocument(idamTokens.getIdamOauth2Token(), idamTokens.getServiceAuthorization(),
            getDocumentIdFromFileUrl(fileUrl), Boolean.TRUE);
    }

    private UUID getDocumentIdFromFileUrl(String fileUrl) {
        return UUID.fromString(fileUrl.substring(fileUrl.length() - DOC_UUID_LENGTH));
    }
}