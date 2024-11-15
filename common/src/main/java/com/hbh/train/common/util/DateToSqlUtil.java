package com.hbh.train.common.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateToSqlUtil {
    public static java.sql.Date dataToSql(Date date){
        Date utilDate = date;

        // 示例2：使用SimpleDateFormat格式化日期
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String formattedDate = sdf.format(utilDate);

        // 示例3：将java.util.Date转换为java.sql.Date
        java.sql.Date sqlDate = new java.sql.Date(utilDate.getTime());
        return sqlDate;
    }
}
