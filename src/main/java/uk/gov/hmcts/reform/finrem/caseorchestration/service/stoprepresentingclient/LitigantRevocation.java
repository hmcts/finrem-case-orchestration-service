package uk.gov.hmcts.reform.finrem.caseorchestration.service.stoprepresentingclient;

public record LitigantRevocation(boolean applicantSolicitorRevoked, boolean respondentSolicitorRevoked) {

    public boolean wasRevoked() {
        return applicantSolicitorRevoked || respondentSolicitorRevoked;
    }
}
