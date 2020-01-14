package com.dbj.douyin;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import redis.clients.jedis.Jedis;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DownloadComments {
    public static final Jedis jedis = new Jedis("localhost");

    //将页面数据转换为字符串
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


    public static void main(String[] args) throws IOException, ClassNotFoundException {
        Scanner s = new Scanner(System.in);

        ExecutorService threadPool = Executors.newFixedThreadPool(5);
        while (true) {
            String commentsURL = s.nextLine();
            threadPool.execute(new Task(commentsURL));


        }
//        //获取评论信息
//        JSONArray comments = ((JSONObject) json).getJSONArray("comments");
//        int len = comments.size();
////        System.out.println(len);
//        for (int i = 0; i < len; i++) {
//            String reply_comment = null;
//            //评论
//            String comment = comments.getJSONObject(i).getString("text");
//            //视频id
//            String aweme_id = comments.getJSONObject(i).getString("aweme_id");
//            //获取回复评论
//            if (comments.getJSONObject(i).getJSONArray("reply_comment") == null) {
//                reply_comment = "";
//            } else {
//                reply_comment = comments.getJSONObject(i).getJSONArray("reply_comment").getJSONObject(0).getString("text");
//            }
//            jedis.hset(aweme_id, "comment", comment);
//            jedis.hset(aweme_id, "reply_comment", reply_comment);
//
//        }

//aweme_id=6567524462369967364


    }
}

class Task implements Runnable {
    private String comments;

    public Task() {

    }

    public Task(String comments) {
        this.comments = comments;
    }

    public void run() {
        try {
            URL url = new URL(comments);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("user-agent", "com.ss.android.ugc.aweme/340 (Linux; U; Android 8.0.0; zh_CN; MI 6; Build/OPR1.170623.027; Cronet/58.0.2991.0)");

            InputStream inputStream = conn.getInputStream();
            String jsonStr = DownloadComments.readInputStream(inputStream);
            JSON json = JSON.parseObject(jsonStr);
            //获取视频id
            String aweme_id = ((JSONObject) json).getJSONArray("comments").getJSONObject(0).getString("aweme_id");

            System.out.println(aweme_id);
            DownloadComments.jedis.lpush(aweme_id, jsonStr);
//
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}