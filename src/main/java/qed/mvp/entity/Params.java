package qed.mvp.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Params {
    private String err;
    private String cmd;
    private String path;
    private String nii_dir;

    //norm
    private String[] dcm_dirs;
    private double weight;
    private double delta_t;
    private double half_time;
    private double activity;
    private String suffix;

    //suv
    private String[] nii_files;

    //tTest
    private double suv;
    private String modality;
    private String comp;
    private String normal_files;

    //resultT
    private double reduce;
    private String c;
    private double p;
    private double k;
    private double[] dims;
    private double[][][][] rois;
    private String last_file;

    private String res_file;

}
