package uk.gov.hmcts.reform.finrem.caseorchestration.handler.citizenui.linktocase;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.CallbackHandlerLogger;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AccessCodeCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AccessCodeEntry;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignCaseAccessService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.InvalidateAccessCodeService;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.apache.commons.collections4.ListUtils.emptyIfNull;

@Slf4j
public abstract class CUILinkToCaseAccessCodeAboutToSubmitHandler extends FinremCallbackHandler {

    protected final InvalidateAccessCodeService invalidateAccessCodeService;
    private final AssignCaseAccessService assignCaseAccessService;

    protected CUILinkToCaseAccessCodeAboutToSubmitHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                          InvalidateAccessCodeService invalidateAccessCodeService,
                                                          AssignCaseAccessService assignCaseAccessService) {
        super(finremCaseDetailsMapper);
        this.invalidateAccessCodeService = invalidateAccessCodeService;
        this.assignCaseAccessService = assignCaseAccessService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType,
                             CaseType caseType,
                             EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && handledEventType().equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        log.info(CallbackHandlerLogger.aboutToSubmit(callbackRequest));

        FinremCaseData data = callbackRequest.getFinremCaseData();
        FinremCaseData dataBefore = callbackRequest.getFinremCaseDataBefore();
        List<AccessCodeCollection> currentAccessCodes = emptyIfNull(getAccessCodes(data));

        List<AccessCodeCollection> merged =
            invalidateAccessCodeService.mergeForInvalidation(
                emptyIfNull(getAccessCodes(dataBefore)),
                currentAccessCodes
            );

        setAccessCodes(data, merged);
        assignCitizenRoleToCase(callbackRequest, currentAccessCodes, merged);

        return response(data);
    }

    private void assignCitizenRoleToCase(FinremCallbackRequest callbackRequest,
                                         List<AccessCodeCollection> currentAccessCodes,
                                         List<AccessCodeCollection> mergedAccessCodes) {
        Long caseId = callbackRequest.getCaseDetails().getId();
        String userId = getUserIdFromAccessCodes(currentAccessCodes, mergedAccessCodes)
            .orElseThrow(() -> new IllegalStateException("No userId found in access code collection"));
        String caseRole = citizenCaseRole();

        try {
            assignCaseAccessService.grantCaseRoleToUser(caseId, userId, caseRole, null);

            log.info("Successfully added user to case. caseId: {}, userId: {}, caseRole: {}", caseId, userId, caseRole);
        } catch (RuntimeException error) {
            log.error("Error adding user to case. caseId: {}, userId: {}, caseRole: {}, error: {}",
                caseId, userId, caseRole, error.getMessage());
            throw error;
        }
    }

    private Optional<String> getUserIdFromAccessCodes(List<AccessCodeCollection> currentAccessCodes,
                                                      List<AccessCodeCollection> mergedAccessCodes) {
        Optional<String> userIdFromCurrent = getLatestUserId(currentAccessCodes);
        if (userIdFromCurrent.isPresent()) {
            return userIdFromCurrent;
        }

        Set<UUID> submittedAccessCodeIds = currentAccessCodes.stream()
            .map(AccessCodeCollection::getId)
            .filter(Objects::nonNull)
            .collect(java.util.stream.Collectors.toSet());

        Optional<String> userIdFromMatchingMerged = getLatestUserId(mergedAccessCodes.stream()
            .filter(accessCode -> submittedAccessCodeIds.contains(accessCode.getId()))
            .toList());

        if (userIdFromMatchingMerged.isPresent()) {
            return userIdFromMatchingMerged;
        }

        return getLatestUserId(mergedAccessCodes);
    }

    private Optional<String> getLatestUserId(List<AccessCodeCollection> accessCodes) {
        return accessCodes.stream()
            .map(AccessCodeCollection::getValue)
            .filter(Objects::nonNull)
            .filter(entry -> entry.getUserIdamID() != null && !entry.getUserIdamID().isBlank())
            .max(Comparator.comparing(AccessCodeEntry::getUsedAt, Comparator.nullsFirst(Comparator.naturalOrder())))
            .map(AccessCodeEntry::getUserIdamID);
    }

    protected abstract EventType handledEventType();

    protected abstract String citizenCaseRole();

    protected abstract List<AccessCodeCollection> getAccessCodes(FinremCaseData data);

    protected abstract void setAccessCodes(FinremCaseData data, List<AccessCodeCollection> accessCodes);
}
