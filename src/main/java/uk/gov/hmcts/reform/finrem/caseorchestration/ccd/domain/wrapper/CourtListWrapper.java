package uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain.wrapper;

import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain.BedfordshireCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain.BirminghamCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain.BristolCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain.CfcCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain.ClevelandCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain.DevonCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain.DorsetCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain.HumberCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain.KentSurreyCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain.LancashireCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain.LiverpoolCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain.ManchesterCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain.NewportCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain.NorthWalesCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain.NottinghamCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain.NwYorkshireCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain.SwanseaCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain.ThamesValleyCourt;

public interface CourtListWrapper {
    NottinghamCourt getNottinghamCourt();

    CfcCourt getCfcCourt();

    BirminghamCourt getBirminghamCourt();

    LiverpoolCourt getLiverpoolCourt();

    ManchesterCourt getManchesterCourt();

    LancashireCourt getLancashireCourt();

    ClevelandCourt getClevelandCourt(boolean isConsented);

    NwYorkshireCourt getNwYorkshireCourt();

    HumberCourt getHumberCourt();

    KentSurreyCourt getKentSurreyCourt();

    BedfordshireCourt getBedfordshireCourt();

    ThamesValleyCourt getThamesValleyCourt();

    DevonCourt getDevonCourt();

    DorsetCourt getDorsetCourt();

    BristolCourt getBristolCourt();

    NewportCourt getNewportCourt();

    SwanseaCourt getSwanseaCourt();

    NorthWalesCourt getNorthWalesCourt();

}
