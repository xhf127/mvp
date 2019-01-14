package qed.mvp.mapper;

import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import qed.mvp.entity.UserBindImage;

@Mapper
@Repository
@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT, timeout = 36000, rollbackFor = Exception.class)
public interface UserBindImageMapper {

    @Select(value = "SELECT * FROM user_bind_image WHERE user_bind_image_key = #{userBindImageKey}")
    UserBindImage findByuserBindImageKey(int userBindImageKey);

    @Select(value = "SELECT * FROM user_bind_image WHERE user_bind_image.user_key = #{userKey} " +
            " AND user_bind_image.series_key=#{seriesKey}")
    @Results({
            @Result(property = "patient", column = "patient_key", one = @One(select = "qed.mvp.mapper.PatientMapper.findByPatientKey")),
    })
    UserBindImage findByUserKeySeriesKey(@Param("userKey") int userKey, @Param("seriesKey") int seriesKey);

    @Insert("INSERT INTO user_bind_image(series_key, study_key, patient_key, user_key,is_deleted,create_datetime,update_datetime)" +
            " VALUES (#{seriesKey},#{studyKey},#{patientKey},#{userKey},#{isDeleted},#{createDatetime},#{updateDatetime})")
    @Options(useGeneratedKeys = true, keyProperty = "userBindImageKey", keyColumn = "user_bind_image_key")
    void insert(UserBindImage userBindDcm);
}
