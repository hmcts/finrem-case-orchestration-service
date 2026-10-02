package uk.gov.hmcts.reform.finrem.caseorchestration.handler.citizenui.linktocase;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AccessCodeCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignCaseAccessService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.InvalidateAccessCodeService;

import java.util.List;

@Slf4j
@Service
public class CUILinkApplicantToCaseAboutToSubmitHandler extends CUILinkToCaseAboutToSubmitHandler {

    public CUILinkApplicantToCaseAboutToSubmitHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                       InvalidateAccessCodeService invalidateAccessCodeService,
                                                       AssignCaseAccessService assignCaseAccessService) {
        super(finremCaseDetailsMapper, invalidateAccessCodeService, assignCaseAccessService);
    }

    @Override
    protected EventType handledEventType() {
        return EventType.LINK_APPLICANT_TO_CASE;
    }

    @Override
    protected String citizenCaseRole() {
        return CaseRole.CITIZEN_APPLICANT.getCcdCode();
    }

    @Override
    protected List<AccessCodeCollection> getAccessCodes(FinremCaseData data) {
        return data.getApplicantAccessCodes();
    }

    @Override
    protected void setAccessCodes(FinremCaseData data, List<AccessCodeCollection> accessCodes) {
        data.setApplicantAccessCodes(accessCodes);
    }
}
