package qed.mvp.authentication.validatedcode.sms;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;
import qed.mvp.authentication.QedAuthenticationFailureHandler;
import qed.mvp.authentication.QedAuthenticationSuccessHandler;
import qed.mvp.authentication.QedUserDetailsService;

@Component
public class SmsCodeAuthenticationSecurityConfig extends SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity> {

    @Autowired
    private QedAuthenticationFailureHandler qedAuthenticationFailureHandler;

    @Autowired
    private QedAuthenticationSuccessHandler qedAuthenticationSuccessHandler;

    @Autowired
    private QedUserDetailsService qedUserDetailsService;

    @Override
    public void configure(HttpSecurity http) {
        //初始化短信登录过滤器
        SmsCodeAuthenticationFilter smsCodeAuthenticationFilter = new SmsCodeAuthenticationFilter();
        smsCodeAuthenticationFilter.setAuthenticationManager(http.getSharedObject(AuthenticationManager.class));
        //设置登录成功处理器
        smsCodeAuthenticationFilter.setAuthenticationSuccessHandler(qedAuthenticationSuccessHandler);
        //设置登录失败处理器
        smsCodeAuthenticationFilter.setAuthenticationFailureHandler(qedAuthenticationFailureHandler);

        //配置Provider
        SmsCodeAuthenticationProvider smsCodeauthenticationProvider = new SmsCodeAuthenticationProvider();
        smsCodeauthenticationProvider.setUserDetailsService(qedUserDetailsService);

        //配置短信登录过滤器
        http.authenticationProvider(smsCodeauthenticationProvider)
                .addFilterAfter(smsCodeAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    }
}
