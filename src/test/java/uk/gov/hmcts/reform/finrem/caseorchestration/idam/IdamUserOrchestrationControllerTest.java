package uk.gov.hmcts.reform.finrem.caseorchestration.idam;

import org.junit.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.BaseControllerTest;

import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.finrem.caseorchestration.idam.IdamUserOrchestrationController.IDAM_CLIENT_SECRET_HEADER;

@WebMvcTest(IdamUserOrchestrationController.class)
public class IdamUserOrchestrationControllerTest extends BaseControllerTest {

    private static final String IDAM_USERS_URL = "/case-orchestration/idam/users";

    @MockitoBean
    private IdamUserOrchestrationService idamUserOrchestrationService;

    @Test
    public void shouldCreateUser() throws Exception {
        when(idamUserOrchestrationService.createUser(
            any(IdamUserOrchestrationModels.CreateUserRequest.class), eq("secret"))
        ).thenReturn(response("CREATE"));

        mvc.perform(post(IDAM_USERS_URL)
                .header(IDAM_CLIENT_SECRET_HEADER, "secret")
                .content(createJson())
                .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.operation", is("CREATE")))
            .andExpect(jsonPath("$.users[0].email", is("staff.admin@example.com")));

        verify(idamUserOrchestrationService).createUser(
            any(IdamUserOrchestrationModels.CreateUserRequest.class), eq("secret")
        );
    }

    @Test
    public void shouldDeleteUser() throws Exception {
        when(idamUserOrchestrationService.deleteUser(any(IdamUserOrchestrationModels.DeleteUserRequest.class)))
            .thenReturn(new IdamUserOrchestrationModels.OrchestrationResponse(
                "DELETE",
                "aat",
                List.of(new IdamUserOrchestrationModels.UserResult(
                    null,
                    "staff.admin@example.com",
                    "staff.admin@example.com",
                    null
                )),
                List.of(step("DELETE_USER"))
            ));

        mvc.perform(delete(IDAM_USERS_URL)
                .content("""
                    {
                      "environment": "aat",
                      "users": [
                        {
                          "email": "staff.admin@example.com"
                        }
                      ]
                    }
                    """)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.operation", is("DELETE")))
            .andExpect(jsonPath("$.users[0].deletedEmail", is("staff.admin@example.com")));

        verify(idamUserOrchestrationService).deleteUser(
            any(IdamUserOrchestrationModels.DeleteUserRequest.class)
        );
    }

    @Test
    public void shouldUpdateUser() throws Exception {
        when(idamUserOrchestrationService.updateUser(
            any(IdamUserOrchestrationModels.UpdateUserRequest.class), eq("secret"))
        ).thenReturn(response("UPDATE"));

        mvc.perform(put(IDAM_USERS_URL)
                .header(IDAM_CLIENT_SECRET_HEADER, "secret")
                .content("""
                    {
                      "environment": "aat",
                      "password": "updated-password",
                      "users": [
                        {
                          "existingEmail": "staff.admin@example.com",
                          "email": "staff.admin@example.com",
                          "forename": "Staff",
                          "surname": "Admin",
                          "roleNames": [
                            "staff-admin",
                            "caseworker"
                          ]
                        }
                      ]
                    }
                    """)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.operation", is("UPDATE")))
            .andExpect(jsonPath("$.users[0].email", is("staff.admin@example.com")));

        verify(idamUserOrchestrationService).updateUser(
            any(IdamUserOrchestrationModels.UpdateUserRequest.class), eq("secret")
        );
    }

    @Test
    public void shouldRejectCreateWithoutUser() throws Exception {
        mvc.perform(post(IDAM_USERS_URL)
                .content("""
                    {
                      "environment": "aat",
                      "password": "test-password"
                    }
                    """)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isBadRequest());
    }

    private String createJson() {
        return """
            {
              "environment": "aat",
              "password": "test-password",
              "users": [
                {
                  "email": "staff.admin@example.com",
                  "forename": "Staff",
                  "surname": "Admin",
                  "roleNames": [
                    "staff-admin",
                    "caseworker"
                  ]
                }
              ]
            }
            """;
    }

    private IdamUserOrchestrationModels.OrchestrationResponse response(String operation) {
        return new IdamUserOrchestrationModels.OrchestrationResponse(
            operation,
            "aat",
            List.of(new IdamUserOrchestrationModels.UserResult(
                "user-id",
                "staff.admin@example.com",
                null,
                List.of("staff-admin", "caseworker")
            )),
            List.of(step("GET_CLIENT_CREDENTIALS_TOKEN"), step("CREATE_USER"))
        );
    }

    private IdamUserOrchestrationModels.Step step(String name) {
        return new IdamUserOrchestrationModels.Step(name, "idam", 200);
    }
}
