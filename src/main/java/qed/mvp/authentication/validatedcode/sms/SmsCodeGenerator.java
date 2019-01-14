package qed.mvp.authentication.validatedcode.sms;

import org.springframework.stereotype.Component;
import org.springframework.web.context.request.ServletWebRequest;
import qed.mvp.authentication.validatedcode.ValidateCode;
import qed.mvp.authentication.validatedcode.ValidatedCodeGenerator;

@Component
public class SmsCodeGenerator implements ValidatedCodeGenerator {
    @Override
    public ValidateCode generate(ServletWebRequest request) {
        int randomNum = (int) ((Math.random() * 9 + 1) * 1000);
        String code = String.valueOf(randomNum);
        return new ValidateCode(code, 600);
    }
}
