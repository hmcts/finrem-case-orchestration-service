package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper;

import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.BedfordshireCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.BirminghamCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.BristolCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CfcCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ClevelandCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DevonCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DorsetCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HighCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HumberCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.KentSurreyCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.LancashireCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.LiverpoolCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ManchesterCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NewportCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NorthWalesCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NottinghamCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NwYorkshireCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.SwanseaCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ThamesValleyCourt;

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

    HighCourt getHighCourt();

}
