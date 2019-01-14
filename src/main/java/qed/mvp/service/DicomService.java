package qed.mvp.service;

import org.dcm4che3.data.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import qed.mvp.entity.*;
import qed.mvp.mapper.PatientMapper;
import qed.mvp.mapper.SeriesMapper;
import qed.mvp.mapper.StudyMapper;
import qed.mvp.mapper.UserBindImageMapper;

import java.util.Date;

@Service
public class DicomService {

    private static final Logger log = LoggerFactory.getLogger(DicomService.class);

    @Autowired
    private PatientMapper patientMapper;

    @Autowired
    private StudyMapper studyMapper;

    @Autowired
    private SeriesMapper seriesMapper;

    @Autowired
    private UserBindImageMapper userBindImageMapper;

    private Patient buildPatient(DicomReader reader) {

        String patientId = reader.getDataSet().getString(Tag.PatientID);

        Patient patient = patientMapper.findByPatientID(patientId);
        if (patient == null) {
            patient = new Patient(reader);
            patientMapper.insert(patient);
            patient = patientMapper.findByPatientKey(patient.getPatientKey());
        } else {
            log.info("Patient already exists");
        }
        return patient;
    }

    private Study buildStudy(DicomReader reader, Patient patient) {

        String studyInstanceUID = reader.getDataSet().getString(Tag.StudyInstanceUID);

        Study study = studyMapper.findByStudyInstanceUID(studyInstanceUID);
        if (study == null) {
            study = new Study(reader, patient);
            studyMapper.insert(study);
            study = studyMapper.findByStudyKey(study.getStudyKey());
        } else {
            log.info("Study already exists");
        }
        return study;
    }

    private Series buildSeries(int userKey, DicomReader reader, Patient patient, Study study, String seriesPath, float patientWeight,
                               int imagingInterval, float radionuclideHalfLife, float radionuclideTotalDose,
                               String radiopharmaceutical, Date radiopharmaceuticalStartDate, Date radiopharmaceuticalStartTime, int imageCount) {

        String studyInstanceUid = reader.getDataSet().getString(Tag.SeriesInstanceUID);
        String seriesNumber = reader.getDataSet().getString(Tag.SeriesNumber);

        Series series = seriesMapper.findBySeriesInstanceUID(studyInstanceUid, seriesNumber, seriesPath);
        Series new_series = new Series(reader, study, seriesPath, patientWeight, imagingInterval, radionuclideHalfLife,
                radionuclideTotalDose, radiopharmaceutical, radiopharmaceuticalStartDate, radiopharmaceuticalStartTime, imageCount);
        if (series == null) {
            seriesMapper.insert(new_series);
            series = seriesMapper.findBySeriesKey(new_series.getSeriesKey());
            //series不存在,绑定关系不存在
            UserBindImage userBindImage = new UserBindImage(series.getSeriesKey(), study.getStudyKey(), patient.getPatientKey(), userKey, 0, new Date(), new Date());
            userBindImageMapper.insert(userBindImage);
        } else {
            log.info("Series already exists");
            seriesMapper.changeImageCount(new_series.getImageCount(),series.getSeriesKey());
            //series存在,绑定关系查询
            UserBindImage userBindImage = userBindImageMapper.findByUserKeySeriesKey(userKey, series.getSeriesKey());
            if (userBindImage == null) {
                userBindImage = new UserBindImage(series.getSeriesKey(), study.getStudyKey(), patient.getPatientKey(), userKey, 0, new Date(), new Date());
                userBindImageMapper.insert(userBindImage);
            } else {
                log.info("userBindImage already exists");
            }
        }
        return series;
    }


    public Series buildEntities(int userKey, DicomReader reader, String seriesPath, float patientWeight,
                                int imagingInterval, float radionuclideHalfLife, float radionuclideTotalDose,
                                String radiopharmaceutical, Date radiopharmaceuticalStartDate, Date radiopharmaceuticalStartTime, int imageCount) {

        Series series = null;

        Patient patient = buildPatient(reader);
        if (patient != null) {
            Study study = buildStudy(reader, patient);
            if (study != null) {
                series = buildSeries(userKey, reader, patient, study, seriesPath, patientWeight, imagingInterval, radionuclideHalfLife,
                        radionuclideTotalDose, radiopharmaceutical, radiopharmaceuticalStartDate, radiopharmaceuticalStartTime, imageCount);
            }
        }
        return series;
    }


}
