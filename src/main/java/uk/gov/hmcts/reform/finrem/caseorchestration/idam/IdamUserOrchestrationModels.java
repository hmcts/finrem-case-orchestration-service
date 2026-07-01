package uk.gov.hmcts.reform.finrem.caseorchestration.idam;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.List;

public final class IdamUserOrchestrationModels {

    private IdamUserOrchestrationModels() {
    }

    public record CreateUserRequest(
        @JsonAlias("env")
        @Pattern(regexp = "^[a-zA-Z0-9-]+$", message = "environment must contain only letters, numbers and hyphens")
        String environment,
        @NotBlank String password,
        @Valid @NotEmpty List<@NotNull User> users
    ) {
    }

    public record DeleteUserRequest(
        @JsonAlias("env")
        @Pattern(regexp = "^[a-zA-Z0-9-]+$", message = "environment must contain only letters, numbers and hyphens")
        String environment,
        @Valid @NotEmpty List<@NotNull UserToDelete> users
    ) {
    }

    public record UpdateUserRequest(
        @JsonAlias("env")
        @Pattern(regexp = "^[a-zA-Z0-9-]+$", message = "environment must contain only letters, numbers and hyphens")
        String environment,
        @NotBlank String password,
        @Valid @NotEmpty List<@NotNull UserToUpdate> users
    ) {
    }

    public record User(
        @Email @NotBlank String email,
        @NotBlank String forename,
        @NotBlank String surname,
        @NotEmpty List<@NotBlank String> roleNames
    ) {
    }

    public record UserToDelete(
        @Email @NotBlank String email
    ) {
    }

    public record UserToUpdate(
        @Email @NotBlank String existingEmail,
        @Email @NotBlank String email,
        @NotBlank String forename,
        @NotBlank String surname,
        @NotEmpty List<@NotBlank String> roleNames
    ) {
        User toUser() {
            return new User(email, forename, surname, roleNames);
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record OrchestrationResponse(
        String operation,
        String environment,
        List<UserResult> users,
        List<Step> steps
    ) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record UserResult(
        String id,
        String email,
        String deletedEmail,
        List<String> roleNames
    ) {
    }

    public record Step(String name, String target, int statusCode) {
    }
}
