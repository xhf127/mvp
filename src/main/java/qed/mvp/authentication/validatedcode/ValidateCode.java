package qed.mvp.authentication.validatedcode;


import java.time.LocalDateTime;

public class ValidateCode {
    private String code;

    private LocalDateTime expiredTime;

    public ValidateCode(String code, int expiredIn) {
        this.code = code;
        this.expiredTime = LocalDateTime.now().plusSeconds(expiredIn);
    }

    public ValidateCode(String code, LocalDateTime expiredTime) {
        this.code = code;
        this.expiredTime = expiredTime;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiredTime);
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public LocalDateTime getExpiredTime() {
        return expiredTime;
    }

    public void setExpiredTime(LocalDateTime expiredTime) {
        this.expiredTime = expiredTime;
    }
}
