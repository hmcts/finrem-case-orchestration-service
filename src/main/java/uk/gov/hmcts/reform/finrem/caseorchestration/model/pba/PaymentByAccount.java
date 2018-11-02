package uk.gov.hmcts.reform.finrem.caseorchestration.model.pba;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentByAccount {
    @JsonProperty(value = "payment_accounts")
    private List<String> accountList;
}
