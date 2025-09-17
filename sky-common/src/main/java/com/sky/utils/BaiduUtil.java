package com.sky.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sky.properties.BaiduProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriUtils;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class BaiduUtil {

    @Autowired
    private BaiduProperties baiduProperties;

    //API服务地址
    public static String BAIDU_MAP_GEOCODING_URL = "https://api.map.baidu.com/geocoding/v3/?";
    public static String BAIDU_MAP_RIDING_URL = "https://api.map.baidu.com/directionlite/v1/riding?";

    //计算用户与商家的距离
    private Long getDistanceFromApi(String url, Map<String, String> param) throws Exception {
        JSONObject jsonObject = requestOfLocation(url, param);

        JSONArray routes = jsonObject.getJSONObject("result").getJSONArray("routes");
        JSONObject routesJSONObject = routes.getJSONObject(0);

        //转为公里
        Long distance = routesJSONObject.getLong("distance") / 1000L;
        return distance;
    }

    //获取地址经纬度
    private String getCurrentAddressLatAndLon(String address) throws Exception {

        Map params = new LinkedHashMap<String, String>();
        params.put("address", address);
        params.put("output", "json");
        params.put("ak", baiduProperties.getAk());
        params.put("callback", "showLocation");


        //调用百度请求获得位置的详细信息
        JSONObject jsonObject = requestOfLocation(BAIDU_MAP_GEOCODING_URL, params);

        //解析数据
        JSONObject location = jsonObject.getJSONObject("result").getJSONObject("location");

        Double lng = location.getDouble("lng");
        Double lat = location.getDouble("lat");
        return lat + "," +lng;
    }

    //获取用户和商家的距离
    public Long getDistance(String userAddress) throws Exception {

        Map params = new LinkedHashMap<String, String>();
        params.put("origin", getCurrentAddressLatAndLon(baiduProperties.getLocation()));
        params.put("destination", getCurrentAddressLatAndLon(userAddress));
        params.put("ak", baiduProperties.getAk());

        Long distance = getDistanceFromApi(BAIDU_MAP_RIDING_URL, params);
        return distance;
    }


    //调用百度API获取地址信息
    private JSONObject requestOfLocation(String strUrl, Map<String, String> param) throws Exception {
        if (strUrl == null || strUrl.length() <= 0 || param == null || param.size() <= 0) {
            return null;
        }

        StringBuffer queryString = new StringBuffer();
        queryString.append(strUrl);
        for (Map.Entry<?, ?> pair : param.entrySet()) {
            queryString.append(pair.getKey() + "=");
            //    第一种方式使用的 jdk 自带的转码方式  第二种方式使用的 spring 的转码方法 两种均可
            //    queryString.append(URLEncoder.encode((String) pair.getValue(), "UTF-8").replace("+", "%20") + "&");
            queryString.append(UriUtils.encode((String) pair.getValue(), "UTF-8") + "&");
        }

        if (queryString.length() > 0) {
            queryString.deleteCharAt(queryString.length() - 1);
        }

        java.net.URL url = new URL(queryString.toString());
        System.out.println(queryString.toString());
        URLConnection httpConnection = (HttpURLConnection) url.openConnection();
        httpConnection.connect();

        InputStreamReader isr = new InputStreamReader(httpConnection.getInputStream());
        BufferedReader reader = new BufferedReader(isr);
        StringBuffer buffer = new StringBuffer();
        String line;
        while ((line = reader.readLine()) != null) {
            buffer.append(line);
        }
        reader.close();
        isr.close();

        //将响应回来的数据转为JSONObject对象
        String result = String.valueOf(buffer);
        int start = result.indexOf("{");
        int end = result.lastIndexOf("}");
        return JSONObject.parseObject(result.substring(start, end + 1));
    }
}
