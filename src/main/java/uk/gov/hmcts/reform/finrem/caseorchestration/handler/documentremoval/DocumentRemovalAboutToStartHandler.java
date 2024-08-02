package uk.gov.hmcts.reform.finrem.caseorchestration.handler.documentremoval;

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
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        FinremCaseData caseData = caseDetails.getData();
        List<JsonNode> documentNodes = new ArrayList<>();
        List<DocumentToRemoveCollection> documentsCollection = new ArrayList<>();

        try {
            JsonNode root = objectMapper.valueToTree(caseData);
            traverse(root, documentNodes);
        } catch (Exception e) {
            log.error("Exception occurred while converting case data to JSON", e);
        }

       for (JsonNode documentNode : documentNodes) {

       }


        List<DocumentToRemoveCollection> documents = List.of(
            DocumentToRemoveCollection.builder()
                .value(DocumentToRemove.builder()
                    .documentToRemoveUrl("Doc URL 1")
                    .documentToRemoveName("Doc name 1")
                    .documentToRemoveId("ID 1")
                    .build())
                .build(),
            DocumentToRemoveCollection.builder()
                .value(DocumentToRemove.builder()
                    .documentToRemoveUrl("Doc URL 2")
                    .documentToRemoveName("Doc name 2")
                    .documentToRemoveId("ID 2")
                    .build()).build());

        caseData.setDocumentToRemoveCollection(documents);

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
