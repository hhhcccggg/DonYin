package com.dbj.douyin;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import redis.clients.jedis.Jedis;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 根据视频下载链接 下载视频 后缀为 .mp4 等
 *
 * @author lenovo
 */

public class DownloadVideo {
    private static String savePath = "E:\\douyin11.23";
    public static final Jedis jedis = new Jedis("localhost");


    //根据url下载视频
    public static void saveVideo(String realURL, String aweme_id) throws IOException {
        URL url = new URL(realURL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(30000);
        conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");

        InputStream inputStream = conn.getInputStream();
        byte[] getData = readInputStream(inputStream);
        java.io.File saveDir = new java.io.File(savePath);

        //根据aweme_id生成视频文件名
        String fileName = "dbj" + aweme_id + "." + "mp4";

        java.io.File file = new java.io.File(saveDir + java.io.File.separator + fileName);
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(getData);

        if (fos != null) {
            fos.close();
        }
        if (inputStream != null) {
            inputStream.close();
        }


    }

    public static byte[] readInputStream(InputStream inputStream) throws IOException {
        byte[] buffer = new byte[1024];
        int len = 0;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        while ((len = inputStream.read(buffer)) != -1) {
            bos.write(buffer, 0, len);
        }
        bos.close();
        return bos.toByteArray();
    }

    //解析url
    public static String analysisURL(String url) throws InterruptedException {
        String downloadUrl = null;
        System.setProperty("webdriver.chrome.driver", "C:Users/entic/Downloads/chromedriver_win32/chromedriver.exe");
        // 实例化一个浏览器对象
        WebDriver driver = new ChromeDriver();
        driver.manage().timeouts().pageLoadTimeout(120, TimeUnit.SECONDS);
        driver.get("http://douyin.iiilab.com/");// 打开可以将每个视频链接转化成可以下载的链接的网页

        Thread.sleep(8000);// 休眠等待页面加载
        //获取可以下载的url
        try {
            driver.findElement(By.cssSelector("input.form-control.link-input")).clear();// 清空这个输入框
            driver.findElement(By.cssSelector("input.form-control.link-input")).sendKeys(url);// 将需要转换的链接放入该输入框中
            driver.findElement(By.cssSelector("button.btn.btn-default")).click();// 点击解析
            Thread.sleep(4000);// 休眠等待页面加载
            downloadUrl = driver.findElement(By.cssSelector("a.btn.btn-success")).getAttribute("href").toString();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            driver.close();
        }
        return downloadUrl;


    }


    //对原始url进行处理，返回需要的数据

    public static String[] searchURL(String searchURL) throws MalformedURLException, IOException {
        String[] datas = new String[3];
        URL serach = new URL(searchURL);
        HttpURLConnection conn = (HttpURLConnection) serach.openConnection();
        conn.setRequestProperty("user-agent", "com.ss.android.ugc.aweme/340 (Linux; U; Android 8.0.0; zh_CN; MI 6; Build/OPR1.170623.027; Cronet/58.0.2991.0)");

        InputStream inputStream = conn.getInputStream();
        byte[] bytes = readInputStream(inputStream);
        String serachStr = bytes.toString();

        //将seach信息转为json
        JSON json = JSON.parseObject(serachStr);


        //获取视频列表信息
        JSONArray aweme_list = ((JSONObject) json).getJSONArray("aweme_list");

        //获取视频id
        String aweme_id = ((JSONObject) json).getJSONArray("aweme_list").getString(2);
        datas[0] = aweme_id;

        //获取视频描述
        String desc = aweme_list.getString(9);
        datas[1] = desc;

        //获取分享的URL
        String shareURL = aweme_list.getString(30);
        datas[2] = shareURL;

        return datas;
    }


    public static void main(String[] args) throws InterruptedException {
        Scanner s = new Scanner(System.in);
        ExecutorService threadPool = Executors.newFixedThreadPool(10);
        while (true) {

            String aweme_id = s.nextLine();
            String desc = s.nextLine();
            String shareURL = s.nextLine();
            aweme_id = aweme_id.replace("aweme_id=", "");
            desc = desc.replace("desc=", "");

            threadPool.execute(new VideoTask(aweme_id, desc, shareURL));


        }

    }

}

class VideoTask implements Runnable {
    private String desc;
    private String shareURL;
    private String aweme_id;

    public VideoTask(String aweme_id, String desc, String shareURL) {
        this.desc = desc;
        this.shareURL = shareURL;
        this.aweme_id = aweme_id;
    }

    public void run() {

        if (DownloadVideo.jedis.get("dbj" + aweme_id) != null) {
            System.out.println("视频已存在");
        } else {
            try {
                //解析分享的url
                String realURL = DownloadVideo.analysisURL(shareURL);
                //下载文件
                DownloadVideo.saveVideo(realURL, aweme_id);
                //保存视频描述
                DownloadVideo.jedis.set("dbj" + aweme_id, desc);
            } catch (Exception e) {

            }
        }
    }


}

