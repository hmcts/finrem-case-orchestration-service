package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangeOfRepresentation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangeOfRepresentationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangeOfRepresentatives;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangedRepresentative;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Organisation;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ChangeOfRepresentationServiceTest extends BaseServiceTest {

    @Autowired private ChangeOfRepresentationService changeOfRepresentationService;

    private ChangeOfRepresentationRequest request;

    @Test
    public void shouldGenerateChangeOfRepresentativesWithSingleElement() {
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

        ChangeOfRepresentatives change = changeOfRepresentationService.generateChangeOfRepresentatives(request);

        assertThat(change.getChangeOfRepresentation()).hasSize(1);
        assertEquals(change.getChangeOfRepresentation().get(0).getAdded(), request.getAddedRepresentative());
        assertEquals(change.getChangeOfRepresentation().get(0).getParty(), request.getParty());
        assertEquals(change.getChangeOfRepresentation().get(0).getClientName(), request.getClientName());
        assertEquals(change.getChangeOfRepresentation().get(0).getBy(), request.getAddedRepresentative().getName());
        assertNull(change.getChangeOfRepresentation().get(0).getRemoved());
    }

    @Test
    public void shouldGenerateChangeOfRepresentativesWithMultipleElements() {
        ChangeOfRepresentatives current = ChangeOfRepresentatives.builder()
            .changeOfRepresentation(new ArrayList<>(Arrays.asList(
            ChangeOfRepresentation.builder()
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
                .build()))).build();

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

        ChangeOfRepresentatives change = changeOfRepresentationService.generateChangeOfRepresentatives(request);

        assertThat(change.getChangeOfRepresentation()).hasSize(2);
    }

    @Test
    public void shouldGenerateChangeOfRepresentativesWithAddedAndRemoved() {
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

        ChangeOfRepresentatives change = changeOfRepresentationService.generateChangeOfRepresentatives(request);
        assertThat(change.getChangeOfRepresentation()).hasSize(1);
        assertEquals(change.getChangeOfRepresentation().get(0).getAdded(), request.getAddedRepresentative());
        assertEquals(change.getChangeOfRepresentation().get(0).getRemoved(), request.getRemovedRepresentative());
    }

}
