package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;

import java.time.LocalDate;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ISSUE_DATE;

@Service
@RequiredArgsConstructor
@Slf4j
public class OnStartDefaultValueService {

    public void defaultIssueDate(CallbackRequest callbackRequest) {
        callbackRequest.getCaseDetails().getData().putIfAbsent(ISSUE_DATE, LocalDate.now());
    }
}