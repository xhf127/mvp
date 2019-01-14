package qed.mvp.entity;

import lombok.*;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@RequiredArgsConstructor
public class UserBindImage {
    private int userBindImageKey;
    @NonNull
    private int seriesKey;
    @NonNull
    private int studyKey;
    @NonNull
    private int patientKey;
    @NonNull
    private int userKey;
    @NonNull
    private int isDeleted;
    @NonNull
    private Date createDatetime;
    @NonNull
    private Date updateDatetime;

    private Patient patient;
}
