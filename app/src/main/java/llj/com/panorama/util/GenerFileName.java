package llj.com.panorama.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class GenerFileName {
    /**
     * 用当前时间作为文件名
     * @return
     */
    public static String generFileName(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        return sdf.format(new Date());
    }
}
