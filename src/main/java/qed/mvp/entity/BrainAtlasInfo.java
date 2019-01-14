package qed.mvp.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BrainAtlasInfo {
    private int id;
    private String name;
    private double value;
    private double symmetryValue;
}
