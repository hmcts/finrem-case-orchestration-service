package uk.gov.hmcts.reform.finrem.caseorchestration.service.documentremoval;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.DocumentDeleteException;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentToRemove;

import java.util.Iterator;
import java.util.List;

import static java.lang.String.format;

@Service
@Slf4j
@RequiredArgsConstructor
public class DocumentRemovalService {

    // Todo - Lots of JDOC to explain this

    public static final String DOCUMENT_URL = "document_url";
    public static final String DOCUMENT_FILENAME = "document_filename";
    private static final String DOCUMENT_BINARY_URL = "document_binary_url";

    public void retrieveDocumentNodes(JsonNode root, List<JsonNode> documentNodes) {
        if (root.isObject()) {
            Iterator<String> fieldNames = root.fieldNames();
            while (fieldNames.hasNext()) {
                String fieldName = fieldNames.next();
                JsonNode fieldValue = root.get(fieldName);
                if (fieldValue.has(DOCUMENT_URL)){
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

    // Todo - test for generated node and exception
    // Todo Jdoc
    public void updateNodeForDocumentToDelete(JsonNode root, JsonNode newNode, DocumentToRemove documentToDelete) {
        if (root.isObject()) {
            Iterator<String> fieldNames = root.fieldNames();
            while (fieldNames.hasNext()) {
                String fieldName = fieldNames.next();
                JsonNode fieldValue = root.get(fieldName);
                if (fieldValue.has(DOCUMENT_URL)){
                    if (fieldValue.get(DOCUMENT_URL).asText().equals(documentToDelete.getDocumentToRemoveUrl())) {
                        log.info(String.format("Updating field %1$s from %2$s to %3$s",
                                fieldName,
                                documentToDelete.getDocumentToRemoveUrl(),
                                newNode.get(DOCUMENT_URL).asText()
                                )
                        );
                        ((ObjectNode) root).set(fieldName, newNode);
                    }
                }
                else {
                    updateNodeForDocumentToDelete(fieldValue, newNode, documentToDelete);
                }
            }
        } else if (root.isArray()) {
            ArrayNode arrayNode = (ArrayNode) root;
            for (int i = 0; i < arrayNode.size(); i++) {
                JsonNode arrayElement = arrayNode.get(i);
                updateNodeForDocumentToDelete(arrayElement, newNode, documentToDelete);
            }
        }
    }

    // Todo - test for generated node and exception
    // Todo Jdoc
    public JsonNode buildNewNodeForDeletedFile(ObjectMapper objectMapper, String documentUrl,
                                               String documentFilename, String documentBinaryUrl) throws DocumentDeleteException {
        String newString = String.format("{" +
                "\"%1$s\": \"%2$s\"," +
                "\"%3$s\": \"%4$s\"," +
                "\"%5$s\": \"%6$s\"}",
                DOCUMENT_URL, documentUrl,
                DOCUMENT_FILENAME, documentFilename,
                DOCUMENT_BINARY_URL, documentBinaryUrl);

        try {
            return objectMapper.readTree(newString);
        } catch (Exception e) {
            log.error(format("Error building new node when deleting document url: %s", documentUrl), e);
            throw new DocumentDeleteException(e.getMessage(), e);
        }
    }

    // todo - some jdoc to explain why we're doing this
    public void removeDocumentToRemoveCollection(JsonNode root) {
        ((ObjectNode) root).remove("documentToRemoveCollection");
    }

}
