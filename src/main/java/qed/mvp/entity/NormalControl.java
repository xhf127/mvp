package qed.mvp.entity;

import lombok.Data;
import lombok.NonNull;

import java.util.Date;

@Data
public class NormalControl {

    private int normalControlKey;
    @NonNull
    private String normalControlName;
    private String description;
    @NonNull
    private String modality;
    @NonNull
    private String bqSuv;
    @NonNull
    private int personCount;
    @NonNull
    private String normalControlPath;
    private Date createDatetime;
    private Date updateDatetime;
}
