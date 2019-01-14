package qed.mvp.controller;

import com.aliyuncs.exceptions.ClientException;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.dcm4che3.data.Tag;
import org.dcm4che3.io.DicomOutputStream;
import org.dcm4che3.util.SafeClose;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.social.connect.web.HttpSessionSessionStrategy;
import org.springframework.social.connect.web.SessionStrategy;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.multipart.MultipartFile;
import qed.mvp.authentication.validatedcode.ValidateCode;
import qed.mvp.authentication.validatedcode.ValidatedCodeGenerator;
import qed.mvp.entity.*;
import qed.mvp.mapper.AnalyzeResultMapper;
import qed.mvp.mapper.UserBindImageMapper;
import qed.mvp.mapper.UserMapper;
import qed.mvp.service.AliyunMessage;
import qed.mvp.service.DicomService;
import qed.mvp.utils.DateUtil;
import qed.mvp.utils.RestResult;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.Date;
import java.util.HashMap;

@RestController
@RequestMapping(value = "/user")
@SuppressWarnings("ResultOfMethodCallIgnored")
public class UserController {

    @Value("${tmpDir}")
    private String tmpDir;

    @Value("${dcmDir}")
    private String dcmDir;

    @Value("${dcmSuffix}")
    private String dcmSuffix;

    @Value("${p}")
    private double p;

    @Value("${k}")
    private int k;

    @Value("${t}")
    private double t;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserBindImageMapper userBindImageMapper;

    @Autowired
    private AnalyzeResultMapper analyzeResultMapper;

    @Autowired
    private AliyunMessage aliyunMessage;

    @Autowired
    private DicomService dicomService;

    @Autowired
    private ValidatedCodeGenerator validatedCodeGenerator;

    private final static String SESSION_KEY_PREFIX = "SESSION_KEY_FOR_CODE_SMS";

    private final static String SESSION_KEY_MOBILE = "SESSION_KEY_FOR_MOBILE";

    private SessionStrategy sessionStrategy = new HttpSessionSessionStrategy();

    @ApiOperation(value = "用户信息", notes = "用户信息")
    @GetMapping(value = "/me")
    public RestResult getCurrentUser(@AuthenticationPrincipal User user) {
        User nowUser = userMapper.findUserByMobileNumber(user.getMobileNumber());

        return new RestResult(true, 200, nowUser, "");
    }

