package uk.gov.hmcts.reform.finrem.functional.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserDetails {
    private String id;
    private String email;
    private String forename;
    private String surname;
    private List<String> roles;

}