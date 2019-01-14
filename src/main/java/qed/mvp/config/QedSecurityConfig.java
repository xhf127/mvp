package qed.mvp.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import qed.mvp.authentication.QedAuthenticationFailureHandler;
import qed.mvp.authentication.QedAuthenticationSuccessHandler;
import qed.mvp.authentication.validatedcode.sms.SmsCodeAuthenticationSecurityConfig;
import qed.mvp.authentication.validatedcode.sms.SmsCodeFilter;

@Configuration
public class QedSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private QedAuthenticationSuccessHandler qedAuthenticationSuccessHandler;

    @Autowired
    private QedAuthenticationFailureHandler qedAuthenticationFailureHandler;

    @Autowired
    private SmsCodeAuthenticationSecurityConfig smsCodeAuthenticationSecurityConfig;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        SmsCodeFilter smsCodeFilter = new SmsCodeFilter();
        smsCodeFilter.setAuthenticationFailureHandler(qedAuthenticationFailureHandler);
        smsCodeFilter.afterPropertiesSet();

        http
                .csrf().disable()
                .addFilterBefore(smsCodeFilter, UsernamePasswordAuthenticationFilter.class)
                .formLogin()
                .loginPage("/login")
                .loginProcessingUrl("/user/login")
                .successHandler(qedAuthenticationSuccessHandler)
                .failureHandler(qedAuthenticationFailureHandler)
                .and()
                .authorizeRequests()
                .antMatchers("/assets/**", "/**.ico", "/js/**", "/css/**", "/image/**").permitAll()
                .antMatchers("/about.html","/", "/login", "/logout", "/user/sms", "/user/register", "/user/registerSms", "/user/loginSms").permitAll()
                // swagger start
                .antMatchers("/swagger-ui.html").permitAll()
                .antMatchers("/swagger-resources/**").permitAll()
                .antMatchers("/images/**").permitAll()
                .antMatchers("/webjars/**").permitAll()
                .antMatchers("/v2/api-docs/**").permitAll()
                .antMatchers("/configuration/ui").permitAll()
                .antMatchers("/configuration/security").permitAll()
                // swagger end
                .anyRequest().authenticated()
                .and()
                .logout()
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login")
                .deleteCookies("JSESSIONID")
                .invalidateHttpSession(true)
                .permitAll()
                .and()
                .apply(smsCodeAuthenticationSecurityConfig);
//                .and()
//                .sessionManagement().maximumSessions(1).maxSessionsPreventsLogin(true);
    }
}
