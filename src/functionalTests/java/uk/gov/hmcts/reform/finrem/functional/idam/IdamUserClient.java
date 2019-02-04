package uk.gov.hmcts.reform.finrem.functional.idam;

public interface IdamUserClient {

    String generateUserTokenWithNoRoles(String username, String password);

}
