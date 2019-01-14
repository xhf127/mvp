package qed.mvp.entity;

import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
public class User implements UserDetails {
    private int userKey;
    @NonNull
    private String userId;
    @NonNull
    private String mobileNumber;
    @NonNull
    private String password;
    @NonNull
    private String username;
    @NonNull
    private String email;
    @NonNull
    private String hospital;
    @NonNull
    private String department;
    @NonNull
    private String title;
    @NonNull
    private int userStatus;
    @NonNull
    private Date createDatetime;
    @NonNull
    private Date updateDatetime;

    private Collection<? extends GrantedAuthority> auth;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}