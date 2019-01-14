package qed.mvp.authentication.validatedcode.sms;

import lombok.Data;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

@Data
public class SmsCodeAuthenticationProvider implements AuthenticationProvider {

    private UserDetailsService userDetailsService;

    /**
     * 身份认证的逻辑
     */
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        //在认证通过前,身份主题 principal存放的是手机号,认证通过后存放的是登录的用户信息
        SmsCodeAuthenticationToken smsCodeAuthenticationToken = (SmsCodeAuthenticationToken) authentication;
        UserDetails user = userDetailsService.loadUserByUsername((String) smsCodeAuthenticationToken.getPrincipal());

        if (user == null) {
            throw new InternalAuthenticationServiceException("用户不存在");
        }

        //构造一个新的Token给Spring,（用户信息,用户的权限）
        SmsCodeAuthenticationToken authenticationResult = new SmsCodeAuthenticationToken(user, user.getAuthorities());
        authenticationResult.setDetails(smsCodeAuthenticationToken.getDetails());


        return authenticationResult;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        //判断传进来的authentication是否是支持的token，支持就用当前类处理登录逻辑
        return SmsCodeAuthenticationToken.class.isAssignableFrom(authentication);
    }

}
