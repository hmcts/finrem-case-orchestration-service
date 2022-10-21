package uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RepresentationUpdate {
    private String party;
    private String name;
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime date;
    private String by;
    private String via;
    private uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain.ChangedRepresentative added;
    private ChangedRepresentative removed;
}
