package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.Solicitor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangeOfRepresentation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangedRepresentative;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ChangeOfRepresentationService {


    public List<ChangeOfRepresentation> generateChangeOfRepresentativesFromSolNoCRequest() {

    }

    private ChangedRepresentative fromSolicitor(Solicitor solicitor) {
        return ChangedRepresentative.builder()
            .name(solicitor.getName())
            .email(solicitor.getEmail())
            .organisation(solicitor.getOrganisation())
            .build();
    }

    private String getClientName(CaseDetails caseDetails) {

    }

    private String getParty(CaseDetails caseDetails) {

    }
}
