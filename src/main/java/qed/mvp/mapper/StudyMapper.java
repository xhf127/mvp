package qed.mvp.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import qed.mvp.entity.Study;

@Mapper
@Repository
@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT, timeout = 36000, rollbackFor = Exception.class)
public interface StudyMapper {

    @Select(value = "SELECT * FROM study WHERE study.study_instance_uid = #{studyInstanceUID}")
    Study findByStudyInstanceUID(String studyInstanceUID);

    @Select(value = "SELECT * FROM study WHERE study.study_key = #{studyKey}")
    Study findByStudyKey(int studyKey);

    @Insert(value = "INSERT INTO study(patient_key, study_instance_uid, study_id, accession_number,study_date,study_time,study_description)" +
            " VALUES (#{patientKey},#{studyInstanceUid},#{studyId},#{accessionNumber},#{studyDate},#{studyTime},#{studyDescription})")
    @Options(useGeneratedKeys = true, keyProperty = "studyKey", keyColumn = "study_key")
    void insert(Study study);

}
