package uk.gov.hmcts.reform.finrem.caseorchestration;

import com.google.common.collect.ImmutableMap;
import feign.FeignException;
import feign.Response;
import org.springframework.http.HttpStatus;

public class SetUpUtils {

    public static  final int STATUS_CODE = HttpStatus.INTERNAL_SERVER_ERROR.value();

    public static FeignException feignError() {
        Response response = Response.builder().status(STATUS_CODE).headers(ImmutableMap.of()).build();
        return FeignException.errorStatus("test", response);
    }
}
