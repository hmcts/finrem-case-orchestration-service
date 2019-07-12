package uk.gov.hmcts.reform.finrem.caseorchestration.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.DocumentClient;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.PensionDocumentData;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

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

    public List<PensionDocumentData> stampDocument(List<PensionDocumentData> pensionList, String authorisationToken) {
        return pensionList.stream()
                .map(data -> stampDocument(data, authorisationToken)).collect(toList());
    }

    private PensionDocumentData stampDocument(PensionDocumentData pensionDocument, String authorisationToken) {
        CaseDocument document = pensionDocument.getPensionDocument().getDocument();
        CaseDocument stampedDocument = stampDocument(document, authorisationToken);
        PensionDocumentData stampedPensionData = copyOf(pensionDocument);
        stampedPensionData.getPensionDocument().setDocument(stampedDocument);
        return stampedPensionData;
    }

    PensionDocumentData copyOf(PensionDocumentData pensionDocument) {
        try {
            return objectMapper.readValue(objectMapper.writeValueAsString(pensionDocument), PensionDocumentData.class);
        } catch (IOException e) {
            throw new IllegalStateException();
        }
    }

}