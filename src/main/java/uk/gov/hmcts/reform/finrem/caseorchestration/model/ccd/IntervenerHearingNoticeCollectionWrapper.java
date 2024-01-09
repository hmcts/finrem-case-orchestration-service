package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class IntervenerHearingNoticeCollectionWrapper {
    private List<IntervenerHearingNoticeCollection> intv1HearingNoticesCollection;
    private List<IntervenerHearingNoticeCollection> intv2HearingNoticesCollection;
    private List<IntervenerHearingNoticeCollection> intv3HearingNoticesCollection;
    private List<IntervenerHearingNoticeCollection> intv4HearingNoticesCollection;
}