    @ApiOperation(value = "修改用户信息", notes = "修改用户信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "mobile", value = "用户手机号", required = true, dataType = "string"),
            @ApiImplicitParam(name = "username", value = "用户姓名", required = true, dataType = "string"),
            @ApiImplicitParam(name = "email", value = "用户邮箱", dataType = "string"),
            @ApiImplicitParam(name = "hospital", value = "用户所属医院", required = true, dataType = "string"),
            @ApiImplicitParam(name = "department", value = "用户科室", dataType = "string"),
            @ApiImplicitParam(name = "title", value = "用户职务", dataType = "string")
    })
    @GetMapping(value = "/changeInfo")
    public RestResult changeUserInfo(@AuthenticationPrincipal User user,
                                     @RequestParam String username,
                                     @RequestParam(defaultValue = "", required = false) String email,
                                     @RequestParam String hospital,
                                     @RequestParam(defaultValue = "", required = false) String department,
                                     @RequestParam(defaultValue = "", required = false) String title) {
        int result = userMapper.changeUserInfo(username, email, hospital,
                department, title, new Date(), user.getUserKey());
        if (result > 0) {

            return new RestResult(true, 200, "修改成功", "");
        } else {
            return new RestResult(false, 500, "", "修改失败");

        }
    }


    @ApiOperation(value = "修改用户密码", notes = "修改用户密码")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "oldPassword", value = "初始密码", dataType = "string"),
            @ApiImplicitParam(name = "newPassword", value = "新密码", dataType = "string")
    })
    @GetMapping(value = "/changePassword")
    public RestResult changePassword(@AuthenticationPrincipal User user,
                                     @RequestParam String oldPassword,
                                     @RequestParam String newPassword) {
        User nowUser = userMapper.findUserByMobileNumber(user.getMobileNumber());
        if (passwordEncoder.matches(oldPassword, nowUser.getPassword())) {
            int result = userMapper.changePassword(passwordEncoder.encode(newPassword), new Date(), user.getUserKey());
            if (result > 0) {
                return new RestResult(true, 200, "修改成功", "");
            } else {
                return new RestResult(false, 500, "", "修改失败");
            }
        } else {
            return new RestResult(false, 500, "", "和原密码不一致");

        }


    }


    @ApiOperation(value = "用户登录", notes = "手机号密码登录")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "username", value = "用户手机号", required = true, dataType = "string"),
            @ApiImplicitParam(name = "password", value = "用户密码", required = true, dataType = "string"),
    })
    @PostMapping(value = "/login")
    public String loginByPassword(@RequestParam String username,
                                  @RequestParam String password) {
        return "";
    }

    @ApiOperation(value = "手机验证码登录", notes = "手机验证码登录")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "mobile", value = "用户手机号", required = true, dataType = "string"),
            @ApiImplicitParam(name = "smsCode", value = "用户密码", required = true, dataType = "string"),
    })
    @PostMapping(value = "/login/mobile")
    public String loginByMobile(@RequestParam String mobile,
                                @RequestParam String smsCode) {
        return "";
    }

    @ApiOperation(value = "注册发送验证码", notes = "发送验证码")
    @ApiImplicitParam(name = "mobileNumber", value = "用户手机号", required = true, dataType = "string")
    @PostMapping(value = "/registerSms")
    public RestResult sendRegisterSms(HttpServletRequest request, @RequestParam String mobileNumber) {

        User exitUser = userMapper.findUserByMobileNumber(mobileNumber);

        RestResult restResult = null;

        HashMap<String, String> params = new HashMap<>();
        ValidateCode validateCode = validatedCodeGenerator.generate(new ServletWebRequest(request));
        int randomNum = Integer.parseInt(validateCode.getCode());

        if (exitUser != null) {
            restResult = new RestResult(false, 500, "", "用户已存在");
            return restResult;
        } else {
            String templateParam = "{\"code\":\"" + randomNum + "\"}";
            params.put("phoneNumbers", mobileNumber);
            params.put("templateParam", templateParam);
        }

        try {
            boolean flag = aliyunMessage.sendSms(params);
            if (flag) {
                restResult = new RestResult(true, 200, "", "");
                sessionStrategy.setAttribute(new ServletWebRequest(request), SESSION_KEY_PREFIX, validateCode);
                sessionStrategy.setAttribute(new ServletWebRequest(request), SESSION_KEY_MOBILE, mobileNumber);

            } else {
                restResult = new RestResult(false, 500, "", "发送失败");
            }
        } catch (ClientException e) {
            e.printStackTrace();
        }
        return restResult;
    }

    @ApiOperation(value = "登录发送验证码", notes = "发送验证码")
    @ApiImplicitParam(name = "mobileNumber", value = "用户手机号", required = true, dataType = "string")
    @PostMapping(value = "/sms")
    public RestResult sendLoginSms(HttpServletRequest request, @RequestParam String mobileNumber) {

        User exitUser = userMapper.findUserByMobileNumber(mobileNumber);

        HashMap<String, String> params = new HashMap<>();
        RestResult restResult = null;
        ValidateCode validateCode = validatedCodeGenerator.generate(new ServletWebRequest(request));
        int randomNum = Integer.parseInt(validateCode.getCode());

        if (exitUser != null) {
            String templateParam = "{\"code\":\"" + randomNum + "\"}";
            params.put("phoneNumbers", mobileNumber);
            params.put("templateParam", templateParam);
        } else {
            restResult = new RestResult(false, 500, "", "您输入的手机号不存在");
            return restResult;
        }

        try {
            boolean flag = aliyunMessage.sendSms(params);

            if (flag) {
                sessionStrategy.setAttribute(new ServletWebRequest(request), SESSION_KEY_PREFIX, validateCode);
                sessionStrategy.setAttribute(new ServletWebRequest(request), SESSION_KEY_MOBILE, mobileNumber);
                restResult = new RestResult(true, 200, "", "");
            } else {
                restResult = new RestResult(false, 500, "", "发送验证码失败");
            }
        } catch (ClientException e) {
            e.printStackTrace();
        }
        return restResult;
    }


    @ApiOperation(value = "注册用户", notes = "根据参数来生成user")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "mobile", value = "用户手机号", required = true, dataType = "string"),
            @ApiImplicitParam(name = "password", value = "用户密码", required = true, dataType = "string"),
            @ApiImplicitParam(name = "username", value = "用户姓名", required = true, dataType = "string"),
            @ApiImplicitParam(name = "email", value = "用户邮箱", dataType = "string"),
            @ApiImplicitParam(name = "hospital", value = "用户所属医院", required = true, dataType = "string"),
            @ApiImplicitParam(name = "department", value = "用户科室", dataType = "string"),
            @ApiImplicitParam(name = "title", value = "用户职务", dataType = "string"),
            @ApiImplicitParam(name = "smsCode", value = "短信验证码", required = true, dataType = "string"),
    })
    @PostMapping(value = "/register")
    public RestResult register(@RequestParam String mobile,
                               @RequestParam String password,
                               @RequestParam String username,
                               @RequestParam(defaultValue = "", required = false) String email,
                               @RequestParam String hospital,
                               @RequestParam(defaultValue = "", required = false) String department,
                               @RequestParam(defaultValue = "", required = false) String title,
                               @RequestParam String smsCode) {
        //重复用户
        User existUser = userMapper.findUserByMobileNumber(mobile);
        if (existUser == null) {
            User user = new User(mobile, mobile, passwordEncoder.encode(password), username, email, hospital, department, title, 0, new Date(), new Date());
            userMapper.insert(user);
            return new RestResult(true, 200, user, "");

        } else {
            return new RestResult(false, 500, null, "用户已存在");
        }
    }

    @ApiOperation(value = "获取用户数据", notes = "读取分析结果最后一条数据")
    @PostMapping(value = "/list")
    public RestResult list(@AuthenticationPrincipal User user) {
        AnalyzeResult analyzeResult = analyzeResultMapper.findByUserKey(user.getUserKey());
        if (analyzeResult == null) {
            return new RestResult(true, 200, null, "");
        }
        Series series = analyzeResult.getSeries();
        UserBindImage userBindImage = userBindImageMapper.findByUserKeySeriesKey(user.getUserKey(), series.getSeriesKey());
        series.setPatient(userBindImage.getPatient());
        analyzeResult.setSeries(series);
        return new RestResult(true, 200, analyzeResult, "");
    }


    @ApiOperation(value = "接受上传数据分析", notes = "上传数据保存至本地,信息写入数据库,分析")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "uploadingFiles", value = "上传文件列表", required = true, dataType = "MultipartFile[]"),

            @ApiImplicitParam(name = "radiopharmaceuticalStartDate", value = "注射日期", required = true, dataType = "string"),
            @ApiImplicitParam(name = "radiopharmaceuticalStartTime", value = "注射时间", required = true, dataType = "string"),
            @ApiImplicitParam(name = "radionuclideTotalDose", value = "注射剂量", required = true, dataType = "string"),
            @ApiImplicitParam(name = "radionuclideHalfLife", value = "半衰期", required = true, dataType = "string"),
            @ApiImplicitParam(name = "radiopharmaceutical", value = "核素", required = true, dataType = "string"),
            @ApiImplicitParam(name = "patientWeight", value = "体重", required = true, dataType = "string"),
            @ApiImplicitParam(name = "imagingInterval", value = "注射时差", required = true, dataType = "int"),
    })
    @PostMapping(value = "/upload")
    public RestResult handleFileUpload(@AuthenticationPrincipal User user,
                                       MultipartFile[] uploadingFiles,
                                       String patientWeight,
                                       int imagingInterval,
                                       String radionuclideHalfLife,
                                       String radionuclideTotalDose,
                                       String radiopharmaceutical,
                                       String radiopharmaceuticalStartDate,
                                       String radiopharmaceuticalStartTime
    ) {

        float patient_weight = Float.parseFloat(patientWeight);
        float radionuclide_halfLife = Float.parseFloat(radionuclideHalfLife);
        float radionuclide_totalDose = Float.parseFloat(radionuclideTotalDose);
        Date radiopharmaceutical_startDate = DateUtil.parseStrToDate(radiopharmaceuticalStartDate, DateUtil.DATE_FORMAT_YYYYMMDD);
        Date radiopharmaceutical_startTime = DateUtil.parseStrToDate(radiopharmaceuticalStartTime, DateUtil.DATE_TIME_FORMAT_HH_MI_SS);

        //userId
        String userId = user.getUserId();

        //dcm保存文件夹
        String dcmPathDir = "";
        DicomReader reader = null;

        String tmpFileDir = tmpDir + userId + "/" + DateUtil.parseDateToStr(new Date(), DateUtil.DATE_TIME_FORMAT_YYYYMMDDHHMISSSSS);
        File tmpFile = new File(tmpFileDir);

        if (!tmpFile.exists()) {
            tmpFile.mkdirs();
        }

        for (int i = 0; i < uploadingFiles.length; i++) {
            try {
                File dcmFile = new File(tmpFileDir + "/" + uploadingFiles[i].getName());
                uploadingFiles[i].transferTo(dcmFile);

                reader = new DicomReader(dcmFile);

                String fileDir = reader.getDataSet().getString(Tag.SeriesInstanceUID);
                String fileName = reader.getFmi().getString(Tag.MediaStorageSOPInstanceUID);
                //userId 保存文件夹
                dcmPathDir = dcmDir + userId + "/" + fileDir;
                File dcmPathDirFile = new File(dcmPathDir);

                //删除文件夹新建文件夹
                if (i == 0) {
                    if (!dcmPathDirFile.exists()) {
                        dcmPathDirFile.mkdirs();
                    }
                }
                //文件名路径
                String fullFile = dcmPathDir + "/" + fileName + dcmSuffix;
                File wFile = new File(fullFile);

                DicomOutputStream dicomOut = new DicomOutputStream(wFile);
                dicomOut.writeDataset(reader.getFmi(), reader.getDataSet());
                SafeClose.close(dicomOut);

                tmpFile.delete();

            } catch (Exception e) {
                e.printStackTrace();
                return new RestResult(false, 500, "", "上传文件失败" + e.getMessage());
            }
        }

        tmpFile.delete();

        //写入数据库
        Series series = dicomService.buildEntities(user.getUserKey(), reader, dcmPathDir, patient_weight, imagingInterval,
                radionuclide_halfLife, radionuclide_totalDose, radiopharmaceutical, radiopharmaceutical_startDate,
                radiopharmaceutical_startTime, uploadingFiles.length);
        if (series == null) {
            return new RestResult(false, 500, "", "写入数据库失败");
        }

        String modality = series.getModality();

        String statisticalMethod;
        if (modality.equals("PET")) {
            statisticalMethod = "SUV";
        } else {
            statisticalMethod = "BQ";
        }

        AnalyzeResult analyzeResult = new AnalyzeResult(user.getUserKey(), 1, 1,
                series.getSeriesKey(), statisticalMethod, "BOTH", k, series.getPatientWeight(),
                series.getAcquisitionDate(), series.getAcquisitionTime(), series.getRadiopharmaceuticalStartDate(),
                series.getRadiopharmaceuticalStartTime(), series.getRadionuclideTotalDose(), series.getRadionuclideHalfLife()
                , p, t, series.getImagingInterval(), 0, new Date(), new Date());
        analyzeResultMapper.insert(analyzeResult);

        if (analyzeResult.getAnalyzeResultKey() == 0) {
            return new RestResult(false, 500, "", "写入数据库失败");
        } else {
            return new RestResult(true, 200, analyzeResult.getAnalyzeResultKey(), "上传写入数据库成功");
        }

    }

}

