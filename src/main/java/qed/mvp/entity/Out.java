package qed.mvp.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Out {

    private String err;

    private double[] dim;
    private double[] spacing;
    private double[] origin;

    private String clipfile;
    private String file;
    private double p;
    private double k;
    private String unit;

    private String outdir;
    private String clipdir;

    private double bqcount;
    private String[] bqlist;
    private double suvcount;
    private String[] suvlist;
    private double[] zpos;
    private double[] Y;

    private double[] dims;
    private double[] spacings;
    private double niduscount;
    private double[] mnicoord;
    private double[] zscore;
    private double size;
    private double[] data;
    private double[] tvalue;
    private double count;
    private double[] rois;

    private String sourcefile;


}
