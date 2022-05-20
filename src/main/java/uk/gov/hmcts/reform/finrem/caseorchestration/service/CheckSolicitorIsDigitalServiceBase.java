package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

@Service
@Slf4j
@RequiredArgsConstructor
public abstract class CheckSolicitorIsDigitalServiceBase {

    protected final CaseDataService caseDataService;

    public abstract boolean isSolicitorDigital(CaseDetails caseDetails);
}
