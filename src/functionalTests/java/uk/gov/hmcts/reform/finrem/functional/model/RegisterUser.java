package uk.gov.hmcts.reform.finrem.functional.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class RegisterUser {
    private String id;
    private String email;
    private String forename;
    private String surname;
    private List<String> roleNames;
}
