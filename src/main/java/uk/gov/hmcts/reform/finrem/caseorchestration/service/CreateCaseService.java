package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.CaseFlagsConfiguration;

import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.singletonMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreateCaseService {

    private final AuthTokenGenerator authTokenGenerator;
    private final CoreCaseDataApi coreCaseDataApi;

    private final CaseFlagsConfiguration caseFlagsConfiguration;

    public void setSupplementaryData(CallbackRequest callbackRequest, String authorisation) {
        String caseId = String.valueOf(callbackRequest.getCaseDetails().getId());
        Map<String, Map<String, Map<String, Object>>> supplementaryDataFinancialRemedy = new HashMap<>();
        supplementaryDataFinancialRemedy.put("supplementary_data_updates",
            singletonMap("$set", singletonMap("HMCTSServiceId",
                caseFlagsConfiguration.getHmctsId())));

        coreCaseDataApi.submitSupplementaryData(authorisation,
                                                authTokenGenerator.generate(),
                                                caseId,
                                                supplementaryDataFinancialRemedy);
    }
}
