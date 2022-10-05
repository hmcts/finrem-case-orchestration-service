package uk.gov.hmcts.reform.finrem.caseorchestration.mapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.CaseFlags;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.caseflag.Flags;

import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class FlagsMapper {
    public CaseFlags mapToCaseFlags(CaseDetails caseDetails, String ServiceID, String party, boolean WelshRequired ) {
        Map<String, Object> caseData = caseDetails.getData();

        return CaseFlags
            .builder()
            .caseflags(
                (Flags) caseData.get(party))
            .build();
    }
}
