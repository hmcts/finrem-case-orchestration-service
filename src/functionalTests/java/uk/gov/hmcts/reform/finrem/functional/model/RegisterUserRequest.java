package uk.gov.hmcts.reform.finrem.functional.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class RegisterUserRequest {
    private String email;
    private String forename;
    @Builder.Default
    private String surname = "User";
    private String password;
    @Builder.Default
    private int levelOfAccess = 1;
    private UserGroup[] roles;
    private UserGroup userGroup;
}
