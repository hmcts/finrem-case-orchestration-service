package uk.gov.hmcts.reform.finrem.caseorchestration.service.caselocation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseLocation;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CaseManagementLocationService {
    public CaseLocation getCaseLocation(Map<String, Object> caseData) {
        Map.Entry<String, Object> entry = caseData.entrySet()
            .stream()
            .filter(e -> e.getKey().endsWith("CourtList"))
            .findFirst()
            .orElse(null);

        if (entry != null) {
            return CaseLocation.builder().baseLocation("438850").region("438850").build();
        }
        return null;

    }
}
