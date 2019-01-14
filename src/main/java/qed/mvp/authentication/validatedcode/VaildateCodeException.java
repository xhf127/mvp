package qed.mvp.authentication.validatedcode;

import org.springframework.security.core.AuthenticationException;

public class VaildateCodeException extends AuthenticationException {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public VaildateCodeException(String msg) {
        super(msg);
    }
}
