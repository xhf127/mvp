package qed.mvp.entity;

import lombok.*;
import org.dcm4che3.data.Tag;
import qed.mvp.utils.DateUtil;

import java.util.Date;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class Patient {
    private int patientKey;
    @NonNull
    private String patientId;
    @NonNull
    private String patientName;
    @NonNull
    private String patientGender;
    @NonNull
    private Date patientBirthDate;

    public Patient(DicomReader dicomReader) {
        this.patientId = dicomReader.getDataSet().getString(Tag.PatientID);
        this.patientName = dicomReader.getDataSet().getString(Tag.PatientName);
        this.patientGender = dicomReader.getDataSet().getString(Tag.PatientSex);
        this.patientBirthDate = dicomReader.getDataSet().getDate(Tag.PatientBirthDate) == null ? DateUtil.parseStrToDate("19000101", DateUtil.DATE_FORMAT_YYYYMMDD) : dicomReader.getDataSet().getDate(Tag.PatientBirthDate);
    }
}
