package qed.mvp.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import qed.mvp.entity.NormalControl;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

@Mapper
@Repository
@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT, timeout = 36000, rollbackFor = Exception.class)
public interface NormalControlMapper {

    @Select(value = "SELECT * FROM normal_control WHERE normal_control_key = #{normalControlKey}")
    NormalControl findByNormalControlKey(int normalControlKey);

    @InsertProvider(type = Provider.class, method = "batchInsert")
    void batchInsert(List<NormalControl> normalControlList);

    @Insert(value = "INSERT INTO normal_control (normal_control_name, description, modality,bq_suv,person_count, normal_control_path," +
            " create_datetime, update_datetime) " +
            "VALUES(#{normalControlName},#{description},#{modality},,#{bqSuv},#{personCount},#{normalControlPath},#{createDatetime},#{updateDatetime})")
    int insert(NormalControl normalControl);

    class Provider {
        /* 批量插入 */
        public String batchInsert(Map map) {
            List<NormalControl> normalControlInfos = (List<NormalControl>) map.get("list");
            StringBuilder sb = new StringBuilder();
            sb.append("INSERT INTO normal_control (normal_control_name, description, modality,bq_suv,person_count, " +
                    "normal_control_path,create_datetime, update_datetime) VALUES ");
            MessageFormat mf = new MessageFormat(
                    "(#'{'list[{0}].normalControlName}, #'{'list[{0}].description}, #'{'list[{0}].modality}," +
                            " #'{'list[{0}].bqSuv},#'{'list[{0}].personCount}," +
                            "#'{'list[{0}].normalControlPath},#'{'list[{0}].createDatetime},#'{'list[{0}].updateDatetime})"
            );

            for (int i = 0; i < normalControlInfos.size(); i++) {
                sb.append(mf.format(new Object[]{i}));
                if (i < normalControlInfos.size() - 1)
                    sb.append(",");
            }
            return sb.toString();
        }
    }
}
