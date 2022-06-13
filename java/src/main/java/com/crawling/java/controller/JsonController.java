package com.crawling.java.controller;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Controller
public class JsonController {

    @GetMapping(value = "/testjson")
    public String jsonTest() throws Exception {

        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String hh = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH"));
        String datehh = date + hh;

        URL url = new URL("https://s.search.naver.com/p/influencer/api/v1/collection" +
                "?is_user_id=0&area=ink_kih&query=%EA%B5%AC%EC%B0%8C" +
                "&nlu_query=%7B%22r_category" +
                "%22%3A%2218%22%2C%22intentblock%22%3A%221%22%2C%22v%22%3A%223%22%7D" +
                //"&display=50&where=influencer_api&channel=2" +
                "&display=50&where=influencer_api" +
                "&_callback=$3361_38f34fdfbb544790b1a56dc84f0684d3&_=1654159175037");

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET"); // http 메서드
        //conn.setRequestProperty("Content-Type", "application/json"); // header Content-Type 정보
        conn.setRequestProperty("Content-Type", " application/javascript"); // header Content-Type 정보
        conn.setRequestProperty("auth", "myAuth"); // header의 auth 정보
        conn.setDoOutput(true);

        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line = null;

        while ((line = br.readLine()) != null) {
            sb.append(line);
            //System.out.println("처음 크기 : "  +sb.length());
            //1248926
        }

        String result = sb.toString();

        result = result.substring(0, result.length()-2);

        //System.out.println("); 제거 후 : " + result.length());

        result = result.replaceAll("\\\\t", "");
        result = result.replaceAll("\\\\n", "");
        result = result.replaceAll("\\\\\"","'");

        // u003c 등 아래를 지우지않아도 JSON객체를 생성하면 사라짐
        //result = result.replaceAll("\\\\u003c", "");
        //result = result.replaceAll("\\\\u003e", "");
        //result = result.replaceAll("\\\\u0026", "");

        result = result.substring(result.indexOf("(")+1, result.length());

        JSONObject obj = new JSONObject(result);
        //System.out.println(obj);

        JSONArray itemList_arr = obj.getJSONObject("result").getJSONObject("docs").getJSONArray("itemList");

        //System.out.println(itemList_arr);

        // html  파일 생성경로 (본인 경로에 맞게 수정 필요)
        String filePath = "D:/devfolder/html/crawling/content" + datehh + ".html";

        BufferedWriter bw;
        String newLine = System.lineSeparator();
        try{

            File file = new File(filePath);
            bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath), "UTF8"));

            for (int i = 0; i < itemList_arr.length(); i++) {


                bw.write(itemList_arr.getJSONObject(i).getString("html"));

                //System.out.println(i+1 + ": =================" + itemList_arr.getJSONObject(i).getString("html"));
                bw.write(newLine);
                bw.flush();
            }

        }catch(Exception e){

            e.printStackTrace();
        }
        return "index.html";

    }
}

