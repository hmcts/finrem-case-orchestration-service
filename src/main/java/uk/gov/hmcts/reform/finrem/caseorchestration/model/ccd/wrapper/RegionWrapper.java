package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RegionWrapper {
    @JsonUnwrapped
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.PUBLIC)
    DefaultRegionWrapper defaultRegionWrapper;
    @JsonUnwrapped
    @Getter(AccessLevel.NONE)
    InterimRegionWrapper interimRegionWrapper;
    @JsonUnwrapped
    @Getter(AccessLevel.NONE)
    GeneralApplicationRegionWrapper generalApplicationRegionWrapper;

    @JsonIgnore
    public DefaultRegionWrapper getDefaultRegionWrapper() {
        if (defaultRegionWrapper == null) {
            this.defaultRegionWrapper = new DefaultRegionWrapper();
        }
        return defaultRegionWrapper;
    }

    @JsonIgnore
    public InterimRegionWrapper getInterimRegionWrapper() {
        if (interimRegionWrapper == null) {
            this.interimRegionWrapper = new InterimRegionWrapper();
        }
        return interimRegionWrapper;
    }

    @JsonIgnore
    public GeneralApplicationRegionWrapper getGeneralApplicationRegionWrapper() {
        if (generalApplicationRegionWrapper == null) {
            this.generalApplicationRegionWrapper = new GeneralApplicationRegionWrapper();
        }
        return generalApplicationRegionWrapper;
    }

    @JsonIgnore
    public DefaultCourtListWrapper getDefaultCourtList() {
        if (defaultRegionWrapper == null) {
            this.defaultRegionWrapper = new DefaultRegionWrapper();
            this.defaultRegionWrapper.setCourtListWrapper(new DefaultCourtListWrapper());
        }
        return defaultRegionWrapper.getDefaultCourtListWrapper();
    }

    @JsonIgnore
    public InterimCourtListWrapper getInterimCourtList() {
        if (interimRegionWrapper == null) {
            this.interimRegionWrapper = new InterimRegionWrapper();
            this.interimRegionWrapper.setCourtListWrapper(new InterimCourtListWrapper());
        }

        return interimRegionWrapper.getCourtListWrapper();
    }

    @JsonIgnore
    public GeneralApplicationCourtListWrapper getGeneralApplicationCourtList() {
        if (this.generalApplicationRegionWrapper == null) {
            this.generalApplicationRegionWrapper = new GeneralApplicationRegionWrapper();
            this.generalApplicationRegionWrapper.setCourtListWrapper(new GeneralApplicationCourtListWrapper());
        }

        return generalApplicationRegionWrapper.getCourtListWrapper();
    }
}
