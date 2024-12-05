package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener;

import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Address;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrganisationPolicy;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;

import java.time.LocalDate;

public interface IntervenerDetails {

    String getIntervenerName();

    String getIntervenerEmail();

    String getIntervenerPhone();

    String getIntervenerSolName();

    String getIntervenerSolEmail();

    String getIntervenerSolPhone();

    String getIntervenerSolicitorFirm();

    String getIntervenerSolicitorReference();

    OrganisationPolicy getIntervenerOrganisation();

    Address getIntervenerAddress();

    YesOrNo getIntervenerRepresented();

    YesOrNo getIntervenerResideOutsideUK();

    LocalDate getIntervenerDateAdded();

    YesOrNo getIntervenerAddressConfidential();

    YesOrNo getIntervenerInRefuge();

    void setIntervenerName(String intervenerName);

    void setIntervenerEmail(String intervenerEmail);

    void setIntervenerPhone(String intervenerPhone);

    void setIntervenerSolName(String intervenerSolName);

    void setIntervenerSolEmail(String intervenerSolEmail);

    void setIntervenerSolPhone(String intervenerSolPhone);

    void setIntervenerSolicitorFirm(String intervenerSolicitorFirm);

    void setIntervenerSolicitorReference(String intervenerSolicitorReference);

    void setIntervenerOrganisation(OrganisationPolicy intervenerOrganisation);

    void setIntervenerAddress(Address intervenerAddress);

    void setIntervenerRepresented(YesOrNo intervenerRepresented);

    void setIntervenerResideOutsideUK(YesOrNo intervenerResideOutsideUK);

    void setIntervenerDateAdded(LocalDate intervenerDateAdded);

    void setIntervenerAddressConfidential(YesOrNo intervenerAddressConfidential);

    void setIntervenerInRefuge(YesOrNo intervenerInRefuge);
}
