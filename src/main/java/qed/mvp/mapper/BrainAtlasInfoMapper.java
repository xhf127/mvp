package qed.mvp.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import qed.mvp.entity.BrainAtlasInfo;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

@Mapper
@Repository
@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT, timeout = 36000, rollbackFor = Exception.class)
public interface BrainAtlasInfoMapper {

    @Insert("INSERT INTO brain_atlas_info(id, name, value,symmetry_value) " +
            "VALUES(#{userId},#{mobileNumber}, #{password},#{username},#{email},#{hospital},#{department},#{title}," +
            "#{userStatus},#{createDatetime},#{updateDatetime})")
    int insert(BrainAtlasInfo brainAtlasInfo);

    @InsertProvider(type = Provider.class, method = "batchInsert")
    void batchInsert(List<BrainAtlasInfo> brainAtlasInfoList);

    @Select("SELECT name,value FROM brain_atlas_info WHERE symmetry_value = (SELECT symmetry_value FROM brain_atlas_info WHERE value = #{brainAtlasValue} ) AND value != #{brainAtlasValue}")
    Map<String, Object> brainValue(double brainAtlasValue);

    @Select("SELECT name,value FROM brain_atlas_info")
    List<Map<String, Object>> selectAll();

    class Provider {
        /* 批量插入 */
        public String batchInsert(Map map) {
            List<BrainAtlasInfo> brainAtlasInfos = (List<BrainAtlasInfo>) map.get("list");
            StringBuilder sb = new StringBuilder();
            sb.append("INSERT INTO brain_atlas_info (id, name, value,symmetry_value) VALUES ");
            MessageFormat mf = new MessageFormat(
                    "(#'{'list[{0}].id}, #'{'list[{0}].name}, #'{'list[{0}].value}, #'{'list[{0}].symmetryValue})"
            );

            for (int i = 0; i < brainAtlasInfos.size(); i++) {
                sb.append(mf.format(new Object[]{i}));
                if (i < brainAtlasInfos.size() - 1)
                    sb.append(",");
            }
            return sb.toString();
        }
    }

}
