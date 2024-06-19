package uk.gov.hmcts.reform.finrem.caseorchestration.utils;

import static org.apache.commons.lang3.StringUtils.SPACE;
import static org.apache.commons.lang3.StringUtils.defaultString;

public enum StringDecorator {
    NO_SPACE {
        @Override
        public String decorate(String str) {
            return str;
        }
    },
    TRAILING_SPACE {
        @Override
        public String decorate(String str) {
            return defaultString(str) + SPACE;
        }
    },
    LEADING_SPACE {
        @Override
        public String decorate(String str) {
            return SPACE + defaultString(str);
        }
    },
    ENCLOSED_WITH_SPACES {
        @Override
        public String decorate(String str) {
            return SPACE + defaultString(str) + SPACE;
        }
    };

    public abstract String decorate(String str);
}
