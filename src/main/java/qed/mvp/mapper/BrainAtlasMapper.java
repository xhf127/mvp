package qed.mvp.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import qed.mvp.entity.BrainAtlas;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

@Mapper
@Repository
@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT, timeout = 36000, rollbackFor = Exception.class)
public interface BrainAtlasMapper {
    @Select(value = "SELECT brain_atlas_path FROM brain_atlas WHERE brain_atlas_key = #{brainAtlasKey}")
    String findByBrainAtlasKey(int brainAtlasKey);

    @InsertProvider(type = Provider.class, method = "batchInsert")
    void batchInsert(List<BrainAtlas> brainAtlasList);

    @Insert(value = "INSERT INTO brain_atlas (brain_atlas_name, description, brain_atlas_path,create_datetime,update_datetime) " +
            "VALUES(#{brainAtlasName},#{description},#{brainAtlasPath},#{createDatetime},#{updateDatetime})")
    int insert(BrainAtlas brainAtlas);

    class Provider {
        /* 批量插入 */
        public String batchInsert(Map map) {
            List<BrainAtlas> brainAtlasInfos = (List<BrainAtlas>) map.get("list");
            StringBuilder sb = new StringBuilder();
            sb.append("INSERT INTO brain_atlas (brain_atlas_name, description, brain_atlas_path,create_datetime,update_datetime) VALUES ");
            MessageFormat mf = new MessageFormat(
                    "(#'{'list[{0}].brainAtlasName}, #'{'list[{0}].description}, #'{'list[{0}].brainAtlasPath}, #'{'list[{0}].createDatetime},#'{'list[{0}].updateDatetime})"
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
