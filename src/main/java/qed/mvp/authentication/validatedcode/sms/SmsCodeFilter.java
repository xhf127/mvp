package qed.mvp.authentication.validatedcode.sms;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.social.connect.web.HttpSessionSessionStrategy;
import org.springframework.social.connect.web.SessionStrategy;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.filter.OncePerRequestFilter;
import qed.mvp.authentication.validatedcode.VaildateCodeException;
import qed.mvp.authentication.validatedcode.ValidateCode;

public class SmsCodeFilter extends OncePerRequestFilter implements InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(SmsCodeFilter.class);

    private AuthenticationFailureHandler authenticationFailureHandler;

    private SessionStrategy sessionStrategy = new HttpSessionSessionStrategy();

    private Set<String> urls = new HashSet<>();

    private final static String SESSION_KEY_PREFIX = "SESSION_KEY_FOR_CODE_SMS";

    private final static String SESSION_KEY_MOBILE = "SESSION_KEY_FOR_MOBILE";

    private AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Override
    public void afterPropertiesSet() throws ServletException {
        super.afterPropertiesSet();
        //属性初始化完毕后,把应用配置的需要校验验证码的url加入到Set集合中,在过滤链判断
//        String[] configUrls = StringUtils.splitByWholeSeparatorPreserveAllTokens(securityProperties.getCode().getSms().getUrl(), ",");
//        if (configUrls != null) {
//            for (String url : configUrls) {
//                urls.add(url);
//            }
//        }

        urls.add("/user/register");

        //登录必须校验验证码,所以无论如何都要添加这个Url
        urls.add("/user/login/mobile");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        boolean action = false;

        for (String url : urls) {
            if (antPathMatcher.match(url, request.getRequestURI())) {
                action = true;
            }
        }


        if (action) {
            // 是登录请求才处理验证码
            try {
                log.info("当前url需要校验验证码:" + request.getRequestURI());
                validate(new ServletWebRequest(request));
            } catch (VaildateCodeException e) {
                // 出异常调用登录失败Handler
                authenticationFailureHandler.onAuthenticationFailure(request, response, e);
                return;
            }
        }

        filterChain.doFilter(request, response);

    }

    private void validate(ServletWebRequest request) throws ServletRequestBindingException {

        log.info("开始验证短信验证码");

        ValidateCode codeInSession = (ValidateCode) sessionStrategy.getAttribute(request, SESSION_KEY_PREFIX);
        String mobileInSession = (String) sessionStrategy.getAttribute(request, SESSION_KEY_MOBILE);

        String codeInRequest = ServletRequestUtils.getStringParameter(request.getRequest(), "smsCode");

        String mobileInRequest = ServletRequestUtils.getStringParameter(request.getRequest(), "mobile");

        if (mobileInSession == null) {
            log.info("no验证码");
            throw new VaildateCodeException("您输入的手机号或验证码错误，请重新获取验证码");
        }

        if (!StringUtils.equalsIgnoreCase(mobileInSession, mobileInRequest)) {
            log.info("手机不一致");
            throw new VaildateCodeException("您输入的手机号或验证码错误");
        }

        if (StringUtils.isBlank(codeInRequest)) {
            //登录请求的验证码为空
            log.info("验证码不能为空");
            throw new VaildateCodeException("验证码不能为空");
        }

        if (codeInSession == null) {
            //Session中验证码不存在
            log.info("验证码不存在");
            throw new VaildateCodeException("验证码不存在");
        }

        if (codeInSession.isExpired()) {
            //移除过期的验证码
            log.info("验证码已过期");
            sessionStrategy.removeAttribute(request, SESSION_KEY_PREFIX);
            throw new VaildateCodeException("您输入的验证码超时，请重新获取验证码");
        }

        if (!StringUtils.equalsIgnoreCase(codeInSession.getCode(), codeInRequest)) {
            log.info("验证码不匹配");
            throw new VaildateCodeException("您输入的验证码错误");
        }

        //验证码验证通过(需要修改为登录成功后再移除验证码)
        log.info("验证码校验通过");
        sessionStrategy.removeAttribute(request, SESSION_KEY_PREFIX);
        sessionStrategy.removeAttribute(request, SESSION_KEY_MOBILE);
    }

    public AuthenticationFailureHandler getAuthenticationFailureHandler() {
        return authenticationFailureHandler;
    }

    public void setAuthenticationFailureHandler(AuthenticationFailureHandler authenticationFailureHandler) {
        this.authenticationFailureHandler = authenticationFailureHandler;
    }

    public SessionStrategy getSessionStrategy() {
        return sessionStrategy;
    }

    public void setSessionStrategy(SessionStrategy sessionStrategy) {
        this.sessionStrategy = sessionStrategy;
    }

    public Set<String> getUrls() {
        return urls;
    }

    public void setUrls(Set<String> urls) {
        this.urls = urls;
    }

}
