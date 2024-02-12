package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class RemoveReplaceCaseDocumentNodeFromCaseData {

    ObjectMapper objectMapper = new ObjectMapper();

    public FinremCaseDetails removeOrReplaceCaseDocumentFromFinremCaseDetails(FinremCaseDetails finremCaseDetails, String documentUrlToRemove,
                                                                              String replacementUrl)
        throws JsonProcessingException {
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        String finRemCaseDetailsAsString = objectMapper.writeValueAsString(finremCaseDetails);
        try {
            JsonNode rootNode = objectMapper.readTree(finRemCaseDetailsAsString);
            finremCaseDetails = removeOrReplaceCaseDocumentFromJson(rootNode, documentUrlToRemove, replacementUrl, finremCaseDetails);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return finremCaseDetails;
    }

    public FinremCaseDetails removeOrReplaceCaseDocumentFromJson(JsonNode rootNode, String documentUrlToFind, String replacementUrl,
                                                                 FinremCaseDetails originalFinremCaseDetails) throws JsonProcessingException {
        FinremCaseDetails finremCaseDetails = originalFinremCaseDetails;
        try {
            // Search for the target value and remove the parent object
            boolean removed = removeOrReplaceParentObject(rootNode, null, documentUrlToFind, replacementUrl, new ArrayList<>());

            if (removed) {
                // Convert the modified tree back to a JSON string
                String updatedJsonString = objectMapper.writeValueAsString(rootNode);
                finremCaseDetails = objectMapper.treeToValue(rootNode, FinremCaseDetails.class);
                System.out.println("Updated JSON: " + updatedJsonString);
            } else {
                System.out.println("Document URL not found.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return finremCaseDetails;
    }


    private static boolean removeOrReplaceParentObject(JsonNode node, JsonNode parentNode, String targetValue, String replacementValue,
                                                       List<String> currentPath) {
        boolean removedOrReplaced = false;
        List<JsonNode> nodesToRemove = new ArrayList<>();
        if (node.isObject()) {
            String nodeFieldName = "";
            // If the current node is an object, iterate through its fields
            Iterator<String> fieldNames = node.fieldNames();
            while (fieldNames.hasNext()) {
                String fieldName = fieldNames.next();
                JsonNode childNode = node.get(fieldName);
                if (!currentPath.isEmpty()) {
                    nodeFieldName = currentPath.get(currentPath.size() - 1);
                }
                List<String> newPath = new ArrayList<>(currentPath);
                newPath.add(fieldName);

                if (fieldName.equals("document_url") && childNode.isTextual() && targetValue.equals(childNode.asText())) {
                    if (replacementValue != null) {
                        ((ObjectNode) node).put(fieldName, replacementValue);
                    }
                    // Remove the parent object
                    else if (parentNode instanceof ObjectNode) {


                            ((ObjectNode) parentNode).remove(nodeFieldName);
                    }
                    removedOrReplaced = true;
                } else if (childNode.isContainerNode()) {
                    // If the field value is a collection or another object, recursively search
                    if (removeOrReplaceParentObject(childNode, node, targetValue, replacementValue, newPath)) {
                        removedOrReplaced = true; // Parent object found and removed
                    }
                }
            }
        } else if (node.isArray()) {
            // If the current node is an array, iterate through its elements
            for (int i = node.size() - 1; i >= 0; i--) {
                JsonNode arrayElement = node.get(i);
                if (arrayElement.isContainerNode() && removeOrReplaceParentObject(arrayElement, node, targetValue, replacementValue, currentPath)) {
                    // If the target value is found in the array element, remove the entire parent object
                    nodesToRemove.add(node.get(i));
                    removedOrReplaced = true;
                }
            }
        }
        if (replacementValue == null) {
            nodesToRemove.forEach(pNode -> {
                System.out.println("Removing node: " + pNode.toString());
                if (pNode instanceof ObjectNode) {
                    ((ObjectNode) pNode).removeAll();
                } else if (pNode instanceof ArrayNode) {
                    ((ArrayNode) pNode).removeAll();
                }
            });
        }
        return removedOrReplaced; // Indicate whether at least one parent object was found and removed
    }


}
