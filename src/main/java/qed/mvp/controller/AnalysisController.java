package qed.mvp.controller;

import com.ericbarnhill.niftijio.NiftiVolume;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import qed.mvp.entity.AnalyzeResult;
import qed.mvp.entity.Series;
import qed.mvp.entity.User;
import qed.mvp.entity.UserBindImage;
import qed.mvp.mapper.AnalyzeResultMapper;
import qed.mvp.mapper.BrainAtlasMapper;
import qed.mvp.mapper.SeriesMapper;
import qed.mvp.mapper.UserBindImageMapper;
import qed.mvp.utils.AnalysisUtil;
import qed.mvp.utils.RestResult;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/analysis")
@SuppressWarnings({"unchecked", "ResultOfMethodCallIgnored"})
public class AnalysisController {

    private static final Logger log = LoggerFactory.getLogger(AnalysisController.class);

    @Autowired
    AnalysisUtil analysisUtil;

    @Autowired
    SeriesMapper seriesMapper;

    @Autowired
    BrainAtlasMapper brainAtlasMapper;

    @Autowired
    AnalyzeResultMapper analyzeResultMapper;

    @Autowired
    UserBindImageMapper userBindImageMapper;

    @Value("${resultDir}")
    private String resultDir;

    @ApiOperation(value = "分析", notes = "数据分析")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "analyzeResultKey", value = "分析结果主键key", required = true, dataType = "int")
    })
    @PostMapping("/analyzing")
    public RestResult analyzing(@AuthenticationPrincipal User user, @RequestParam int analyzeResultKey) {
        AnalyzeResult analyzeResult = analyzeResultMapper.findByAnalyzeResultKey(analyzeResultKey);
        Series series = analyzeResult.getSeries();
        String resultPathDir = resultDir + user.getUserId() + "/" + series.getSeriesInstanceUid();

        if (!new File(resultPathDir).exists()) {
            new File(resultPathDir).mkdirs();
        }

        analyzeResultMapper.changeStatus(1, "", analyzeResult.getAnalyzeResultKey());
        log.info(analyzeResultKey + ": start analyzing");

        Map<String, String> resultMap = analysisUtil.analysis(series.getSeriesPath(), resultPathDir, series.getModality(),
                series.getPatientWeight(), series.getImagingInterval(), series.getRadionuclideHalfLife(),
                series.getRadionuclideTotalDose());
        log.info(analyzeResultKey + ": " + resultMap.get("err"));
        if (resultMap.containsKey("err") && resultMap.get("err").startsWith("successed")) {
            analyzeResultMapper.updatePath(2, resultMap.get("sourcePath"), resultMap.get("normPath"),
                    resultMap.get("suvPath"), resultMap.get("bpPath"), new Date(),
                    analyzeResult.getAnalyzeResultKey());
            new File(resultMap.get("path")).delete();
        } else {
            analyzeResultMapper.changeStatus(3, resultMap.get("err"), analyzeResult.getAnalyzeResultKey());
            return new RestResult(false, 500, resultMap, resultMap.get("err"));
        }

        return new RestResult(true, 200, resultMap, resultMap.get("err"));
    }


    @ApiOperation(value = "定量分析列表/病灶影像", notes = "有记录调用定量分析返回结果,没有按照默认值")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "analyzeResultKey", value = "分析结果主键key", required = true, dataType = "int")
    })
    @PostMapping("/quantitativeHistory")
    public RestResult quantitativeHistory(@RequestParam int analyzeResultKey) {

        AnalyzeResult analyzeResult = analyzeResultMapper.findByAnalyzeResultKey(analyzeResultKey);
        String statisticalMethod = analyzeResult.getStatisticalMethod();
        String statisticalMode = analyzeResult.getStatisticalMode();
        int abnormalVolume = analyzeResult.getAbnormalVolume();
        double t = analyzeResult.getTValue();

        RestResult restResult = new RestResult(true, 200, "", "");

        //final
        Map<String, Object> result = new HashMap<>();

        result.put("analyzeResult", analyzeResult);

        Map<Double, Map<String, Object>> quantitative;
        //col
        List<Object> resultData = new ArrayList<>();
        //fz
        List<Map<String, Object>> fzList = new ArrayList<>();

        switch (statisticalMode) {
            case "+":
                quantitative = analysisUtil.quantitative(statisticalMethod, true, abnormalVolume, t, analyzeResultKey);
                fzList.add(quantitative.get(-1.0));
                quantitative.remove(-1.0);
                resultData.add(quantitative.values());
                break;
            case "-":
                quantitative = analysisUtil.quantitative(statisticalMethod, false, abnormalVolume, t, analyzeResultKey);
                fzList.add(quantitative.get(-1.0));
                quantitative.remove(-1.0);
                resultData.add(quantitative.values());
                break;
            case "BOTH":
                quantitative = analysisUtil.quantitative(statisticalMethod, true, abnormalVolume, t, analyzeResultKey);
                fzList.add(quantitative.get(-1.0));
                quantitative.remove(-1.0);
                resultData.add(quantitative.values());

                quantitative = analysisUtil.quantitative(statisticalMethod, false, abnormalVolume, t, analyzeResultKey);
                fzList.add(quantitative.get(-1.0));
                quantitative.remove(-1.0);
                resultData.add(quantitative.values());
                break;
        }
        result.put("table", resultData);
        result.put("fz", fzList);
        restResult.setData(result);
        return restResult;
    }


    //定量分析 suv/bq矩阵 + 归一化+脑图谱
    @ApiOperation(value = "定量分析请求", notes = "修改参数计算定量分析(T值矩阵,归一化,实心脑图谱计算)")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "statisticalMethod", value = "统计方式", required = true, dataType = "string"),
            @ApiImplicitParam(name = "statisticalMode", value = "统计模式", required = true, dataType = "string"),
            @ApiImplicitParam(name = "abnormalVolume", value = "病灶体积", required = true, dataType = "double"),
            @ApiImplicitParam(name = "normalControlKey", value = "正常人群组", required = true, dataType = "int"),
            @ApiImplicitParam(name = "p", value = "p值", required = true, dataType = "double"),
            @ApiImplicitParam(name = "analyzeResultKey", value = "分析结果主键key", required = true, dataType = "int")
    })
    @PostMapping("/quantitative")
    public RestResult quantitative(@RequestParam String statisticalMethod,
                                   @RequestParam String statisticalMode,
                                   @RequestParam int abnormalVolume,
                                   @RequestParam int normalControlKey,
                                   @RequestParam double p,
                                   @RequestParam int analyzeResultKey
    ) {
        //final
        Map<String, Object> result = new HashMap<>();

        Map<Double, Map<String, Object>> quantitative;
        //col
        List<Object> resultData = new ArrayList<>();
        //fz
        List<Map<String, Object>> fzList = new ArrayList<>();

        double t = 0;
        if (p == 0.05) {
            t = 1.7171;
        } else if (p == 0.01) {
            t = 2.5083;
        } else if (p == 0.005) {
            t = 2.8188;
        } else if (p == 0.001) {
            t = 3.5050;
        }
        switch (statisticalMode) {
            case "+":
                quantitative = analysisUtil.quantitative(statisticalMethod, true, abnormalVolume, t, analyzeResultKey);
                fzList.add(quantitative.get(-1.0));
                quantitative.remove(-1.0);
                resultData.add(quantitative.values());
                break;
            case "-":
                quantitative = analysisUtil.quantitative(statisticalMethod, false, abnormalVolume, t, analyzeResultKey);
                fzList.add(quantitative.get(-1.0));
                quantitative.remove(-1.0);
                resultData.add(quantitative.values());
                break;
            case "BOTH":
                quantitative = analysisUtil.quantitative(statisticalMethod, true, abnormalVolume, t, analyzeResultKey);
                fzList.add(quantitative.get(-1.0));
                quantitative.remove(-1.0);
                resultData.add(quantitative.values());
                quantitative = analysisUtil.quantitative(statisticalMethod, false, abnormalVolume, t, analyzeResultKey);
                fzList.add(quantitative.get(-1.0));
                quantitative.remove(-1.0);
                resultData.add(quantitative.values());
                break;
            default:
                return new RestResult(false, 500, "", "分析模式错误");
        }
        analyzeResultMapper.updateParams(statisticalMethod, statisticalMode, abnormalVolume, p, t, new Date(), analyzeResultKey);
        result.put("table", resultData);
        result.put("fz", fzList);
        return new RestResult(true, 200, result, "");
    }

    //半定量 归一化和脑图谱
    @ApiOperation(value = "半定量分析", notes = "修改阈值计算半定量分析(归一化,实心脑图谱计算)")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "analyzeResultKey", value = "分析结果主键key", required = true, dataType = "int"),
            @ApiImplicitParam(name = "suvThreshold", value = "suv阈值", required = true, dataType = "double"),
    })
    @PostMapping("/semiQuantitation")
    public RestResult semiQuantitation(@RequestParam int analyzeResultKey,
                                       @RequestParam double suvThreshold) {
        Map<Double, Map<String, Object>> result = analysisUtil.semiQuantitation(analyzeResultKey, suvThreshold);
        return new RestResult(true, 200, result.values(), "");
    }


    @ApiOperation(value = "获得影像信息", notes = "series对应信息")
    @ApiImplicitParam(name = "seriesKey", value = "series主键", required = true, dataType = "int")
    @PostMapping("/dcmInfo")
    public Series dcmInfo(@AuthenticationPrincipal User user, @RequestParam int seriesKey) {
        Series series = seriesMapper.findBySeriesKey(seriesKey);
        int userKey = user.getUserKey();
        UserBindImage userBindImage = userBindImageMapper.findByUserKeySeriesKey(userKey, seriesKey);
        series.setPatient(userBindImage.getPatient());
        return series;

    }


    @ApiOperation(value = "归一化影像", notes = "获得归一化影像矩阵")
    @ApiImplicitParam(name = "analyzeResultKey", value = "分析结果主键key", required = true, dataType = "int")
    @GetMapping("/norm")
    public RestResult norm(@RequestParam int analyzeResultKey) {
        Map<String, Object> resultNorm = new HashMap<>();
        AnalyzeResult analyzeResult = analyzeResultMapper.findByAnalyzeResultKey(analyzeResultKey);

        RestResult restResult;
        try {

            NiftiVolume volume = NiftiVolume.read(analyzeResult.getNormalizedImagePath());
            short x = volume.header.dim[1];
            short y = volume.header.dim[2];
            short z = volume.header.dim[3];
            short dim = volume.header.dim[4];
            float[] pixDim = {analysisUtil.formatFloat(volume.header.pixdim[1], 3),
                    analysisUtil.formatFloat(volume.header.pixdim[2], 3),
                    analysisUtil.formatFloat(volume.header.pixdim[3], 3)};
            short[] normDim = {x, y, z, dim};
            double[][][][] volumeArray = volume.data.toArray();
            INDArray array = Nd4j.create(volumeArray);
            array = array.permute(2, 1, 0, 3);

            float slope;
            if (analyzeResult.getSeries().getModality().equals("SPECT")) {
                slope = 1;
            } else {
                slope = volume.header.scl_slope;
            }

            float inter = volume.header.scl_inter;

            String max = array.max().toString();
            double ww = Double.valueOf(max);
            double wc = ww / 2;

            resultNorm.put("ww", ww);
            resultNorm.put("wc", wc);
            resultNorm.put("normDim", normDim);
            resultNorm.put("pixDim", pixDim);
            resultNorm.put("norm", analysisUtil.toDoubles(array));
            resultNorm.put("slope", slope);
            resultNorm.put("inter", inter);

            restResult = new RestResult(true, 200, resultNorm, "");
        } catch (IOException e) {
            e.printStackTrace();
            restResult = new RestResult(false, 500, "", "读取数据失败");
            return restResult;
        }
        return restResult;
    }


    @ApiOperation(value = "原始影像", notes = "获得原始影像矩阵")
    @ApiImplicitParam(name = "analyzeResultKey", value = "分析结果主键key", required = true, dataType = "int")
    @PostMapping("/dicom")
    public RestResult dicom(@RequestParam int analyzeResultKey) {
        Map<String, Object> resultNorm = new HashMap<>();
        AnalyzeResult analyzeResult = analyzeResultMapper.findByAnalyzeResultKey(analyzeResultKey);
        RestResult restResult;
        try {
            //查找数据库
            NiftiVolume volume = NiftiVolume.read(analyzeResult.getSourcePath());
            short x = volume.header.dim[1];
            short y = volume.header.dim[2];
            short z = volume.header.dim[3];
            short dim = volume.header.dim[4];
            short[] brainAtlasDim = {x, y, z, dim};
            double[][][][] volumeArray = volume.data.toArray();

            float slope;
            if (analyzeResult.getSeries().getModality().equals("SPECT")) {
                slope = 1;
            } else {
                slope = volume.header.scl_slope;
            }
            float inter = volume.header.scl_inter;

            INDArray array = Nd4j.create(volumeArray);
            array = array.permute(2, 1, 0, 3);
            String max = array.max().toString();
            double ww = Double.valueOf(max);
            double wc = ww / 2;
            float[] pixDim = {analysisUtil.formatFloat(volume.header.pixdim[1], 3),
                    analysisUtil.formatFloat(volume.header.pixdim[2], 3),
                    analysisUtil.formatFloat(volume.header.pixdim[3], 3)};

            resultNorm.put("sourceDim", brainAtlasDim);
            resultNorm.put("source", analysisUtil.toDoubles(array));
            resultNorm.put("ww", ww);
            resultNorm.put("wc", wc);
            resultNorm.put("slope", slope);
            resultNorm.put("inter", inter);
            resultNorm.put("pixDim", pixDim);

            restResult = new RestResult(true, 200, resultNorm, "");
        } catch (IOException e) {
            e.printStackTrace();
            restResult = new RestResult(false, 500, "", "读取数据失败");
            return restResult;
        }
        return restResult;

    }

    @ApiOperation(value = "脑图谱", notes = "获得显示脑图谱空心矩阵")
    //@ApiImplicitParam(name = "brainAtlasKey", value = "脑图谱主键", required = true, dataType = "int")
    @GetMapping("/brainAtlas")
    public RestResult brainAtlas() {
        //@RequestParam int brainAtlasKey

        Map<String, Object> resultNorm = new HashMap<>();
        RestResult restResult;
        try {
            //查找数据库
            NiftiVolume volume = NiftiVolume.read(brainAtlasMapper.findByBrainAtlasKey(3));
            short x = volume.header.dim[1];
            short y = volume.header.dim[2];
            short z = volume.header.dim[3];
            short dim = volume.header.dim[4];
            short[] brainAtlasDim = {x, y, z, dim};
            double[][][][] volumeArray = volume.data.toArray();
            INDArray array = Nd4j.create(volumeArray);
            array = array.permute(2, 1, 0, 3);
            double[] volumeArrayResult = analysisUtil.toDoubles(array);
            resultNorm.put("brainAtlasDim", brainAtlasDim);
            resultNorm.put("brainAtlas", volumeArrayResult);
            restResult = new RestResult(true, 200, resultNorm, "");
        } catch (IOException e) {
            e.printStackTrace();
            restResult = new RestResult(false, 500, "", "读取数据失败");
            return restResult;
        }
        return restResult;

    }


    @ApiOperation(value = "suv分析", notes = "修改参数重新调用算法")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "radionuclideTotalDose", value = "注射剂量", required = true, dataType = "string"),
            @ApiImplicitParam(name = "radionuclideHalfLife", value = "半衰期", required = true, dataType = "string"),
            @ApiImplicitParam(name = "patientWeight", value = "体重", required = true, dataType = "string"),
            @ApiImplicitParam(name = "imagingInterval", value = "注射时差", required = true, dataType = "String"),
            @ApiImplicitParam(name = "analyzeResultKey", value = "分析结果主键key", required = true, dataType = "int")
    })
    @PostMapping("/suvAnalysis")
    public RestResult suvAnalysis(@RequestParam String radionuclideTotalDose,
                                  @RequestParam String radionuclideHalfLife,
                                  @RequestParam String patientWeight,
                                  @RequestParam String imagingInterval,
                                  @RequestParam int analyzeResultKey) {
        RestResult restResult;

        analyzeResultMapper.changeStatus(1, "", analyzeResultKey);

        AnalyzeResult analyzeResult = analyzeResultMapper.findByAnalyzeResultKey(analyzeResultKey);
        String normPath = analyzeResult.getNormalizedImagePath();
        String resultPath = new File(normPath).getParent();
        String modality = analyzeResult.getSeries().getModality();
        Map<String, String> result = analysisUtil.suvAnalysis(resultPath, modality, normPath, Double.valueOf(patientWeight),
                Double.valueOf(radionuclideTotalDose), Double.valueOf(radionuclideHalfLife), imagingInterval);

        analyzeResult.setRadionuclideHalfLife(Float.parseFloat(radionuclideHalfLife));
        analyzeResult.setRadionuclideTotalDose(Float.parseFloat(radionuclideTotalDose));
        analyzeResult.setPatientWeight(Float.parseFloat(patientWeight));
        analyzeResult.setImagingInterval(Integer.parseInt(imagingInterval));

        if (result.get("err").startsWith("successed")) {
            analyzeResult.setAnalyzeStatus(2);
            analyzeResult.setBqTPath(result.get("bpPath"));
            analyzeResult.setSuvTPath(result.get("suvPath"));
            analyzeResultMapper.updateSuvParams(analyzeResult);
            restResult = new RestResult(true, 200, analyzeResult.getAnalyzeResultKey(), "");
            new File(result.get("path")).delete();
        } else {
            analyzeResult.setAnalyzeStatus(3);
            analyzeResult.setErrCode(result.get("err"));
            analyzeResultMapper.updateSuvParams(analyzeResult);
            restResult = new RestResult(false, 500, "", result.get("err"));
        }
        log.info(analyzeResultKey + ": " + result.get("err"));
        return restResult;
    }
}
