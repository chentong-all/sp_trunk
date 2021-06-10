package com.ayue.sp.tools;

import com.alibaba.fastjson.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class TimeUtil {

    public static JSONObject time(){
        JSONObject result = new JSONObject();
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String format = simpleDateFormat.format(date);
        String start=format.substring(0,10)+" 00:00:00";
        String end=format.substring(0,10)+" 24:00:00";
        Date parseStart = null;
        Date parseEnd =null;
        try {
            parseStart = simpleDateFormat.parse(start);
            parseEnd = simpleDateFormat.parse(end);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        result.put("start",parseStart);
        result.put("end",parseEnd);
        return result;
    }
}
