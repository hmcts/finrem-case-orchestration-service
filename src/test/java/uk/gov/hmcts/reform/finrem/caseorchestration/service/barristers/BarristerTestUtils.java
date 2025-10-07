package uk.gov.hmcts.reform.finrem.caseorchestration.service.barristers;

import uk.gov.hmcts.reform.finrem.caseorchestration.model.BarristerChange;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Barrister;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.BarristerParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Organisation;

import java.util.Set;
import java.util.UUID;

class BarristerTestUtils {

    static final String TEST_ORGANISATION_ID = "4646433";
    static final String TEST_ORGANISATION_NAME = "Test Org";

    static Set<Barrister> createBarristers() {
        Organisation organisation = Organisation.builder()
            .organisationName(TEST_ORGANISATION_NAME)
            .organisationID(TEST_ORGANISATION_ID)
            .build();
        return Set.of(
            barrister(organisation, "barrister1@test.com"),
            barrister(organisation, "barrister2@test.com"),
            barrister(organisation, "barrister3@test.com")
        );
    }

    static BarristerChange createBarristerChange(Set<Barrister> barristerAdded,
                                                 Set<Barrister> barristerRemoved) {
        return createBarristerChange(BarristerParty.APPLICANT, barristerAdded, barristerRemoved);
    }

    static BarristerChange createBarristerChange(BarristerParty barristerParty, Set<Barrister> barristerAdded,
                                                 Set<Barrister> barristerRemoved) {
        return BarristerChange.builder()
            .barristerParty(barristerParty)
            .added(barristerAdded)
            .removed(barristerRemoved)
            .build();
    }

    private static Barrister barrister(Organisation organisation, String email) {
        return Barrister.builder()
            .userId(UUID.randomUUID().toString())
            .email(email)
            .organisation(organisation)
            .build();
    }
}
