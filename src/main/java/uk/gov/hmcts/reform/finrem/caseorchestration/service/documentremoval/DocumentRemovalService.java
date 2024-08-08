package uk.gov.hmcts.reform.finrem.caseorchestration.service.documentremoval;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.DocumentDeleteException;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentToRemove;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentToRemoveCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static java.lang.String.format;

@Service
@Slf4j
public class DocumentRemovalService {

    // Todo - Lots of JDOC to explain this

    public static final String DOCUMENT_URL = "document_url";
    public static final String DOCUMENT_FILENAME = "document_filename";
    private static final String DOCUMENT_BINARY_URL = "document_binary_url";

    private final GenericDocumentService genericDocumentService;

    @Autowired
    public DocumentRemovalService(GenericDocumentService genericDocumentService) {
        this.genericDocumentService = genericDocumentService;
    }

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
    public void updateNodeForDocumentToDelete(JsonNode root, JsonNode newNode, DocumentToRemove documentToRemove) {
        if (root.isObject()) {
            Iterator<String> fieldNames = root.fieldNames();
            while (fieldNames.hasNext()) {
                String fieldName = fieldNames.next();
                JsonNode fieldValue = root.get(fieldName);
                if (fieldValue.has(DOCUMENT_URL)){
                    if (fieldValue.get(DOCUMENT_URL).asText().equals(documentToRemove.getDocumentToRemoveUrl())) {
                        log.info(String.format("Updating field %1$s from %2$s to %3$s",
                                fieldName,
                                documentToRemove.getDocumentToRemoveUrl(),
                                newNode.get(DOCUMENT_URL).asText()
                                )
                        );
                        ((ObjectNode) root).set(fieldName, newNode);
                    }
                }
                else {
                    updateNodeForDocumentToDelete(fieldValue, newNode, documentToRemove);
                }
            }
        } else if (root.isArray()) {
            ArrayNode arrayNode = (ArrayNode) root;
            for (int i = 0; i < arrayNode.size(); i++) {
                JsonNode arrayElement = arrayNode.get(i);
                updateNodeForDocumentToDelete(arrayElement, newNode, documentToRemove);
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
    // Clears out the document collection from the root node, so that it isn't part of the final CCD data.
    public void removeDocumentToRemoveCollection(JsonNode root) {
        ((ObjectNode) root).remove("documentToRemoveCollection");
    }

    // todo jdoc - based on deleteOldMiniFormA
    public void deleteDocument(DocumentToRemove documentToRemove, String authorisationToken) {

        try {
            genericDocumentService.deleteDocument(documentToRemove.getDocumentToRemoveUrl(), authorisationToken);
        } catch (Exception e) {
            log.error(format(
                    "Failed to delete document url %s",
                    documentToRemove.getDocumentToRemoveUrl()), e);

            throw new DocumentDeleteException(e.getMessage(), e);
        }
    }

    // Uses the node tree to rebuild the same documents collection, that we use to display to the user after about-to-start
    public List<DocumentToRemoveCollection> buildCaseDocumentList(List<JsonNode> documentNodes) {

        List<DocumentToRemoveCollection> allExistingDocumentsList = new ArrayList<>();

        for (JsonNode documentNode : documentNodes) {
            String docUrl = documentNode.get(DOCUMENT_URL).asText();
            String[] documentUrlAsArray = docUrl.split("/");
            String docId = documentUrlAsArray[documentUrlAsArray.length-1];

            allExistingDocumentsList.add(
                    DocumentToRemoveCollection.builder()
                            .value(DocumentToRemove.builder()
                                    .documentToRemoveUrl(docUrl)
                                    .documentToRemoveName(documentNode.get(DOCUMENT_FILENAME).asText())
                                    .documentToRemoveId(docId)
                                    .build())
                            .build());
        }
        return allExistingDocumentsList;
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
