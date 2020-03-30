package uk.gov.hmcts.reform.finrem.functional.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDetails {
    private String username;
    private String emailAddress;
    private String password;
    private String authToken;
    private String id;
}
