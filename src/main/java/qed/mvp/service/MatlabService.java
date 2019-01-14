package qed.mvp.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import qed.mvp.entity.Out;
import qed.mvp.entity.Params;
import qed.mvp.utils.DateUtil;

import java.util.Date;

@Service
public class MatlabService {

    @Value("${TPM}")
    private String tpmPath;


    @Value("${p}")
    private double p;

    @Value("${k}")
    private double k;

    public Params init() {
        Params params = new Params();
        params.setCmd("init");
        params.setPath(tpmPath);
        params.setNii_dir("");
        return params;
    }


    public Params norm(Params params, String[] dcmDirs, String modality, double patientWeight,
                       double imagingInterval, double radionuclideHalfLife, double radionuclideTotalDose,String niiDir) {
        if (modality.equals("PET")) {
            params.setCmd("norm");
        } else {
            params.setCmd("norm_spect");
        }

        params.setDcm_dirs(dcmDirs);
        params.setWeight(patientWeight);
        params.setDelta_t(imagingInterval);
        params.setHalf_time(radionuclideHalfLife);
        params.setActivity(radionuclideTotalDose);
        params.setSuffix("*.dcm");
        params.setNii_dir(niiDir);

        return params;
    }

    public Params suvFile(Params params, String[] niiFiles, double weight, double activity, double halfTime, double deltaT,String niiDir) {
        params.setCmd("suvfile");
        params.setNii_dir(niiDir);
        params.setSuffix("*.dcm");

        params.setNii_files(niiFiles);
        params.setWeight(weight);
        params.setActivity(activity);
        params.setHalf_time(halfTime);
        params.setDelta_t(deltaT);
        return params;
    }

    public Params tTest(Params params, Out normOut, String modality, double suv) {

        params.setCmd("ttest");
        params.setModality(modality);

        params.setComp("grandmean");
        params.setNormal_files("");
        params.setSuv(suv);
        if (suv == 0)
            params.setNii_files(normOut.getBqlist());
        else
            params.setNii_files(normOut.getSuvlist());

        return params;
    }

    public Params resultT(Params params, Out tTestOut, double[] dim4) {
        params.setCmd("result_t");
        params.setReduce(0);
        params.setC("none");
        params.setP(p);
        params.setK(k);
        params.setDims(dim4);
        double[][][][] zeros = new double[(int) dim4[0]][(int) dim4[1]][(int) dim4[2]][(int) dim4[3]];
        params.setRois(zeros);
        params.setLast_file(tTestOut.getFile());

        return params;
    }
}
