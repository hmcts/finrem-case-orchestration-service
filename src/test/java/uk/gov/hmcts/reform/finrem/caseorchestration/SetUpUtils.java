package uk.gov.hmcts.reform.finrem.caseorchestration;

import com.google.common.collect.ImmutableMap;
import feign.FeignException;
import feign.Response;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.InvalidCaseDataException;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.fee.FeeResponse;

import java.math.BigDecimal;

public class SetUpUtils {

    public static  final int INTERNAL_SERVER_ERROR = HttpStatus.INTERNAL_SERVER_ERROR.value();
    public static  final int BAD_REQUEST = HttpStatus.BAD_REQUEST.value();

    public static FeignException feignError() {
        Response response = Response.builder().status(INTERNAL_SERVER_ERROR).headers(ImmutableMap.of()).build();
        return FeignException.errorStatus("test", response);
    }

    public static InvalidCaseDataException invalidCaseDataError() {
        return new InvalidCaseDataException(BAD_REQUEST, "Bad request");
    }

    public static FeeResponse fee() {
        FeeResponse feeResponse = new FeeResponse();
        feeResponse.setCode("FEE0640");
        feeResponse.setDescription("finrem");
        feeResponse.setFeeAmount(BigDecimal.valueOf(10d));
        feeResponse.setVersion("v1");
        return feeResponse;
    }

}
