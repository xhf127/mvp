package qed.mvp.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import qed.mvp.entity.Patient;

@Mapper
@Repository
@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT, timeout = 36000, rollbackFor = Exception.class)
public interface PatientMapper {

    @Select(value = "SELECT * FROM patient WHERE patient_id = #{patientId}")
    Patient findByPatientID(String patientId);

    @Select(value = "SELECT * FROM patient WHERE patient_key = #{patientKey}")
    Patient findByPatientKey(int patientKey);

    @Insert("INSERT INTO patient(patient_id, patient_name, patient_gender, patient_birth_date)" +
            " VALUES (#{patientId},#{patientName},#{patientGender},#{patientBirthDate})")
    @Options(useGeneratedKeys = true, keyProperty = "patientKey", keyColumn = "patient_key")
    void insert(Patient patient);

}
