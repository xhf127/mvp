package qed.mvp.authentication.validatedcode;

import org.springframework.stereotype.Component;
import org.springframework.web.context.request.ServletWebRequest;

@Component
public interface ValidatedCodeGenerator {
    ValidateCode generate(ServletWebRequest request);
}
