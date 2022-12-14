package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ContestedChildrenDetailDataWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CHILDREN_COLLECTION;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContestedChildrenService {

    private final ObjectMapper objectMapper;

    public List<ContestedChildrenDetailDataWrapper> getChildren(Map<String, Object> caseData) {
        return Optional.ofNullable(caseData.get(CHILDREN_COLLECTION))
            .map(this::contestedChildrenDetailDataWrapper).orElse(new ArrayList<>());

    }

    private List<ContestedChildrenDetailDataWrapper> contestedChildrenDetailDataWrapper(Object object) {
        return objectMapper.registerModule(new JavaTimeModule()).convertValue(object, new TypeReference<>() {
        });
    }

    public void hasChildrenLivingOutsideOfEnglandAndWales(CaseDetails caseDetails, List<String> errors) {
        Map<String, Object> caseData = caseDetails.getData();
        List<ContestedChildrenDetailDataWrapper> childrenList =
            getChildren(caseData).stream().filter(child -> child.getValue().childrenLivesInEnglandOrWales.equals(NO_VALUE)).toList();

        if (!childrenList.isEmpty()) {
            log.info("children living outside of uk {} for case {}", childrenList,
                caseDetails.getId());
            errors.add("The court does not have jurisdiction as the child is not habitually resident in England or "
                + "Wales");
        }
    }
}
