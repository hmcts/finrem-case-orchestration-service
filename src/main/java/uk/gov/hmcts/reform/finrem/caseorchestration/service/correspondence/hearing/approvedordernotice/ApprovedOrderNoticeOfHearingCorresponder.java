package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.hearing.approvedordernotice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.MultiLetterOrEmailAllPartiesCorresponder;

@Component
@Slf4j
public class ApprovedOrderNoticeOfHearingCorresponder extends MultiLetterOrEmailAllPartiesCorresponder {

    @Autowired
    public ApprovedOrderNoticeOfHearingCorresponder(
        ApprovedOrderNoticeOfHearingApplicantCorresponder approvedOrderNoticeOfHearingApplicantCorresponder,
        ApprovedOrderNoticeOfHearingRespondentCorresponder approvedOrderNoticeOfHearingRespondentCorresponder) {
        super(approvedOrderNoticeOfHearingApplicantCorresponder, approvedOrderNoticeOfHearingRespondentCorresponder);

    }
}
