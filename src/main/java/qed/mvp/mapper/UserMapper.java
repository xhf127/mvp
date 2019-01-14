package qed.mvp.mapper;

import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import qed.mvp.entity.User;

import java.util.Date;
import java.util.List;

@Mapper
@Repository
@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT, timeout = 36000, rollbackFor = Exception.class)
public interface UserMapper {

    @Select("SELECT * FROM user")
    List<User> findAll();

    @Select(value = "SELECT * FROM user WHERE user.username = #{username}")
    User findUserByName(String username);

    @Select(value = "SELECT * FROM user WHERE user.user_id = #{userId}")
    User findUserByUserId(String userId);

    @Select(value = "SELECT * FROM user WHERE user.mobile_number = #{mobileNumber}")
    User findUserByMobileNumber(String mobileNumber);

    @Insert("INSERT INTO user(user_id, mobile_number, password, username, email, hospital, department, title, user_status," +
            "create_datetime, update_datetime) " +
            "VALUES(#{userId},#{mobileNumber}, #{password},#{username},#{email},#{hospital},#{department},#{title}," +
            "#{userStatus},#{createDatetime},#{updateDatetime})")
    int insert(User user);

    @Update("UPDATE user SET username=#{username},email=#{email},hospital=#{hospital}," +
            "department=#{department},title=#{title} ,update_datetime=#{update_datetime} " +
            "WHERE user_key = #{userKey}")
    int changeUserInfo(@Param("username") String username,
                       @Param("email") String email, @Param("hospital") String hospital,
                       @Param("department") String department, @Param("title") String title,
                       @Param("update_datetime") Date update_datetime, @Param("userKey") int userKey);

    @Update("UPDATE user SET password=#{password} ,update_datetime=#{update_datetime} WHERE user_key = #{userKey}")
    int changePassword(@Param("password") String password,@Param("update_datetime") Date update_datetime, @Param("userKey") int userKey);


}
