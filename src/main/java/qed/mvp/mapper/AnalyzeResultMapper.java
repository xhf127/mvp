package qed.mvp.mapper;

import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import qed.mvp.entity.AnalyzeResult;

import java.util.Date;
import java.util.List;

@Mapper
@Repository
@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT, timeout = 36000, rollbackFor = Exception.class)
public interface AnalyzeResultMapper {

    @Select(value = "SELECT * FROM analyze_result WHERE user_key = #{userKey} AND series_key = #{seriesKey}")
    List<AnalyzeResult> findByUserKeyAndSeriesKey(@Param("userKey") int userKey, @Param("seriesKey") int seriesKey);

    @Select({"SELECT * FROM analyze_result WHERE user_key = #{userKey} order by analyze_result_key desc limit 1"})
    @Results({
            @Result(property = "series", column = "series_key", one = @One(select = "qed.mvp.mapper.SeriesMapper.findBySeriesKey")),
            @Result(property = "seriesKey", column = "series_key")
    })
    AnalyzeResult findByUserKey(@Param("userKey") int userKey);

    @Select(value = "SELECT * FROM analyze_result WHERE analyze_result_key = #{analyzeResultKey}")
    @Results({
            @Result(property = "series", column = "series_key", one = @One(select = "qed.mvp.mapper.SeriesMapper.findBySeriesKey")),
            @Result(property = "seriesKey", column = "series_key"),
            @Result(property = "brainAtlasPath", column = "brain_atlas_key", one = @One(select = "qed.mvp.mapper.BrainAtlasMapper.findByBrainAtlasKey")),
            @Result(property = "brainAtlasKey", column = "brain_atlas_key")
    })
    AnalyzeResult findByAnalyzeResultKey(int analyzeResultKey);

    @Insert("INSERT INTO analyze_result(user_key, brain_atlas_key, normal_control_key, series_key, statistical_method," +
            " statistical_mode, abnormal_volume, patient_weight, acquisition_date, acquisition_time," +
            " radiopharmaceutical_start_date, radiopharmaceutical_start_time, radionuclide_total_dose," +
            " radionuclide_half_life, p_value,t_value,imaging_interval, analyze_status,source_path," +
            " normalized_image_path, suv_t_path, bq_t_path," +
            " create_datetime, update_datetime)" +
            " VALUES (#{userKey},#{brainAtlasKey},#{normalControlKey},#{seriesKey},#{statisticalMethod},#{statisticalMode}" +
            ",#{abnormalVolume},#{patientWeight},#{acquisitionDate},#{acquisitionTime},#{radiopharmaceuticalStartDate}," +
            "#{radiopharmaceuticalStartTime},#{radionuclideTotalDose},#{radionuclideHalfLife},#{pValue},#{tValue},#{imagingInterval}" +
            ",#{analyzeStatus},#{sourcePath},#{normalizedImagePath},#{suvTPath},#{bqTPath},#{createDatetime},#{updateDatetime})")
    @Options(useGeneratedKeys = true, keyProperty = "analyzeResultKey", keyColumn = "analyze_result_key")
    void insert(AnalyzeResult analyzeResult);

    @Update("UPDATE analyze_result SET analyze_status=#{analyzeStatus}, err_code=#{errCode} WHERE analyze_result_key =#{analyzeResultKey}")
    void changeStatus(@Param("analyzeStatus") int analyzeStatus, @Param("errCode") String errCode, @Param("analyzeResultKey") int analyzeResultKey);

    @Update("UPDATE analyze_result SET analyze_status=#{analyzeStatus}, source_path=#{sourcePath}," +
            " normalized_image_path=#{normalizedImagePath},suv_t_path=#{suvTPath},bq_t_path=#{bqTPath},update_datetime=#{updateTime} WHERE analyze_result_key =#{analyzeResultKey}")
    void updatePath(@Param("analyzeStatus") int analyzeStatus, @Param("sourcePath") String sourcePath,
                    @Param("normalizedImagePath") String normalizedImagePath, @Param("suvTPath") String suvTPath,
                    @Param("bqTPath") String bqTPath, @Param("updateTime") Date updateTime, @Param("analyzeResultKey") int analyzeResultKey);

    @Update("UPDATE analyze_result SET statistical_method=#{statisticalMethod},statistical_mode=#{statisticalMode}," +
            "p_value=#{pValue},t_value=#{tValue},abnormal_volume=#{abnormalVolume} ,update_datetime=#{updateTime} WHERE analyze_result_key =#{analyzeResultKey}")
    void updateParams(@Param("statisticalMethod") String statisticalMethod,
                      @Param("statisticalMode") String statisticalMode, @Param("abnormalVolume") int abnormalVolume,
                      @Param("pValue") double pValue, @Param("tValue") double tValue,
                      @Param("updateTime") Date updateTime, @Param("analyzeResultKey") int analyzeResultKey);

    @Update("UPDATE analyze_result SET analyze_status=#{analyzeStatus}, err_code=#{errCode} ,patient_weight =#{patientWeight}," +
            "radionuclide_total_dose=#{radionuclideTotalDose},radionuclide_half_life=#{radionuclideHalfLife}, " +
            "imaging_interval=#{imagingInterval} WHERE analyze_result_key =#{analyzeResultKey}")
    void updateSuvParams(AnalyzeResult analyzeResult);

}
