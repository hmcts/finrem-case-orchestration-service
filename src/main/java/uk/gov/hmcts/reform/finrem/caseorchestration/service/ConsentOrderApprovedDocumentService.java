package uk.gov.hmcts.reform.finrem.caseorchestration.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.DocumentClient;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.PensionDocumentData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.Document;

import java.io.IOException;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
@Slf4j
public class ConsentOrderApprovedDocumentService extends AbstractDocumentService {

    @Autowired
    public ConsentOrderApprovedDocumentService(DocumentClient documentClient,
                                               DocumentConfiguration config,
                                               ObjectMapper objectMapper) {
        super(documentClient, config, objectMapper);
    }


    public CaseDocument generateApprovedConsentOrderLetter(CaseDetails caseDetails, String authToken) {
        return generateDocument(authToken, caseDetails,
                config.getApprovedConsentOrderTemplate(),
                config.getApprovedConsentOrderFileName());
    }

    public CaseDocument annexStampDocument(CaseDocument document, String authToken) {
        return super.annexStampDocument(document, authToken);
    }

    public List<PensionDocumentData> stampPensionDocuments(List<PensionDocumentData> pensionList, String authToken) {
        return pensionList.stream()
                .map(data -> stampPensionDocuments(data, authToken)).collect(toList());
    }

    private PensionDocumentData stampPensionDocuments(PensionDocumentData pensionDocument, String authToken) {
        CaseDocument document = pensionDocument.getPensionDocument().getDocument();
        CaseDocument stampedDocument = stampDocument(document, authToken);
        PensionDocumentData stampedPensionData = copyOf(pensionDocument);
        stampedPensionData.getPensionDocument().setDocument(stampedDocument);
        return stampedPensionData;
    }

    private PensionDocumentData copyOf(PensionDocumentData pensionDocument) {
        try {
            return objectMapper.readValue(objectMapper.writeValueAsString(pensionDocument), PensionDocumentData.class);
        } catch (IOException e) {
            throw new IllegalStateException();
        }
    }

}