package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;

import java.time.LocalDate;

import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ISSUE_DATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.TYPE_OF_APPLICATION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.TYPE_OF_APPLICATION_DEFAULT_TO;

@Service
@RequiredArgsConstructor
@Slf4j
public class OnStartDefaultValueService {

    public void defaultCivilPartnershipField(CallbackRequest callbackRequest) {
        callbackRequest.getCaseDetails().getData().putIfAbsent(CIVIL_PARTNERSHIP, NO_VALUE);
    }

    public void defaultTypeOfApplication(CallbackRequest callbackRequest) {
        callbackRequest.getCaseDetails().getData().putIfAbsent(TYPE_OF_APPLICATION, TYPE_OF_APPLICATION_DEFAULT_TO);
    }

    public void defaultIssueDate(CallbackRequest callbackRequest) {
        callbackRequest.getCaseDetails().getData().putIfAbsent(ISSUE_DATE, LocalDate.now());
    }
}
