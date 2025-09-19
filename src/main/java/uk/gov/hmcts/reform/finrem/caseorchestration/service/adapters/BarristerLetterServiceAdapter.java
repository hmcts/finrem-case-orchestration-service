package uk.gov.hmcts.reform.finrem.caseorchestration.service.adapters;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.BarristerLetterTuple;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Barrister;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.barristers.BarristerLetterService;

@Service
@RequiredArgsConstructor
public class BarristerLetterServiceAdapter {
    private final BarristerLetterService barristerLetterService;
    private final FinremCaseDetailsMapper finremCaseDetailsMapper;

    public void sendBarristerLetter(FinremCaseDetails caseDetails, Barrister barrister,
                                    BarristerLetterTuple barristerLetterTuple, String authToken) {
        CaseDetails adapterCaseDetails = finremCaseDetailsMapper.mapToCaseDetails(caseDetails);
        barristerLetterService.sendBarristerLetter(adapterCaseDetails, barrister, barristerLetterTuple, authToken);
    }
}
