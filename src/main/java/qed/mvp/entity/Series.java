package qed.mvp.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.dcm4che3.data.Tag;

import java.util.Date;

@Data
public class Series {
    private int seriesKey;
    private int studyKey;
    private String seriesInstanceUid;
    private String seriesNumber;
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date seriesDate;
    @JsonFormat(pattern = "HH:mm:ss", timezone = "GMT+8")
    private Date seriesTime;
    private String seriesDescription;
    private String modality;
    private String manufacturer;
    private String stationName;
    private String institutionName;
    private Date acquisitionDate;
    private Date acquisitionTime;
    private Date radiopharmaceuticalStartDate;
    private Date radiopharmaceuticalStartTime;
    private float radionuclideTotalDose;
    private float radionuclideHalfLife;
    private String radiopharmaceutical;
    private float patientWeight;
    private int imagingInterval;

    private float sliceThickness;
    private double pixelSpacing;
    private String windowCenter;
    private String windowWidth;
    private double rescaleIntercept;
    private double rescaleSlope;
    private String seriesPath;
    private String studyAge;
    private int imageCount;
    private Date createDatetime;
    private Date updateDatetime;

    private Patient patient;


    public Series(Study study, String seriesInstanceUid, String seriesNumber, Date seriesDate, Date seriesTime,
                  String seriesDescription, String modality, String manufacturer, String stationName,
                  String institutionName, Date acquisitionDate, Date acquisitionTime, Date radiopharmaceuticalStartDate,
                  Date radiopharmaceuticalStartTime, float radionuclideTotalDose, float radionuclideHalfLife,
                  String radiopharmaceutical, float patientWeight, int imagingInterval, float sliceThickness,
                  double pixelSpacing, String windowCenter, String windowWidth, double rescaleIntercept,
                  double rescaleSlope, String seriesPath, String studyAge, int imageCount) {
        this.studyKey = study.getStudyKey();
        this.seriesInstanceUid = seriesInstanceUid;
        this.seriesNumber = seriesNumber;
        this.seriesDate = seriesDate;
        this.seriesTime = seriesTime;
        this.seriesDescription = seriesDescription;
        this.modality = modality;
        this.manufacturer = manufacturer;
        this.stationName = stationName;
        this.institutionName = institutionName;
        this.acquisitionDate = acquisitionDate;
        this.acquisitionTime = acquisitionTime;
        this.radiopharmaceuticalStartDate = radiopharmaceuticalStartDate;
        this.radiopharmaceuticalStartTime = radiopharmaceuticalStartTime;
        this.radionuclideTotalDose = radionuclideTotalDose;
        this.radionuclideHalfLife = radionuclideHalfLife;
        this.radiopharmaceutical = radiopharmaceutical;
        this.patientWeight = patientWeight;
        this.imagingInterval = imagingInterval;
        this.sliceThickness = sliceThickness;
        this.pixelSpacing = pixelSpacing;
        this.windowCenter = windowCenter;
        this.windowWidth = windowWidth;
        this.rescaleIntercept = rescaleIntercept;
        this.rescaleSlope = rescaleSlope;
        this.seriesPath = seriesPath;
        this.studyAge = studyAge;
        this.imageCount = imageCount;
        this.createDatetime = new Date();
        this.updateDatetime = new Date();
    }

    public Series(DicomReader dicomReader, Study study, String seriesPath,
                  float patientWeight, int imagingInterval, float radionuclideHalfLife, float radionuclideTotalDose,
                  String radiopharmaceutical, Date radiopharmaceuticalStartDate, Date radiopharmaceuticalStartTime, int imageCount) {
        this.studyKey = study.getStudyKey();
        this.seriesInstanceUid = dicomReader.getDataSet().getString(Tag.SeriesInstanceUID);
        this.seriesNumber = dicomReader.getDataSet().getString(Tag.SeriesNumber);
        this.seriesDate = dicomReader.getDataSet().getDate(Tag.SeriesDate);
        this.seriesTime = dicomReader.getDataSet().getDate(Tag.SeriesTime);
        this.seriesDescription = dicomReader.getDataSet().getString(Tag.SeriesDescription);

        //modality
        String modality = dicomReader.getDataSet().getString(Tag.Modality);
        if (modality.equals("PT")) {
            this.modality = "PET";
        } else if (modality.equals("NM")) {
            this.modality = "SPECT";
        }

        this.radionuclideTotalDose = radionuclideTotalDose;
        this.radionuclideHalfLife = radionuclideHalfLife;
        this.patientWeight = patientWeight;
        this.imagingInterval = imagingInterval;
        this.seriesPath = seriesPath;
        this.radiopharmaceuticalStartDate = radiopharmaceuticalStartDate;
        this.radiopharmaceuticalStartTime = radiopharmaceuticalStartTime;
        this.radiopharmaceutical = radiopharmaceutical;
        if (imageCount == 1) {
            this.imageCount = dicomReader.getDataSet().getInt(Tag.NumberOfFrames, 1);
        } else {
            this.imageCount = imageCount;
        }

        this.manufacturer = dicomReader.getDataSet().getString(Tag.Manufacturer);
        this.stationName = dicomReader.getDataSet().getString(Tag.StationName);
        this.institutionName = dicomReader.getDataSet().getString(Tag.InstitutionName);
        this.acquisitionDate = dicomReader.getDataSet().getDate(Tag.AcquisitionDate);
        this.acquisitionTime = dicomReader.getDataSet().getDate(Tag.AcquisitionTime);
        this.sliceThickness = dicomReader.getDataSet().getFloat(Tag.SliceThickness, 0);
        this.pixelSpacing = dicomReader.getDataSet().getDouble(Tag.PixelSpacing, 0);
        this.windowCenter = dicomReader.getDataSet().getString(Tag.WindowCenter);
        this.windowWidth = dicomReader.getDataSet().getString(Tag.WindowWidth);
        this.rescaleIntercept = dicomReader.getDataSet().getDouble(Tag.RescaleIntercept, 0);
        this.rescaleSlope = dicomReader.getDataSet().getDouble(Tag.RescaleSlope, 0);
        this.studyAge = dicomReader.getDataSet().getString(Tag.PatientAge) == null ? "-Y" : dicomReader.getDataSet().getString(Tag.PatientAge);
        this.createDatetime = new Date();
        this.updateDatetime = new Date();

    }
}
