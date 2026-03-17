package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ValidatePartiesService {

    private final PrdOrganisationService organisationService;

    /**
     * Determines whether the given email address is registered within the specified organisation.
     *
     * <p>This method first attempts to resolve a user ID for the supplied email address.
     * If a user is found, it then retrieves the organisation identifier associated with
     * that user and compares it with the provided {@code orgId}.</p>
     *
     * <p>The method returns {@code false} if:
     * <ul>
     *     <li>No user is found for the given email address</li>
     *     <li>No organisation is associated with the resolved user</li>
     *     <li>The organisation identifier does not match the provided {@code orgId}</li>
     * </ul>
     * </p>
     *
     * @param email the email address to check
     * @param orgId the organisation identifier to validate against
     * @return {@code true} if the email belongs to a user registered in the specified
     *         organisation; otherwise {@code false}
     */
    public boolean isEmailRegisteredInOrg(String email, String orgId) {
        return organisationService.findUserByEmail(email)
            .flatMap(organisationService::findOrganisationIdByUserId)
            .map(orgId::equals)
            .orElse(false);
    }
}
