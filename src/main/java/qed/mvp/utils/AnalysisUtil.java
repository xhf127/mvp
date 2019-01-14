package qed.mvp.utils;

import com.ericbarnhill.niftijio.FourDimensionalArray;
import com.ericbarnhill.niftijio.NiftiVolume;
import com.mathworks.mps.client.MWClient;
import com.mathworks.mps.client.MWHttpClient;
import org.apache.commons.io.FileUtils;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import qed.mvp.entity.AnalyzeResult;
import qed.mvp.entity.Out;
import qed.mvp.entity.Params;
import qed.mvp.mapper.AnalyzeResultMapper;
import qed.mvp.mapper.BrainAtlasInfoMapper;
import qed.mvp.mapper.BrainAtlasMapper;
import qed.mvp.service.MatlabService;
import qed.mvp.service.MpsClientConfig;
import qed.mvp.service.QedPreprocess;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Math.max;
import static java.lang.Math.min;

@Component
@SuppressWarnings({"unchecked", "ResultOfMethodCallIgnored"})
public class AnalysisUtil {

    private static final Logger log = LoggerFactory.getLogger(AnalysisUtil.class);


    @Autowired
    private MatlabService matlabService;

    @Autowired
    private BrainAtlasInfoMapper brainAtlasInfoMapper;

    @Autowired
    private BrainAtlasMapper brainAtlasMapper;

    @Autowired
    private AnalyzeResultMapper analyzeResultMapper;

    @Value("${niiDir}")
    private String niiDir;

    /**
     * 分析
     */
    public Map<String, String> analysis(String dcmPathDir, String resultPathDir, String modality, double patientWeight,
                                        double imagingInterval, double radionuclideHalfLife, double radionuclideTotalDose) {
        MWClient client = new MWHttpClient(new MpsClientConfig());
        Map<String, String> resultMap = new HashMap<>();
        String nii_dir = niiDir + DateUtil.parseDateToStr(new Date(), DateUtil.DATE_TIME_FORMAT_YYYYMMDDHHMISSSSS);
        log.info("analysis path" + nii_dir);
        resultMap.put("path", nii_dir);
        File nii = new File(nii_dir);
        if (!nii.exists()) {
            nii.mkdirs();
        }
        try {
            //init
            QedPreprocess matlab = client.createProxy(new URL("http://localhost:9910/qed_preprocess"),
                    QedPreprocess.class);
            Params initParams = matlabService.init();

            Out initOut = matlab.qed_preprocess(initParams);
            String err = initOut.getErr();
            resultMap.put("err", err);
            if (!err.equals("successed-init")) {
                return resultMap;
            }

            //norm
            Params paramsNorm = matlabService.norm(initParams, new String[]{dcmPathDir}, modality, patientWeight,
                    imagingInterval, radionuclideHalfLife, radionuclideTotalDose, nii_dir);

            Out normOut = matlab.qed_preprocess(paramsNorm);

            err = normOut.getErr();
            resultMap.put("err", err);
            if (!err.startsWith("successed-norm")) {
                return resultMap;
            }


            String sourceMP = normOut.getSourcefile();
            String sourcePath = resultPathDir + "/source.nii";
            File sourceFile = new File(sourcePath);

            String normMP = normOut.getFile();
            String normPath = resultPathDir + "/norm.nii";
            File normFile = new File(normPath);
            //创建result文件夹
            if (!normFile.getParentFile().exists()) {
                normFile.getParentFile().mkdirs();
            }
            FileUtils.copyFile(new File(sourceMP), sourceFile);
            FileUtils.copyFile(new File(normMP), normFile);
            resultMap.put("normPath", normPath);
            resultMap.put("sourcePath", sourcePath);

            double[] dim3 = normOut.getDim();
            double[] dim4 = Arrays.copyOf(dim3, dim3.length + 1);
            dim4[dim3.length] = 1;

            if (modality.equals("PET")) {
                //suv_T_Array
                Params paramsTTestSuv = matlabService.tTest(paramsNorm, normOut, modality, 1);
                Out tTestOut = matlab.qed_preprocess(paramsTTestSuv);

                err = tTestOut.getErr();
                resultMap.put("err", err);
                if (!err.equals("successed-ttest")) {
                    return resultMap;
                }

                Params paramsResultT = matlabService.resultT(paramsTTestSuv, tTestOut, dim4);
                Out resultTOutSuv = matlab.qed_preprocess(paramsResultT);

                err = resultTOutSuv.getErr();
                resultMap.put("err", err);
                if (!err.equals("successed-result_t")) {
                    return resultMap;
                }

                String suvMPath = resultTOutSuv.getFile();
                String suvPath = resultPathDir + "/suvArray.nii";
                File suvFile = new File(suvPath);

                FileUtils.copyFile(new File(suvMPath), suvFile);
                resultMap.put("suvPath", suvPath);
            }

            //bq_T_Array
            Params paramsTTestBq = matlabService.tTest(paramsNorm, normOut, modality, 0);
            Out tTestBqOut = matlab.qed_preprocess(paramsTTestBq);

            err = tTestBqOut.getErr();
            resultMap.put("err", err);
            if (!err.equals("successed-ttest")) {
                return resultMap;
            }

            Params paramsResultT = matlabService.resultT(paramsTTestBq, tTestBqOut, dim4);
            Out resultTOutBq = matlab.qed_preprocess(paramsResultT);

            err = resultTOutBq.getErr();
            resultMap.put("err", err);
            if (!err.equals("successed-result_t")) {
                return resultMap;
            }

            String bpMPath = resultTOutBq.getFile();
            String bpPath = resultPathDir + "/bqArray.nii";
            File bpFile = new File(bpPath);
            FileUtils.copyFile(new File(bpMPath), bpFile);
            resultMap.put("bpPath", bpPath);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            client.close();
        }

        return resultMap;

    }

