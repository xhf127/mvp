package qed.mvp.mapper;

import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import qed.mvp.entity.Series;

@Mapper
@Repository
@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT, timeout = 36000, rollbackFor = Exception.class)
public interface SeriesMapper {

    @Select(value = "SELECT * FROM series WHERE series_key = #{seriesKey}")
    Series findBySeriesKey(int seriesKey);

    @Select(value = "SELECT * FROM series WHERE series_instance_uid = #{seriesInstanceUid}  AND series_number=#{seriesNumber}" +
            " AND series.series_path=#{seriesPath}")
    Series findBySeriesInstanceUID(@Param("seriesInstanceUid") String seriesInstanceUid, @Param("seriesNumber") String seriesNumber, @Param("seriesPath") String seriesPath);

    @Insert(value = "INSERT INTO series(study_key,series_instance_uid, series_number, series_date, series_time, " +
            "series_description, modality, manufacturer, station_name, institution_name, acquisition_time, " +
            "radiopharmaceutical_start_date, radiopharmaceutical_start_time, radionuclide_total_dose, " +
            "radionuclide_half_life, acquisition_date, radiopharmaceutical, patient_weight, series_path, " +
            "imaging_interval, slice_thickness, pixel_spacing, window_center, window_width, rescale_intercept," +
            " rescale_slope, study_age,image_count,create_datetime,update_datetime) VALUES (#{studyKey},#{seriesInstanceUid}," +
            "#{seriesNumber},#{seriesDate},#{seriesTime},#{seriesDescription},#{modality},#{manufacturer},#{stationName}," +
            "#{institutionName},#{acquisitionTime},#{radiopharmaceuticalStartDate},#{radiopharmaceuticalStartTime}," +
            "#{radionuclideTotalDose},#{radionuclideHalfLife},#{acquisitionDate},#{radiopharmaceutical},#{patientWeight}," +
            "#{seriesPath},#{imagingInterval},#{sliceThickness},#{pixelSpacing},#{windowCenter},#{windowWidth}," +
            "#{rescaleIntercept},#{rescaleSlope},#{studyAge},#{imageCount},#{createDatetime},#{updateDatetime})")
    @Options(useGeneratedKeys = true, keyProperty = "seriesKey", keyColumn = "series_key")
    void insert(Series series);

    @Update("UPDATE series SET image_count=#{imageCount} WHERE series_key =#{seriesKey}")
    void changeImageCount(@Param("imageCount") int imageCount, @Param("seriesKey") int seriesKey);

}
