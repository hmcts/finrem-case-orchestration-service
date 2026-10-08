package uk.gov.hmcts.reform.finrem.caseorchestration.handler.citizenui.linktocase;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AccessCodeCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignCaseAccessService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CorrespondenceEventAuditOrchestrationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.InvalidateAccessCodeService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.citizen.SignInConfirmationCorresponder;

import java.util.List;

@Slf4j
@Service
public class CUILinkRespondentToCaseAboutToSubmitHandler extends CUILinkToCaseAboutToSubmitHandler {

    public CUILinkRespondentToCaseAboutToSubmitHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                       InvalidateAccessCodeService invalidateAccessCodeService,
                                                       AssignCaseAccessService assignCaseAccessService,
                                                       SignInConfirmationCorresponder signInConfirmationCorresponder,
                                                       CorrespondenceEventAuditOrchestrationService correspondenceEventAuditOrchestrationService) {
        super(finremCaseDetailsMapper, invalidateAccessCodeService, assignCaseAccessService,
            signInConfirmationCorresponder, correspondenceEventAuditOrchestrationService);
    }
    @Override
    protected EventType handledEventType() {
        return EventType.LINK_RESPONDENT_TO_CASE;
    }

    @Override
    protected String citizenCaseRole() {
        return CaseRole.CITIZEN_RESPONDENT.getCcdCode();
    }

    @Override
    protected List<AccessCodeCollection> getAccessCodes(FinremCaseData data) {
        return data.getRespondentAccessCodes();
    }

    @Override
    protected void setAccessCodes(FinremCaseData data, List<AccessCodeCollection> accessCodes) {
        data.setRespondentAccessCodes(accessCodes);
    }

    @Override
    protected NotificationParty notificationParty() {
        return NotificationParty.CITIZEN_RESPONDENT;
    }
}
