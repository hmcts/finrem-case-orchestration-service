package uk.gov.hmcts.reform.finrem.caseorchestration.service.managehearings.postalhearingdocuments;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.HearingType;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static uk.gov.hmcts.reform.finrem.caseorchestration.service.managehearings.postalhearingdocuments.HearingTypeResolver.getByWorkingHearingAndDocumentType;

@Component
class FdaHearingDocumentSupplier implements PostalHearingDocumentSupplier {

    @Override
    public List<CaseDocument> get(FinremCaseDetails finremCaseDetails) {
        HearingType workingHearingType = finremCaseDetails.getData().getManageHearingsWrapper().getWorkingHearing().getHearingType();

        if (!HearingType.FDA.equals(workingHearingType)) {
            return Collections.emptyList();
        }

        CaseDocument formC = finremCaseDetails.getData().isFastTrackApplication()
            ? getByWorkingHearingAndDocumentType(finremCaseDetails, CaseDocumentType.FORM_C_FAST_TRACK)
            : getByWorkingHearingAndDocumentType(finremCaseDetails, CaseDocumentType.FORM_C);

        CaseDocument formG = finremCaseDetails.getData().isFastTrackApplication()
            ? null
            : getByWorkingHearingAndDocumentType(finremCaseDetails, CaseDocumentType.FORM_G);

        return Stream.of(formC, formG)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }
}