    /**
     * 改变suv分析
     */
    public Map<String, String> suvAnalysis(String resultPathDir, String modality, String niiFiles, double weight, double activity, double halfTime, String deltaT) {

        MWClient client = new MWHttpClient(new MpsClientConfig());
        Map<String, String> resultMap = new HashMap<>();
        String nii_dir = niiDir + DateUtil.parseDateToStr(new Date(), DateUtil.DATE_TIME_FORMAT_YYYYMMDDHHMISSSSS);
        resultMap.put("path", nii_dir);

        File nii = new File(nii_dir);
        if (!nii.exists()) {
            nii.mkdirs();
        }

        try {
            //init
            QedPreprocess matlab = client.createProxy(new URL("http://localhost:9910/qed_preprocess"),
                    QedPreprocess.class);
            Params initParams = matlabService.init();

            Out initOut = matlab.qed_preprocess(initParams);
            String err = initOut.getErr();
            resultMap.put("err", err);
            if (!err.equals("successed-init")) {
                return resultMap;
            }

            Params suvFileParams = matlabService.suvFile(initParams, new String[]{niiFiles}, weight, activity, halfTime, Double.valueOf(deltaT), nii_dir);

            Out suvFileOut = matlab.qed_preprocess(suvFileParams);
            err = suvFileOut.getErr();
            resultMap.put("err", err);
            if (!err.startsWith("successed")) {
                return resultMap;
            }

            double[] dim3 = suvFileOut.getDim();
            double[] dim4 = Arrays.copyOf(dim3, dim3.length + 1);
            dim4[dim3.length] = 1;

            if (modality.equals("PET")) {
                //suv_T_Array
                Params paramsTTestSuv = matlabService.tTest(suvFileParams, suvFileOut, modality, 1);
                Out tTestOut = matlab.qed_preprocess(paramsTTestSuv);

                err = tTestOut.getErr();
                resultMap.put("err", err);
                if (!err.equals("successed-ttest")) {
                    return resultMap;
                }

                Params paramsResultT = matlabService.resultT(paramsTTestSuv, tTestOut, dim4);
                Out resultTOutSuv = matlab.qed_preprocess(paramsResultT);

                err = resultTOutSuv.getErr();
                resultMap.put("err", err);
                if (!err.equals("successed-result_t")) {
                    return resultMap;
                }

                String suvMPath = resultTOutSuv.getFile();
                String suvPath = resultPathDir + "/suvArray.nii";
                File suvFile = new File(suvPath);

                FileUtils.copyFile(new File(suvMPath), suvFile);
                resultMap.put("suvPath", suvPath);
            }

            //bq_T_Array
            Params paramsTTestBq = matlabService.tTest(suvFileParams, suvFileOut, modality, 0);
            Out tTestBqOut = matlab.qed_preprocess(paramsTTestBq);

            err = tTestBqOut.getErr();
            resultMap.put("err", err);
            if (!err.equals("successed-ttest")) {
                return resultMap;
            }

            Params paramsResultT = matlabService.resultT(paramsTTestBq, tTestBqOut, dim4);
            Out resultTOutBq = matlab.qed_preprocess(paramsResultT);

            err = resultTOutBq.getErr();
            resultMap.put("err", err);
            if (!err.equals("successed-result_t")) {
                return resultMap;
            }

            String bpMPath = resultTOutBq.getFile();
            String bpPath = resultPathDir + "/bqArray.nii";
            File bpFile = new File(bpPath);
            FileUtils.copyFile(new File(bpMPath), bpFile);
            resultMap.put("bpPath", bpPath);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            client.close();
        }

        return resultMap;
    }

    private static int[] tt;
    private static FourDimensionalArray il;
    //返回矩阵
    private static FourDimensionalArray l;

    /**
     * 计算连通域
     */
    private Map<String, Object> mexFunction(FourDimensionalArray data, int connectedDomain) {

        Map<String, Object> result = new HashMap<>();

        tt = new int[0];
        il = new FourDimensionalArray(data.sizeX(), data.sizeY(), data.sizeZ(), data.dimension());
        l = new FourDimensionalArray(data.sizeX(), data.sizeY(), data.sizeZ(), data.dimension());

        int[] dims = {data.sizeX(), data.sizeY(), data.sizeZ(), data.dimension()};

        int ttn = do_initial_labelling(data, dims, connectedDomain);
        //连通域个数
        double nl = translate_labels(dims, ttn);

        result.put("bwLabelCount", nl);
        result.put("bwLabelArray", l);

        return result;

    }

