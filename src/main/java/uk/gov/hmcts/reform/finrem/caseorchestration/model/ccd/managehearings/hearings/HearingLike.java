package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.hearings;

import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Court;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.HearingMode;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.HearingType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.PartyOnCaseCollectionItem;

import java.time.LocalDate;
import java.util.List;

public interface HearingLike {

    LocalDate getHearingDate();

    HearingType getHearingType();

    String getHearingTimeEstimate();

    String getHearingTime();

    Court getHearingCourtSelection();

    HearingMode getHearingMode();

    String getAdditionalHearingInformation();

    YesOrNo getHearingNoticePrompt();

    YesOrNo getAdditionalHearingDocPrompt();

    List<DocumentCollectionItem> getAdditionalHearingDocs();

    List<PartyOnCaseCollectionItem> getPartiesOnCase();

    YesOrNo getWasMigrated();
}
