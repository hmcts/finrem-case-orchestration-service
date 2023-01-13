package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChildDetailsCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NatureOfApplicationSchedule;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Schedule1OrMatrimonialAndCpList;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ScheduleOneWrapper {

    private Schedule1OrMatrimonialAndCpList typeOfApplication;
    private List<ChildDetailsCollection> childrenCollection;
    private List<NatureOfApplicationSchedule> natureOfApplicationChecklistSchedule;
    private List<NatureOfApplicationSchedule> consentNatureOfApplicationChecklistSchedule;
}