    private int do_initial_labelling(FourDimensionalArray bw, int[] dims, int conn) {
        int i, j;
        int[] nabo = new int[8];
        int label = 1;
        int nr_set;
        int l;
        int sl, r, c, dim;
        int ttn = 1000;
        tt = new int[ttn];

        for (dim = 0; dim < dims[3]; dim++) {
            for (sl = 0; sl < dims[2]; sl++) {
                for (c = 0; c < dims[1]; c++) {
                    for (r = 0; r < dims[0]; r++) {
                        nr_set = 0;
                        if (bw.get(r, c, sl, dim) != 0) {
                            nabo[0] = check_previous_slice(r, c, sl, dim, dims, conn);

                            if (nabo[0] != 0) {
                                nr_set += 1;
                            }

                            if (conn >= 6) {
                                if (r != 0) {
                                    if ((l = (int) il.get(r - 1, c, sl, dim)) != 0) {
                                        nabo[nr_set++] = l;
                                    }
                                }
                                if (c != 0) {
                                    if ((l = (int) il.get(r, c - 1, sl, dim)) != 0) {
                                        nabo[nr_set++] = l;
                                    }
                                }
                            }
                           /*
                              For 18(edge)-connectivity
                              N.B. In current slice no difference to 26.
                           */
                            if (conn >= 18) {
                                if (c != 0 && r != 0) {
                                    if ((l = (int) il.get(r - 1, c - 1, sl, dim)) != 0) {
                                        nabo[nr_set++] = l;
                                    }
                                }
                                if (c != 0 && (r < dims[0] - 1)) {
                                    if ((l = (int) il.get(r + 1, c - 1, sl, dim)) != 0) {
                                        nabo[nr_set++] = l;
                                    }
                                }
                            }
                            if (nr_set != 0) {

                                il.set(r, c, sl, dim, nabo[0]);
                                fill_tratab(nabo, nr_set);
                            } else {
                                il.set(r, c, sl, dim, label);
                                if (label >= ttn) {
                                    ttn += 1000;
                                    tt = Arrays.copyOf(tt, ttn);
                                }
                                tt[label - 1] = label;
                                label++;
                            }
                        }
                    }
                }
            }
        }

   /*
      Finalise translation table
   */
        for (i = 0; i < (label - 1); i++) {
            j = i;
            while (tt[j] != j + 1) {
                j = tt[j] - 1;
            }
            tt[i] = j + 1;
        }

        return label - 1;
    }

    private int check_previous_slice(int r, int c, int sl, int dim, int[] dims, int conn) {
        int l;
        int[] nabo = new int[9];
        int nr_set = 0;

        if (sl == 0)
            return 0;

        if (conn >= 6) {
            if ((l = (int) il.get(r, c, sl - 1, dim)) != 0) {
                nabo[nr_set++] = l;
            }
        }
        if (conn >= 18) {
            if (r != 0) {
                if ((l = (int) il.get(r - 1, c, sl - 1, dim)) != 0) {
                    nabo[nr_set++] = l;
                }
            }
            if (c != 0) {
                if ((l = (int) il.get(r, c - 1, sl - 1, dim)) != 0) {
                    nabo[nr_set++] = l;
                }
            }
            if (r < dims[0] - 1) {
                if ((l = (int) il.get(r + 1, c, sl - 1, dim)) != 0) {
                    nabo[nr_set++] = l;
                }
            }
            if (c < dims[1] - 1) {
                if ((l = (int) il.get(r, c + 1, sl - 1, dim)) != 0) {
                    nabo[nr_set++] = l;
                }
            }
        }
        if (conn == 26) {
            if (r != 0 && c != 0) {
                if ((l = (int) il.get(r - 1, c - 1, sl - 1, dim)) != 0) {
                    nabo[nr_set++] = l;
                }
            }
            if ((r < dims[0] - 1) && c != 0) {
                if ((l = (int) il.get(r + 1, c - 1, sl - 1, dim)) != 0) {
                    nabo[nr_set++] = l;
                }
            }
            if (r != 0 && (c < dims[1] - 1)) {
                if ((l = (int) il.get(r - 1, c + 1, sl - 1, dim)) != 0) {
                    nabo[nr_set++] = l;
                }
            }
            if ((r < dims[0] - 1) && (c < dims[1] - 1)) {
                if ((l = (int) il.get(r + 1, c + 1, sl - 1, dim)) != 0) {
                    nabo[nr_set++] = l;
                }
            }
        }

        if (nr_set != 0) {
            fill_tratab(nabo, nr_set);
            return nabo[0];
        } else {
            return 0;
        }
    }

    private void fill_tratab(int[] nabo, int nr_set) {
        int i, j, cntr;
        int[] tn = new int[9];
        int ltn = Integer.MAX_VALUE;

   /*
   Find smallest terminal number in neighbourhood
   */

        for (i = 0; i < nr_set; i++) {
            j = nabo[i];
            cntr = 0;
            while (tt[(j - 1)] != j) {
                j = tt[(j - 1)];
                cntr++;
                if (cntr > 100) {
                    System.out.println("Ooh no!!");
                    break;
                }
            }
            tn[i] = j;
            ltn = min(ltn, j);
        }
   /*
   Replace all terminal numbers in neighbourhood by the smallest one
   */
        for (i = 0; i < nr_set; i++) {
            tt[tn[i] - 1] = ltn;
        }

    }

    private double translate_labels(int[] dims, int ttn) {
        int i;
        int ml = 0;
        double cl = 0.0;

        for (i = 0; i < ttn; i++) {
            ml = max(ml, tt[i]);
        }

        double[] fl = new double[ml];

        for (int dim = 0; dim < dims[3]; dim++) {
            for (int sl = 0; sl < dims[2]; sl++) {
                for (int c = 0; c < dims[1]; c++) {
                    for (int r = 0; r < dims[0]; r++) {
                        int num = (int) il.get(r, c, sl, dim);
                        if (num != 0) {
                            if (fl[tt[num - 1] - 1] == 0) {
                                cl += 1.0;
                                fl[tt[num - 1] - 1] = cl;
                            }
                            l.set(r, c, sl, dim, fl[tt[num - 1] - 1]);
                        }
                    }
                }
            }
        }


        return cl;
    }

