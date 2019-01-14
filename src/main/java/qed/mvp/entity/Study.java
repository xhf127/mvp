package qed.mvp.entity;

import lombok.*;
import org.dcm4che3.data.Tag;

import java.util.Date;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class Study {

    private int studyKey;
    @NonNull
    private int patientKey;
    @NonNull
    private String studyInstanceUid;
    @NonNull
    private String studyId;
    @NonNull
    private String accessionNumber;
    @NonNull
    private Date studyDate;
    @NonNull
    private Date studyTime;
    @NonNull
    private String studyDescription;

    public Study(DicomReader dicomReader, Patient patient) {
        this.patientKey = patient.getPatientKey();
        this.studyInstanceUid = dicomReader.getDataSet().getString(Tag.StudyInstanceUID);
        this.studyId = dicomReader.getDataSet().getString(Tag.StudyID);
        this.accessionNumber = dicomReader.getDataSet().getString(Tag.AccessionNumber);
        this.studyDate = dicomReader.getDataSet().getDate(Tag.StudyDate);
        this.studyTime = dicomReader.getDataSet().getDate(Tag.StudyTime);
        this.studyDescription = dicomReader.getDataSet().getString(Tag.StudyDescription);
    }
}
