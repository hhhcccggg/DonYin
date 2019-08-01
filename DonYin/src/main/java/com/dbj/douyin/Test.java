package com.dbj.douyin;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

public class Test {
    public static void main(String[] args) throws MalformedURLException, IOException {
        Scanner s = new Scanner(System.in);
        String searchURL = s.nextLine();
        URL serach = new URL(searchURL);
        HttpURLConnection conn = (HttpURLConnection) serach.openConnection();
        conn.setRequestProperty("user-agent", "com.ss.android.ugc.aweme/340 (Linux; U; Android 8.0.0; zh_CN; MI 6; Build/OPR1.170623.027; Cronet/58.0.2991.0)");
        conn.setRequestProperty("Connection", "Keep-Alive");
        InputStream inputStream = conn.getInputStream();

        String serachStr = readInputStream(inputStream);
        System.out.println(serachStr);
        //将seach信息转为json
        JSON json = JSON.parseObject(serachStr, JSON.class);
        JSONArray aweme_list = ((JSONObject) json).getJSONArray("aweme_list");
        //获取分享的URL
        String shareURL = aweme_list.getJSONObject(0).getString("desc");
        //获取描述
        String desc = aweme_list.getJSONObject(0).getString("desc");
        System.out.println(desc);
    }

    public static String readInputStream(InputStream inputStream) throws IOException {
        byte[] buffer = new byte[1024];
        int len = 0;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        while ((len = inputStream.read(buffer)) != -1) {
            bos.write(buffer, 0, len);
        }
        bos.close();
        return bos.toString();
    }
}
