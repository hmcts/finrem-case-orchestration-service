package uk.gov.hmcts.reform.finrem.caseorchestration.service.documentremoval;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.DocumentDeleteException;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentToKeep;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentToKeepCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static java.lang.String.format;

@Service
@Slf4j
@RequiredArgsConstructor
public class DocumentRemovalService {

    static final String DOCUMENT_URL = "document_url";
    public static final String DOCUMENT_FILENAME = "document_filename";
    private static final String DOCUMENT_BINARY_URL = "document_binary_url";

    private final ObjectMapper objectMapper;

    private final GenericDocumentService genericDocumentService;

    public List<JsonNode> getDocumentNodes(FinremCaseData caseData) {
        JsonNode root = objectMapper.valueToTree(caseData);
        List<JsonNode> documentNodes = new ArrayList<>();

        retrieveDocumentNodes(root, documentNodes);
        return documentNodes;
    }

    public void retrieveDocumentNodes(JsonNode root, List<JsonNode> documentNodes) {
        if (root.isObject()) {
            Iterator<String> fieldNames = root.fieldNames();
            while (fieldNames.hasNext()) {
                String fieldName = fieldNames.next();
                JsonNode fieldValue = root.get(fieldName);
                if (fieldValue.has(DOCUMENT_URL)) {
                    documentNodes.add(fieldValue);
                } else {
                    retrieveDocumentNodes(fieldValue, documentNodes);
                }
            }
        } else if (root.isArray()) {
            ArrayNode arrayNode = (ArrayNode) root;
            for (int i = 0; i < arrayNode.size(); i++) {
                JsonNode arrayElement = arrayNode.get(i);
                retrieveDocumentNodes(arrayElement, documentNodes);
            }
        }
    }

    public void removeDocumentFromJson(JsonNode root, DocumentToKeep documentToDelete) {
        if (root.isObject()) {
            // Use a list to store field names to be removed
            List<String> fieldsToRemove = new ArrayList<>();
            Iterator<String> fieldNames = root.fieldNames();

            while (fieldNames.hasNext()) {
                String fieldName = fieldNames.next();
                JsonNode fieldValue = root.get(fieldName);

                if (fieldValue.has(DOCUMENT_URL)) {
                    if (fieldValue.get(DOCUMENT_URL).asText().equals(documentToDelete.getCaseDocument().getDocumentUrl())) {
                        log.info(String.format("Deleting doc with url %s", documentToDelete.getCaseDocument().getDocumentUrl()));
                        fieldsToRemove.add(fieldName);
                    }
                } else {
                    removeDocumentFromJson(fieldValue, documentToDelete);
                }
            }

            // Remove the fields after iteration
            for (String fieldName : fieldsToRemove) {
                ((ObjectNode) root).remove(fieldName);
            }
        } else if (root.isArray()) {
            ArrayNode arrayNode = (ArrayNode) root;
            for (int i = 0; i < arrayNode.size(); i++) {
                JsonNode arrayElement = arrayNode.get(i);
                removeDocumentFromJson(arrayElement, documentToDelete);
            }
        }
    }

    public List<DocumentToKeepCollection> buildCaseDocumentList(List<JsonNode> documentNodes) {

        List<DocumentToKeepCollection> documentsCollection = new ArrayList<>();

        for (JsonNode documentNode : documentNodes) {
            String docUrl = documentNode.get(DOCUMENT_URL).asText();
            String[] documentUrlAsArray = docUrl.split("/");
            String docId = documentUrlAsArray[documentUrlAsArray.length - 1];

            documentsCollection.add(
                DocumentToKeepCollection.builder()
                    .value(DocumentToKeep.builder()
                        .documentId(docId)
                        .caseDocument(CaseDocument.builder()
                            .documentFilename(documentNode.get(DOCUMENT_FILENAME).asText())
                            .documentUrl(documentNode.get(DOCUMENT_URL).asText())
                            .documentBinaryUrl(documentNode.get(DOCUMENT_BINARY_URL).asText())
                            .build())
                        .build())
                    .build());
        }
        return documentsCollection;
    }


    // todo - some jdoc to explain why we're doing this
    // Clears out the document collection from the root node, so that it isn't part of the final CCD data.
    public void removeDocumentToRemoveCollection(JsonNode root) {
        ((ObjectNode) root).remove("documentToKeepCollection");
    }

    // todo jdoc - based on deleteOldMiniFormA
    // Once working, consider making async again.  See deleteOldMiniFormA
    public void deleteDocument(DocumentToKeep documentToRemove, String authorisationToken) {

        try {
            genericDocumentService.deleteDocument(documentToRemove.getCaseDocument().getDocumentUrl(), authorisationToken);
        } catch (Exception e) {
            log.error(format(
                    "Failed to delete document url %s",
                    documentToRemove.getCaseDocument().getDocumentUrl()), e);

            throw new DocumentDeleteException(e.getMessage(), e);
        }
    }

    // rebuild case data with file data redacted.  Does this from the root node with the required updates.
    public FinremCaseData buildAmendedCaseDataFromRootNode(JsonNode root, Long caseId, ObjectMapper objectMapper) {
        FinremCaseData amendedCaseData;
        try {
            amendedCaseData = objectMapper.treeToValue(root, FinremCaseData.class);
        } catch (Exception e) {
            log.error(format("Error building amendedCaseData for case id %s after deleting document", caseId), e);
            throw new DocumentDeleteException(e.getMessage(), e);
        }
        return amendedCaseData;
    }
}
