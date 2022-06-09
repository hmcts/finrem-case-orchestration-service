package uk.gov.hmcts.reform.finrem.caseorchestration.wrapper;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class IdamToken {
    String idamOauth2Token;
    String serviceAuthorization;
    final String userId;
    final String email;
    final List<String> roles;
}