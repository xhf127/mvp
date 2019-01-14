package qed.mvp.entity;

import lombok.*;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@RequiredArgsConstructor
public class AnalyzeResult {

    private int analyzeResultKey;
    @NonNull
    private int userKey;
    @NonNull
    private int brainAtlasKey;
    @NonNull
    private int normalControlKey;
    @NonNull
    private int seriesKey;

    @NonNull
    private String statisticalMethod;
    @NonNull
    private String statisticalMode;
    @NonNull
    private int abnormalVolume;

    @NonNull
    private float patientWeight;
    @NonNull
    private Date acquisitionDate;
    @NonNull
    private Date acquisitionTime;
    @NonNull
    private Date radiopharmaceuticalStartDate;
    @NonNull
    private Date radiopharmaceuticalStartTime;
    @NonNull
    private float radionuclideTotalDose;
    @NonNull
    private float radionuclideHalfLife;
    @NonNull
    private double pValue;
    @NonNull
    private double tValue;
    @NonNull
    private int imagingInterval;
    @NonNull
    private int analyzeStatus;

    private String errCode;
    private String sourcePath;
    private String normalizedImagePath;
    private String suvTPath;
    private String bqTPath;

    @NonNull
    private Date createDatetime;
    @NonNull
    private Date updateDatetime;

    private Series series;
    private String brainAtlasPath;
}
