package uk.gov.hmcts.reform.finrem.caseorchestration.service.adapters;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.hearing.FinremApprovedOrderNoticeOfHearingCorresponder;

@Component
@RequiredArgsConstructor
public class FinremApprovedOrderNoticeOfHearingCorresponderAdapter {

    private final FinremApprovedOrderNoticeOfHearingCorresponder finremApprovedOrderNoticeOfHearingCorresponder;
    private final FinremCaseDetailsMapper finremCaseDetailsMapper;

    public void sendCorrespondence(CaseDetails caseDetails, String authorisationToken) {
        FinremCaseDetails finremCaseDetails = finremCaseDetailsMapper.mapToFinremCaseDetails(caseDetails);
        finremApprovedOrderNoticeOfHearingCorresponder.sendCorrespondence(finremCaseDetails, authorisationToken);
    }
}
