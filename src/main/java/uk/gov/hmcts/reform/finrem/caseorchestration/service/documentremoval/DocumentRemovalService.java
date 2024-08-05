package uk.gov.hmcts.reform.finrem.caseorchestration.service.documentremoval;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentToRemove;

import java.util.Iterator;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class DocumentRemovalService {

    /* Todo - refactor the standard JSON iterator pattern to take a function to call
    So we don't have the iterator code and their tests replicated.
    Consider Jdoc to explain the iterator pattern and what it's doing*/


    public static final String DOCUMENT_URL = "document_url";
    public static final String DOCUMENT_FILENAME = "document_filename";

    public void retrieveDocumentNodes(JsonNode root, List<JsonNode> documentNodes) {
        if (root.isObject()) {
            Iterator<String> fieldNames = root.fieldNames();
            while (fieldNames.hasNext()) {
                String fieldName = fieldNames.next();
                JsonNode fieldValue = root.get(fieldName);
                if (fieldValue.has("document_url")){
                    documentNodes.add(fieldValue);
                }
                else {
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

    public void updateNodeForDocumentToDelete(JsonNode root, DocumentToRemove documentToDelete, String deletedDocId) {
        if (root.isObject()) {
            Iterator<String> fieldNames = root.fieldNames();
            while (fieldNames.hasNext()) {
                String fieldName = fieldNames.next();
                JsonNode fieldValue = root.get(fieldName);
                if (fieldValue.has("document_url")){
                    if (fieldValue.get("document_url").asText().equals(documentToDelete.getDocumentToRemoveUrl())) {
                        log.info("found this one to remove.  Is this a useful log with ID?");
                        // Todo.  Next modify the node with deletedDocId and a suitable new filename.
                    }
                }
                else {
                    updateNodeForDocumentToDelete(fieldValue, documentToDelete, deletedDocId);
                }
            }
        } else if (root.isArray()) {
            ArrayNode arrayNode = (ArrayNode) root;
            for (int i = 0; i < arrayNode.size(); i++) {
                JsonNode arrayElement = arrayNode.get(i);
                updateNodeForDocumentToDelete(arrayElement, documentToDelete, deletedDocId);
            }
        }
    }
}
