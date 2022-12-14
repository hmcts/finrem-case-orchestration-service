package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ContestedChildrenDetailDataWrapper;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CHILDREN_COLLECTION;


public class ContestedChildrenServiceTest  extends BaseServiceTest  {


    @Autowired
    private ContestedChildrenService service;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String TEST_JSON = "/fixtures/contested/schedule-1-children.json";


    @Test
    public void hasChildrenLivingOutsideOfEnglandAndWalesYes() {
        CallbackRequest callbackRequest = buildCallbackRequest();
        List<String> errors  = new ArrayList<>();
        service.hasChildrenLivingOutsideOfEnglandAndWales(callbackRequest.getCaseDetails(), errors);
        assertTrue(errors.get(0)
            .contains("The court does not have jurisdiction as the child is not habitually resident in England or Wales"));
    }

    @Test
    public void hasChildrenLivingOutsideOfEnglandAndWalesNo() {
        CallbackRequest callbackRequest = buildCallbackRequest();
        List<ContestedChildrenDetailDataWrapper> children = service.getChildren(callbackRequest.getCaseDetails().getData());
        children.forEach(child -> child.getValue().setChildrenLivesInEnglandOrWales(YES_VALUE));
        callbackRequest.getCaseDetails().getData().put(CHILDREN_COLLECTION, children);
        List<String> errors  = new ArrayList<>();
        service.hasChildrenLivingOutsideOfEnglandAndWales(callbackRequest.getCaseDetails(), errors);
        assertTrue(errors.isEmpty());
    }

    @Test
    public void getChildren() {
        CallbackRequest callbackRequest = buildCallbackRequest();
        List<ContestedChildrenDetailDataWrapper> children = service.getChildren(callbackRequest.getCaseDetails().getData());
        assertTrue(children.size() > 0);
    }

    private CallbackRequest buildCallbackRequest()  {
        try (InputStream resourceAsStream = getClass().getResourceAsStream(TEST_JSON)) {
            return objectMapper.readValue(resourceAsStream, CallbackRequest.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}