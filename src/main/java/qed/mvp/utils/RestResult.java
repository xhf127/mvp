package qed.mvp.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.joda.time.DateTime;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
public class RestResult {

    /**
     * 请求是否成功
     */
    private boolean success;
    /**
     * 成功或者失败的code错误码
     */
    private int code;
    /**
     * 成功时返回的数据，失败时返回具体的异常信息
     */
    private Object data;
    /**
     * 请求失败返回的提示信息，给前端进行页面展示的信息
     */
    private Object errorMessage;
    /**
     * 服务器当前时间（添加该字段的原因是便于查找定位请求时间，因为实际开发过程中服务器时间可能跟本地时间不一致，加上这个时间戳便于日后定位）
     */
    private Timestamp currentTime;

    @Override
    public String toString() {
        return "RestResult{" +
                "success=" + success +
                ", code='" + code + '\'' +
                ", data=" + data +
                ", errorMessage=" + errorMessage +
                ", currentTime=" + currentTime +
                '}';
    }

    public RestResult(boolean success, int code, Object data, Object errorMessage) {
        this.success = success;
        this.code = code;
        this.data = data;
        this.errorMessage = errorMessage;
        this.currentTime = new Timestamp(new DateTime().getMillis());
    }


}
