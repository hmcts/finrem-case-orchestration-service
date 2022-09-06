package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.CaseFlagsConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;

import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.singletonMap;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MINI_FORM_A;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreateCaseService {

    private final AuthTokenGenerator authTokenGenerator;
    private final OnlineFormDocumentService onlineFormDocumentService;
    private final IdamService idamService;
    private final CoreCaseDataApi coreCaseDataApi;

    private final CaseFlagsConfiguration caseFlagsConfiguration;

    public void draftContestedMiniFormA(CallbackRequest callbackRequest, String userAuthorisation) {
        Map<String, Object> caseData = callbackRequest.getCaseDetails().getData();
        CaseDocument document = onlineFormDocumentService.generateDraftContestedMiniFormA(userAuthorisation, callbackRequest.getCaseDetails());
        caseData.put(MINI_FORM_A, document);
        if (!idamService.isUserRoleAdmin(userAuthorisation)) {
            log.info("other users.");
            caseData.put(APPLICANT_REPRESENTED, YES_VALUE);
        }
    }

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
