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

@Service
@Slf4j
public class ConsentOrderApprovedDocumentService extends AbstractDocumentService {

    @Autowired
    public ConsentOrderApprovedDocumentService(DocumentClient documentClient,
                                               DocumentConfiguration config,
                                               ObjectMapper objectMapper) {
        super(documentClient, config, objectMapper);
    }

    public CaseDocument stampDocument(CaseDocument document, String authorisationToken) {
        return super.stampDocument(document, authorisationToken);
    }


    public List<PensionDocumentData> stampDocument(List<PensionDocumentData> pensionList, String authorisationToken) {
        List<PensionDocumentData> stampedPensionList = pensionList.stream()
                .map(data -> stampDocument(data, authorisationToken)).collect(Collectors.toList());
        return stampedPensionList;
    }

    private PensionDocumentData stampDocument(PensionDocumentData pensionDocumentData, String authorisationToken) {
        PensionDocumentData stampedPensionData = copyOf(pensionDocumentData);
        CaseDocument document = pensionDocumentData.getPensionDocument().getDocument();
        CaseDocument stampedDocument = stampDocument(document, authorisationToken);
        stampedPensionData.getPensionDocument().setDocument(stampedDocument);
        return stampedPensionData;
    }

    PensionDocumentData copyOf(PensionDocumentData pensionDocumentData) {
        try {
            return objectMapper.readValue(objectMapper.writeValueAsString(pensionDocumentData), PensionDocumentData.class);
        } catch (IOException e) {
            throw new IllegalStateException();
        }
    }

}