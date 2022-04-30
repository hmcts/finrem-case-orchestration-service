package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangeOfRepresentationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangedRepresentative;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Organisation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RepresentationUpdate;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RepresentationUpdateHistory;



import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Element.element;

public class ChangeOfRepresentationServiceTest extends BaseServiceTest {

    @Autowired private ChangeOfRepresentationService changeOfRepresentationService;

    private ChangeOfRepresentationRequest request;

    @Test
    public void shouldGenerateRepresentationUpdateHistoryWithSingleElement() {
        request = ChangeOfRepresentationRequest.builder()
            .current(null)
            .party("applicant")
            .clientName("John Smith")
            .by("Sir Solicitor")
            .addedRepresentative(ChangedRepresentative.builder()
                .name("Sir Solicitor")
                .email("sirsolicitor1@gmail.com")
                .organisation(Organisation.builder()
                    .organisationID("A31PTVA")
                    .organisationName("FRApplicantSolicitorFirm")
                    .build()).build())
            .removedRepresentative(null)
            .build();

        RepresentationUpdateHistory change = changeOfRepresentationService.generateRepresentationUpdateHistory(request);

        assertThat(change.getRepresentationUpdateHistory()).hasSize(1);
        assertEquals(change.getRepresentationUpdateHistory().get(0).getValue().getAdded(), request.getAddedRepresentative());
        assertEquals(change.getRepresentationUpdateHistory().get(0).getValue().getParty(), request.getParty());
        assertEquals(change.getRepresentationUpdateHistory().get(0).getValue().getClientName(), request.getClientName());
        assertEquals(change.getRepresentationUpdateHistory().get(0).getValue().getBy(),
            request.getAddedRepresentative().getName());
        assertNull(change.getRepresentationUpdateHistory().get(0).getValue().getRemoved());
    }

    @Test
    public void shouldGenerateRepresentationUpdateHistoryWithMultipleElements() {
        RepresentationUpdateHistory current = RepresentationUpdateHistory.builder()
            .representationUpdateHistory(new ArrayList<>(List.of(element(
                UUID.randomUUID(),
                RepresentationUpdate.builder()
                    .party("applicant")
                    .clientName("John Smith")
                    .by("Sir Solicitor")
                    .via("Notice of Change")
                    .date(LocalDate.now())
                    .added(ChangedRepresentative.builder()
                        .name("Sir Solicitor")
                        .email("sirsolicitor1@gmail.com")
                        .organisation(Organisation.builder()
                            .organisationID("A31PTVA")
                            .organisationName("FRApplicantSolicitorFirm")
                            .build()).build())
                    .removed(null)
                    .build())))).build();

        request = ChangeOfRepresentationRequest.builder()
            .current(current)
            .party("respondent")
            .clientName("Jane Smith")
            .by("Test Solicitor")
            .addedRepresentative(ChangedRepresentative.builder()
                .name("Test Solicitor")
                .email("testsolicitor1@gmail.com")
                .organisation(Organisation.builder()
                    .organisationID("A31PTVU")
                    .organisationName("FRRespondentSolicitorFirm")
                    .build()).build())
            .removedRepresentative(null)
            .build();

        RepresentationUpdateHistory change = changeOfRepresentationService.generateRepresentationUpdateHistory(request);

        assertThat(change.getRepresentationUpdateHistory()).hasSize(2);
    }

    @Test
    public void shouldGenerateRepresentationUpdateHistoryWithAddedAndRemoved() {
        request = ChangeOfRepresentationRequest.builder()
            .current(null)
            .party("applicant")
            .clientName("John Smith")
            .by("Sir Solicitor")
            .addedRepresentative(ChangedRepresentative.builder()
                .name("Sir Solicitor")
                .email("sirsolicitor1@gmail.com")
                .organisation(Organisation.builder()
                    .organisationID("A31PTVA")
                    .organisationName("FRApplicantSolicitorFirm")
                    .build()).build())
            .removedRepresentative(ChangedRepresentative.builder()
                .name("Removed Solicitor")
                .email("removedsolicitor1@gmail.com")
                .organisation(Organisation.builder()
                    .organisationID("A31PTVR")
                    .organisationName("FRRemovedSolicitorFirm")
                    .build()).build())
            .build();

        RepresentationUpdateHistory change = changeOfRepresentationService.generateRepresentationUpdateHistory(request);
        assertThat(change.getRepresentationUpdateHistory()).hasSize(1);
        assertEquals(change.getRepresentationUpdateHistory().get(0).getValue().getAdded(), request.getAddedRepresentative());
        assertEquals(change.getRepresentationUpdateHistory().get(0).getValue().getRemoved(),
            request.getRemovedRepresentative());
    }

}
