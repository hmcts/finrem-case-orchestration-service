package uk.gov.hmcts.reform.finrem.caseorchestration.handler.documentremoval;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentToRemove;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentToRemoveCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Slf4j
@Service
public class DocumentRemovalAboutToStartHandler extends FinremCallbackHandler {

    private final ObjectMapper objectMapper;

    public DocumentRemovalAboutToStartHandler(FinremCaseDetailsMapper mapper) {
        super(mapper);
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_START.equals(callbackType)
            && (EventType.REMOVE_CASE_DOCUMENT.equals(eventType));
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest, String userAuthorisation) {
        final String DOCUMENT_URL = "document_url";
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        FinremCaseData caseData = caseDetails.getData();
        List<JsonNode> documentNodes = new ArrayList<>();
        List<DocumentToRemoveCollection> documentsCollection = new ArrayList<>();

        try {
            JsonNode root = objectMapper.valueToTree(caseData);
            traverse(root, documentNodes);

            for (JsonNode documentNode : documentNodes) {

                // Splits the document into array elements, so we can use the last as the document id
                String[] documentUrlAsArray = documentNode.get(DOCUMENT_URL).asText().split("/");

                documentsCollection.add(
                        DocumentToRemoveCollection.builder()
                                .value(DocumentToRemove.builder()
                                        .documentToRemoveUrl(documentNode.get(DOCUMENT_URL).asText())
                                        .documentToRemoveName(documentNode.get("document_filename").asText())
                                        .documentToRemoveId(documentUrlAsArray[documentUrlAsArray.length-1])
                                        .build())
                                .build());
            }

        } catch (Exception e) {
            log.error("Exception occurred while converting case data to JSON", e);
        }

        caseData.setDocumentToRemoveCollection(documentsCollection);

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().data(caseData).build();
    }

    public void traverse(JsonNode root, List<JsonNode> documentNodes) {
        if (root.isObject()) {
            Iterator<String> fieldNames = root.fieldNames();
            while (fieldNames.hasNext()) {
                String fieldName = fieldNames.next();
                JsonNode fieldValue = root.get(fieldName);
                if (fieldValue.has("document_url")){
                    documentNodes.add(fieldValue);
                }
                else {
                    traverse(fieldValue, documentNodes);
                }
            }
        } else if (root.isArray()) {
            ArrayNode arrayNode = (ArrayNode) root;
            for (int i = 0; i < arrayNode.size(); i++) {
                JsonNode arrayElement = arrayNode.get(i);
                traverse(arrayElement, documentNodes);
            }
        }
    }

}
