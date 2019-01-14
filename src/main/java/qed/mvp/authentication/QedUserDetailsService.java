package qed.mvp.authentication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import qed.mvp.entity.User;
import qed.mvp.mapper.UserMapper;

@Component
public class QedUserDetailsService implements UserDetailsService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String mobile) throws UsernameNotFoundException {
        //todo:查找用户,设置权限
        User user = userMapper.findUserByMobileNumber(mobile);
        if (user == null) {
            throw new BadCredentialsException("您输入的手机号不存在");
        }
//        List<GrantedAuthority> arrayAuths = AuthorityUtils.commaSeparatedStringToAuthorityList("admin");
//        user.setAuth(arrayAuths);
        return user;
    }
}
