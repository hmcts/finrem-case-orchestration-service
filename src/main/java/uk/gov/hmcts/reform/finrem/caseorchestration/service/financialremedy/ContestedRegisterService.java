package uk.gov.hmcts.reform.finrem.caseorchestration.service.financialremedy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CaseResource;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.CaseDataApiV2;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SystemUserService;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ContestedRegisterService {

    private final CaseDataApiV2 caseDataApi;
    private final AuthTokenGenerator authTokenGenerator;
    private final SystemUserService systemUserService;

    public CaseResource getCaseDetails(String caseReference) {
        String userToken = systemUserService.getSysUserToken();
        String authToken = authTokenGenerator.generate();

        return caseDataApi.getCaseDetails(authToken, userToken, false, caseReference);
    }

}
