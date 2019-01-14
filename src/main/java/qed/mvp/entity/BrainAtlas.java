package qed.mvp.entity;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Date;

@Data
@RequiredArgsConstructor
public class BrainAtlas {
    private int brainAtlasKey;
    @NonNull
    private String brainAtlasName;
    private String description;
    @NonNull
    private String brainAtlasPath;
    private Date createDatetime;
    private Date updateDatetime;
}