    /**
     * 定量分析
     */
    public Map<Double, Map<String, Object>> quantitative(String statisticalMethod, boolean statisticalMode,
                                                         double abnormalVolume, double t, int analyzeResultKey) {
        Map<Double, Map<String, Object>> result = new HashMap<>();
        try {
            AnalyzeResult analyzeResult = analyzeResultMapper.findByAnalyzeResultKey(analyzeResultKey);

            NiftiVolume normSource = NiftiVolume.read(analyzeResult.getNormalizedImagePath());

            //t值矩阵 statisticalMethod
            String tPath;
            if (statisticalMethod.equals("BQ")) {
                tPath = analyzeResult.getBqTPath();
            } else {
                tPath = analyzeResult.getSuvTPath();
            }
            NiftiVolume tSource = NiftiVolume.read(tPath);
            NiftiVolume tSource_1 = NiftiVolume.read(tPath);

            int[] dims = {tSource.data.sizeX(), tSource.data.sizeY(), tSource.data.sizeZ(), tSource.data.dimension()};

            Map<String, Object> bwLabelN = binaryzation(t, tSource_1.data, dims, statisticalMode);

            //连通域矩阵,t值矩阵
            FourDimensionalArray bwLabelArray = (FourDimensionalArray) bwLabelN.get("bwLabelArray");

            //脑图谱二值化合并矩阵
            FourDimensionalArray brainT = new FourDimensionalArray(dims[0], dims[1], dims[2], dims[3]);

            //全脑图谱
            FourDimensionalArray brainAtlas = NiftiVolume.read(brainAtlasMapper.findByBrainAtlasKey(4)).data;
            Map<Double, ArrayList<Double>> contourMap = new HashMap<>();
            Map<Double, ArrayList<Double>> volumeMap = new HashMap<>();

            for (int dim = 0; dim < dims[3]; dim++) {
                for (int sl = 0; sl < dims[2]; sl++) {
                    for (int c = 0; c < dims[1]; c++) {
                        for (int r = 0; r < dims[0]; r++) {
                            double label = bwLabelArray.get(r, c, sl, dim);
                            double atlas = brainAtlas.get(r, c, sl, dim);
                            //体积
                            if (label != 0) {
                                if (volumeMap.containsKey(label)) {
                                    ArrayList<Double> count = volumeMap.get(label);
                                    count.add(label);
                                    volumeMap.put(label, count);
                                } else {
                                    ArrayList<Double> count = new ArrayList<>();
                                    count.add(label);
                                    volumeMap.put(label, count);
                                }
                            }

                            //合并
                            if (label != 0 && atlas != 0) {
                                brainT.set(r, c, sl, dim, label);
                                if (contourMap.containsKey(label)) {
                                    ArrayList<Double> count = contourMap.get(label);
                                    count.add(atlas);
                                    contourMap.put(label, count);
                                } else {
                                    ArrayList<Double> count = new ArrayList<>();
                                    count.add(atlas);
                                    contourMap.put(label, count);
                                }
                            }
                        }
                    }
                }
            }

            float spaceX = normSource.header.pixdim[1];
            float spaceY = normSource.header.pixdim[2];
            float spaceZ = normSource.header.pixdim[3];

            //过滤体积
            for (Map.Entry<Double, ArrayList<Double>> entry : volumeMap.entrySet()) {
                double key = entry.getKey();
                int size = entry.getValue().size();
                //体积计算
                double volume = size * spaceX * spaceY * spaceZ;
                if (volume < abnormalVolume || !contourMap.containsKey(key)) {
                    entry.getValue().clear();
                    contourMap.remove(key);
                }
            }

            //nii路径,小脑图谱
            List<Double> cerebellumList = cerebellum(dims, analyzeResult.getNormalizedImagePath(), brainAtlasMapper.findByBrainAtlasKey(2));
            result = calculation(statisticalMode, dims, bwLabelArray, brainT, tSource, normSource, volumeMap, contourMap, cerebellumList);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 半定量分析
     */
    public Map<Double, Map<String, Object>> semiQuantitation(int analyzeResultKey, double suvThreshold) {
        FourDimensionalArray brainAtlas;
        FourDimensionalArray normData;
        AnalyzeResult analyzeResult = new AnalyzeResult();
        String modality = "";
        //结果集
        Map<Double, Map<String, Object>> result = new HashMap<>();
        try {
            analyzeResult = analyzeResultMapper.findByAnalyzeResultKey(analyzeResultKey);

            modality = analyzeResult.getSeries().getModality();

            brainAtlas = NiftiVolume.read(analyzeResult.getBrainAtlasPath()).data;
            normData = NiftiVolume.read(analyzeResult.getNormalizedImagePath()).data;

            int[] dims = {normData.sizeX(), normData.sizeY(), normData.sizeZ(), normData.dimension()};

            for (int dim = 0; dim < dims[3]; dim++) {
                for (int sl = 0; sl < dims[2]; sl++) {
                    for (int c = 0; c < dims[1]; c++) {
                        for (int r = 0; r < dims[0]; r++) {
                            double brainAtlasValue = brainAtlas.get(r, c, sl, dim);
                            if (brainAtlasValue > 0) {
                                double normValue = normData.get(r, c, sl, dim);
                                if (result.containsKey(brainAtlasValue)) {
                                    Map<String, Object> data = result.get(brainAtlasValue);
                                    List<Double> normValueList = (ArrayList<Double>) data.get("normValueList");
                                    normValueList.add(normValue);
                                    data.put("normValueList", normValueList);
                                    result.put(brainAtlasValue, data);
                                } else {
                                    Map<String, Object> data = new HashMap<>();
                                    List<Double> normValueList = new ArrayList<>();
                                    normValueList.add(normValue);
                                    data.put("normValueList", normValueList);
                                    result.put(brainAtlasValue, data);
                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<Double> allBrainAtlasList = new ArrayList<>();

        //所有脑区数值
        for (Map.Entry<Double, Map<String, Object>> entry : result.entrySet()) {
            List<Double> normValueList = (ArrayList<Double>) entry.getValue().get("normValueList");
            double max = Collections.max(normValueList) * suvThreshold;
            normValueList.removeIf(x -> x < max);
            System.out.println(entry.getKey() + " size " + normValueList.size());
            allBrainAtlasList.addAll(normValueList);
            double avg = normValueList.stream().collect(Collectors.averagingDouble((x) -> x));
            entry.getValue().put("max", formatDouble2(Collections.max(normValueList)));
            entry.getValue().put("avg", formatDouble2(avg));
        }

        double allBrainAtlasMax = Collections.max(allBrainAtlasList);
        double allBrainAtlasAvg = allBrainAtlasList.stream().collect(Collectors.averagingDouble((x) -> x));

        //小脑功能区列表
        Map<String, Object> left_cerebellum = result.get(39.0);
        Map<String, Object> right_cerebellum = result.get(38.0);

        //正常人群组均值
        Map<Double, Double> normalControl = new HashMap<>();
        normalControl.put(4.0, 5.174160114278755);
        normalControl.put(11.0, 5.1652188677671695);
        normalControl.put(23.0, 6.868052904576975);
        normalControl.put(30.0, 6.934541645102608);
        normalControl.put(31.0, 4.977858176449516);
        normalControl.put(32.0, 4.990925106621428);
        normalControl.put(35.0, 4.439059148879301);
        normalControl.put(37.0, 6.661584960990763);
        normalControl.put(36.0, 6.503808156538472);
        normalControl.put(38.0, 5.670041316392509);
        normalControl.put(39.0, 5.754868467794857);
        normalControl.put(40.0, 5.370061252005393);
        normalControl.put(41.0, 5.446043534832185);
        normalControl.put(44.0, 5.993508227345429);
        normalControl.put(45.0, 5.970040953541204);
        normalControl.put(47.0, 5.268909921710946);
        normalControl.put(46.0, 4.634995124194682);
        normalControl.put(48.0, 5.369054738812034);
        normalControl.put(49.0, 4.671002559401613);
        normalControl.put(50.0, 4.566988012496177);
        normalControl.put(51.0, 3.882456092431418);
        normalControl.put(52.0, 3.7478198711065125);
        normalControl.put(55.0, 6.650446584766081);
        normalControl.put(57.0, 8.154120167433806);
        normalControl.put(56.0, 6.711764105055965);
        normalControl.put(58.0, 8.209123536431653);
        normalControl.put(59.0, 6.655578766028495);
        normalControl.put(61.0, 4.967916743563231);
        normalControl.put(60.0, 6.555471940663176);
        normalControl.put(62.0, 4.993857307388769);
        normalControl.put(63.0, 6.055391591963479);
        normalControl.put(64.0, 6.1287937693629395);
        normalControl.put(71.0, 6.09829410279906);
        normalControl.put(69.0, 2.5518756152787283);
        normalControl.put(73.0, 6.515630394254543);
        normalControl.put(72.0, 6.289959503725975);
        normalControl.put(75.0, 5.131798901465477);
        normalControl.put(76.0, 5.0883747259755205);
        normalControl.put(103.0, 7.053372100454595);
        normalControl.put(102.0, 7.144569493748004);
        normalControl.put(101.0, 7.268338246290502);
        normalControl.put(100.0, 7.18227046815467);
        normalControl.put(104.0, 6.213745735082006);
        normalControl.put(105.0, 6.003277338637245);
        normalControl.put(107.0, 6.906714189249644);
        normalControl.put(106.0, 6.925269462125023);
        normalControl.put(109.0, 8.903063862342679);
        normalControl.put(108.0, 9.2143594327478);
        normalControl.put(112.0, 7.376682228712886);
        normalControl.put(113.0, 7.2953039942819125);
        normalControl.put(115.0, 7.9765789133462235);
        normalControl.put(114.0, 8.219600892082315);
        normalControl.put(116.0, 4.6581476295279725);
        normalControl.put(117.0, 4.6439012112972575);
        normalControl.put(119.0, 7.8119375464874885);
        normalControl.put(118.0, 7.999228551821924);
        normalControl.put(122.0, 6.29414130043634);
        normalControl.put(123.0, 6.357984844282062);
        normalControl.put(121.0, 4.800642113419103);
        normalControl.put(120.0, 4.817700951194595);
        normalControl.put(125.0, 5.640900723414937);
        normalControl.put(124.0, 5.788647119265748);
        normalControl.put(132.0, 5.743209566396813);
        normalControl.put(133.0, 5.776505215682883);
        normalControl.put(135.0, 8.030220366207097);
        normalControl.put(129.0, 5.785426516746597);
        normalControl.put(128.0, 5.70193445648051);
        normalControl.put(134.0, 7.894235111851601);
        normalControl.put(140.0, 7.792697978828928);
        normalControl.put(141.0, 7.738153399943544);
        normalControl.put(136.0, 5.042900650462972);
        normalControl.put(137.0, 4.937447374104256);
        normalControl.put(143.0, 7.309011867159419);
        normalControl.put(142.0, 7.274122934583683);
        normalControl.put(138.0, 7.378547935824453);
        normalControl.put(139.0, 7.221558865153002);
        normalControl.put(146.0, 5.7322801589957315);
        normalControl.put(147.0, 5.734024743986699);
        normalControl.put(145.0, 5.932713642483369);
        normalControl.put(144.0, 6.209634927691727);
        normalControl.put(151.0, 6.94078143911469);
        normalControl.put(150.0, 6.937649082555284);
        normalControl.put(148.0, 6.5035370981990805);
        normalControl.put(149.0, 6.602065299705219);
        normalControl.put(155.0, 6.239426616571662);
        normalControl.put(154.0, 6.307850131198002);
        normalControl.put(152.0, 7.528054855209094);
        normalControl.put(157.0, 4.412988236454568);
        normalControl.put(156.0, 4.5079961204931465);
        normalControl.put(153.0, 7.491223925150106);
        normalControl.put(161.0, 7.079865035118767);
        normalControl.put(160.0, 7.058801136190379);
        normalControl.put(164.0, 5.843312868730083);
        normalControl.put(165.0, 5.706142940898328);
        normalControl.put(166.0, 8.16934011470074);
        normalControl.put(167.0, 8.150332693529407);
        normalControl.put(163.0, 7.098126875144208);
        normalControl.put(162.0, 7.265082030599963);
        normalControl.put(170.0, 5.1196997780564);
        normalControl.put(171.0, 5.083592217509938);
        normalControl.put(172.0, 6.816712787544584);
        normalControl.put(173.0, 6.848349754884678);
        normalControl.put(169.0, 8.42426886305857);
        normalControl.put(168.0, 8.297488285370997);
        normalControl.put(175.0, 7.419672918075352);
        normalControl.put(174.0, 7.32291566051837);
        normalControl.put(179.0, 6.4036804255665025);
        normalControl.put(178.0, 6.33825961318614);
        normalControl.put(180.0, 6.354201590053132);
        normalControl.put(181.0, 6.392009359546493);
        normalControl.put(183.0, 6.761100818050117);
        normalControl.put(182.0, 6.733067359680379);
        normalControl.put(177.0, 6.120670750178738);
        normalControl.put(176.0, 6.259768462021173);
        normalControl.put(186.0, 5.545097708469883);
        normalControl.put(187.0, 5.666762268614404);
        normalControl.put(191.0, 6.862877243659657);
        normalControl.put(190.0, 6.857473307848444);
        normalControl.put(185.0, 7.741676976891351);
        normalControl.put(184.0, 7.734575441296598);
        normalControl.put(197.0, 5.7604916923729625);
        normalControl.put(196.0, 5.898383092260871);
        normalControl.put(195.0, 6.266901587032131);
        normalControl.put(194.0, 6.256883726031972);
        normalControl.put(199.0, 6.489288113508592);
        normalControl.put(198.0, 6.347296200492198);
        normalControl.put(192.0, 7.628034143444802);
        normalControl.put(193.0, 7.621352188153817);
        normalControl.put(203.0, 5.029914763487047);
        normalControl.put(202.0, 5.155439707135188);
        normalControl.put(201.0, 6.471991306331171);
        normalControl.put(200.0, 6.538823846549127);
        normalControl.put(204.0, 5.935885705394725);
        normalControl.put(205.0, 5.816039081610693);
        normalControl.put(206.0, 8.318899843235284);
        normalControl.put(207.0, 8.553070704679707);

        //小脑比
        Iterator<Map.Entry<Double, Map<String, Object>>> it = result.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Double, Map<String, Object>> entry = it.next();
            Map<String, Object> brainAtlasValueMap = entry.getValue();

            //左小脑比（最大值）
            double leftCerebellumCompareMax = formatDouble2((double) brainAtlasValueMap.get("max") / (double) left_cerebellum.get("max"));

            //右小脑比（最大值）
            double rightCerebellumCompareMax = formatDouble2((double) brainAtlasValueMap.get("max") / (double) right_cerebellum.get("max"));

            //左小脑比（均值）
            double leftCerebellumCompareAvg = formatDouble2((double) brainAtlasValueMap.get("avg") / (double) left_cerebellum.get("avg"));

            //右小脑比（均值）
            double rightCerebellumCompareAvg = formatDouble2((double) brainAtlasValueMap.get("avg") / (double) right_cerebellum.get("avg"));

            //全脑比（最大值）
            double allBrainAtlasCompareMax = formatDouble2((double) brainAtlasValueMap.get("max") / allBrainAtlasMax);

            //全脑比（均值）
            double allBrainAtlasCompareAvg = formatDouble2((double) brainAtlasValueMap.get("max") / allBrainAtlasAvg);

            brainAtlasValueMap.put("leftCerebellumCompareMax", leftCerebellumCompareMax);
            brainAtlasValueMap.put("rightCerebellumCompareMax", rightCerebellumCompareMax);
            brainAtlasValueMap.put("leftCerebellumCompareAvg", leftCerebellumCompareAvg);
            brainAtlasValueMap.put("rightCerebellumCompareAvg", rightCerebellumCompareAvg);
            brainAtlasValueMap.put("allBrainAtlasCompareMax", allBrainAtlasCompareMax);
            brainAtlasValueMap.put("allBrainAtlasCompareAvg", allBrainAtlasCompareAvg);

            float weight = analyzeResult.getPatientWeight();
            int imagingInterval = analyzeResult.getImagingInterval();
            float radionuclideHalfLife = analyzeResult.getRadionuclideHalfLife();
            float radionuclideTotalDose = analyzeResult.getRadionuclideTotalDose();

            if (modality.equals("PET")) {
                double suvMax = formatDouble2((double) brainAtlasValueMap.get("max") * weight / (radionuclideTotalDose * Math.pow(2, -(imagingInterval / radionuclideHalfLife)) * 1000));
                double suvAvg = formatDouble2((double) brainAtlasValueMap.get("avg") * weight / (radionuclideTotalDose * Math.pow(2, -(imagingInterval / radionuclideHalfLife)) * 1000));
                brainAtlasValueMap.put("suvMax", suvMax);
                brainAtlasValueMap.put("suvAvg", suvAvg);
                double normalControlValue = normalControl.get(entry.getKey());
                double normalControlAvg = formatDouble2(suvAvg / normalControlValue);
                brainAtlasValueMap.put("normalControlAvg", normalControlAvg);
            } else {
                brainAtlasValueMap.put("suvMax", "");
                brainAtlasValueMap.put("suvAvg", "");
                brainAtlasValueMap.put("normalControlAvg", "");
            }
            brainAtlasValueMap.remove("normValueList");

            //对侧比
            Map<String, Object> info = brainAtlasInfoMapper.brainValue(entry.getKey());
            if (info != null) {
                if (info.get("value") != null) {
                    double oppositeCompareAvg = formatDouble2((double) brainAtlasValueMap.get("avg") / (double) result.get(info.get("value")).get("avg"));
                    brainAtlasValueMap.put("oppositeCompareAvg", oppositeCompareAvg);
                } else {
                    brainAtlasValueMap.put("oppositeCompareAvg", null);
                }
                if (info.get("name") != null) {
                    brainAtlasValueMap.put("name", info.get("name"));
                }
            } else {
                it.remove();
            }
        }
        return result;
    }

    /**
     * 二值化,连通域
     */
    private Map<String, Object> binaryzation(double t, FourDimensionalArray source, int[] dims, boolean flag) {

        for (int dim = 0; dim < dims[3]; dim++) {
            for (int sl = 0; sl < dims[2]; sl++) {
                for (int c = 0; c < dims[1]; c++) {
                    for (int r = 0; r < dims[0]; r++) {
                        double value = source.get(r, c, sl, dim);
                        if (flag) {
                            if (value > t) {
                                source.set(r, c, sl, dim, 1);
                            } else {
                                source.set(r, c, sl, dim, 0);
                            }
                        } else {
                            if (value < -t) {
                                source.set(r, c, sl, dim, 1);
                            } else {
                                source.set(r, c, sl, dim, 0);
                            }
                        }
                    }
                }
            }

        }

        //计算连通域
        return mexFunction(source, 18);
    }

    /**
     * 脑图谱对应关系
     */
    private Map<Double, String> brianValueName() {
        List<Map<String, Object>> value = brainAtlasInfoMapper.selectAll();
        Map<Double, String> vName = new HashMap<>();
        for (Map<String, Object> v : value) {
            vName.put((double) v.get("value"), v.get("name").toString());
        }
        return vName;
    }

    /**
     * 小脑对应值
     */
    private List<Double> cerebellum(int[] dims, String normPath, String cerebellumPath) {
        FourDimensionalArray cerebellumBrainAtlas;
        FourDimensionalArray normData;
        List<Double> cerebellumList = new ArrayList<>();
        try {
            cerebellumBrainAtlas = NiftiVolume.read(cerebellumPath).data;
            normData = NiftiVolume.read(normPath).data;

            for (int dim = 0; dim < dims[3]; dim++) {
                for (int sl = 0; sl < dims[2]; sl++) {
                    for (int c = 0; c < dims[1]; c++) {
                        for (int r = 0; r < dims[0]; r++) {
                            double cerebellumValue = cerebellumBrainAtlas.get(r, c, sl, dim);
                            if (cerebellumValue > 0) {
                                double normValue = normData.get(r, c, sl, dim);
                                cerebellumList.add(normValue);
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return cerebellumList;
    }

    /**
     * 定量分析计算
     * 归一化影像,二值化脑图谱合并
     */
    private Map<Double, Map<String, Object>> calculation(boolean statisticalMode, int[] dims, FourDimensionalArray bwLabelArray,
                                                         FourDimensionalArray brainT, NiftiVolume tSource, NiftiVolume normSource,
                                                         Map<Double, ArrayList<Double>> volumeMap,
                                                         Map<Double, ArrayList<Double>> contourMap, List<Double> cerebellumList) {
        Map<Double, String> vName = brianValueName();

        Map<Double, Map<String, Object>> resultMap = new HashMap<>();
        Map<Double, Map<String, Object>> oppositeResultMap = new HashMap<>();

        //病灶区
        FourDimensionalArray fz = new FourDimensionalArray(dims[0], dims[1], dims[2], dims[3]);

        float quatX = normSource.header.qoffset[0];
        float quatY = normSource.header.qoffset[1];
        float quatZ = normSource.header.qoffset[2];

        float sform00 = normSource.header.srow_x[0];
        float sform11 = normSource.header.srow_y[1];
        float sform22 = normSource.header.srow_z[2];

        float spaceX = normSource.header.pixdim[1];
        float spaceY = normSource.header.pixdim[2];
        float spaceZ = normSource.header.pixdim[3];

        //二值化与T值合并矩阵,取最值
        for (int dim = 0; dim < dims[3]; dim++) {
            for (int sl = 0; sl < dims[2]; sl++) {
                for (int c = 0; c < dims[1]; c++) {
                    for (int r = 0; r < dims[0]; r++) {
                        double label = bwLabelArray.get(r, c, sl, dim);
                        if (label != 0 && volumeMap.get(label).size() != 0) {
                            double normValue = normSource.data.get(r, c, sl, dim);
                            double tValue = tSource.data.get(r, c, sl, dim);
                            if (brainT.get(r, c, sl, dim) != 0) {
                                fz.set(r, c, sl, dim, tValue);

                            }
                            if (resultMap.containsKey(label)) {
                                Map<String, Object> data = resultMap.get(label);
                                List<Double> normValueList = (ArrayList<Double>) data.get("normValueList");
                                normValueList.add(normValue);
                                data.put("normValueList", normValueList);
                                if (statisticalMode) {
                                    if ((double) data.get("tMax") > tValue) {
                                        data.put("tMax", data.get("tMax"));

                                    } else {
                                        data.put("tMax", tValue);
                                        data.put("xyz", (r * sform00 + quatX) + "_" + (c * sform11 + quatY) + "_" + (sl * sform22 + quatZ));
                                    }
                                } else {
                                    if ((double) data.get("tMax") < tValue) {
                                        data.put("tMax", data.get("tMax"));

                                    } else {
                                        data.put("tMax", tValue);
                                        data.put("xyz", (r * sform00 + quatX) + "_" + (c * sform11 + quatY) + "_" + (sl * sform22 + quatZ));
                                    }
                                }

                                resultMap.put(label, data);
                            } else {
                                Map<String, Object> data = new HashMap<>();
                                data.put("tMax", tValue);
                                data.put("xyz", (r * sform00 + quatX) + "_" + (c * sform11 + quatY) + "_" + (sl * sform22 + quatZ));
                                List<Double> normValueList = new ArrayList<>();
                                normValueList.add(tValue);
                                data.put("normValueList", normValueList);
                                resultMap.put(label, data);
                            }

                            //对侧值
                            double oppositeTValue = normSource.data.get(dims[0] - r + 1, c, sl, dim);
                            if (oppositeResultMap.containsKey(label)) {
                                Map<String, Object> data = oppositeResultMap.get(label);
                                List<Double> oppositeTValueList = (ArrayList<Double>) data.get("oppositeTValueList");
                                oppositeTValueList.add(oppositeTValue);
                                data.put("oppositeTValueList", oppositeTValueList);
                                oppositeResultMap.put(label, data);
                            } else {
                                Map<String, Object> data = new HashMap<>();
                                List<Double> oppositeTValueList = new ArrayList<>();
                                oppositeTValueList.add(oppositeTValue);
                                data.put("oppositeTValueList", oppositeTValueList);
                                oppositeResultMap.put(label, data);
                            }

                        }
                    }
                }
            }
        }

        //todo: normalControlValue
        int normalControlValue = 24;

        for (Map.Entry<Double, Map<String, Object>> entry : resultMap.entrySet()) {
            List<Double> normValueList = (ArrayList<Double>) entry.getValue().get("normValueList");
            double avg = normValueList.stream().collect(Collectors.averagingDouble((x) -> x));

            List<Double> oppositeTValueList = (ArrayList<Double>) oppositeResultMap.get(entry.getKey()).get("oppositeTValueList");
            double oppositeAvg = oppositeTValueList.stream().collect(Collectors.averagingDouble((x) -> x));

            double cerebellumMax = Collections.max(cerebellumList);
            double cerebellumAvg = cerebellumList.stream().collect(Collectors.averagingDouble((x) -> x));
            double normValueMax = Collections.max(normValueList);

            double zScore = (double) entry.getValue().get("tMax") * Math.sqrt(1 + (double) 1 / (normalControlValue - 1));
            entry.getValue().put("zscore", Math.abs(formatDouble2(zScore)));
            double volume = contourMap.get(entry.getKey()).size() * spaceX * spaceY * spaceZ;
            entry.getValue().put("volume", formatDouble2(volume));
            entry.getValue().put("cerebellumCompareMax", formatDouble2(normValueMax / cerebellumMax));
            entry.getValue().put("cerebellumCompareAvg", formatDouble2(avg / cerebellumAvg));
            entry.getValue().put("oppositeCompareAvg", oppositeAvg == 0 ? 0 : formatDouble2(avg / oppositeAvg));
            entry.getValue().put("tMax", formatDouble2((double) entry.getValue().get("tMax")));
            entry.getValue().remove("normValueList");

            List<Double> atlas = contourMap.get(entry.getKey()).stream().distinct().collect(Collectors.toList());
            List<String> atlasName = atlas.stream().map(vName::get).collect(Collectors.toList());
            entry.getValue().put("atlasName", atlasName);
        }

        //病灶
        Map<String, Object> fzMap = new HashMap<>();

        INDArray fzArray = Nd4j.create(fz.toArray());
        fzArray = fzArray.permute(2, 1, 0, 3);
        fzMap.put("fz", toDoubles(fzArray));
        fzMap.put("fzDim", dims);
        resultMap.put(-1.0, fzMap);

        return resultMap;

    }

    private double formatDouble2(double d) {
        if (d == 0 || Double.isInfinite(d)) {
            return 0;
        }
        BigDecimal bg = new BigDecimal(d).setScale(1, RoundingMode.UP);
        return bg.doubleValue();
    }

    public float formatFloat(float d, int count) {
        if (d == 0) {
            return 0;
        }
        BigDecimal bg = new BigDecimal(d).setScale(count, RoundingMode.UP);
        return bg.floatValue();
    }

    public double[] toDoubles(INDArray array) {
        array = array.linearView();
        double[] doubles = new double[(int) array.length()];

        for (int i = 0; (long) i < array.length(); ++i) {
            doubles[i] = formatDouble2(array.getDouble(i));
//            doubles[i] = array.getDouble(i);
        }
        return doubles;
    }


}
