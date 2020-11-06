package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Service
public class MoveCollectionService {

    public Map<String, Object> moveCollection(Map<String, Object> caseData, String source, String destination) {
        if (caseData.get(source) != null && (caseData.get(source) instanceof Collection)) {
            if (caseData.get(destination) == null || (caseData.get(destination) instanceof Collection)) {
                final List destinationList = new ArrayList();
                if (caseData.get(destination) != null) {
                    destinationList.addAll((List) caseData.get(destination));
                }
                destinationList.addAll((List) caseData.get(source));
                caseData.put(destination, destinationList);
                caseData.put(source, null);
            }
        }

        return caseData;
    }
}
