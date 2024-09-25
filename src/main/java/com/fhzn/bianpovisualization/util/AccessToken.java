package com.fhzn.bianpovisualization.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

import org.json.JSONObject;  // 需要 org.json 库来解析 JSON
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AccessToken {
    @Value("${fhzn.token_url}")
    private  String apiUrl;

    public  String getAccessToken(String username, String password) {
        System.out.println("Username: " + username + ", Password: " + password);

        try {
            System.out.println(apiUrl);
            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            // 设置请求方法和头部信息
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setDoOutput(true);  // 设置可以发送请求体

            // 准备表单参数
            Map<Object, Object> data = new HashMap<>();
            data.put("username", username);
            data.put("password", password);



            // 将参数转换为 `application/x-www-form-urlencoded` 格式
            StringJoiner sj = new StringJoiner("&");
            for (Map.Entry<Object, Object> entry : data.entrySet()) {
                sj.add(URLEncoder.encode(entry.getKey().toString(), "UTF-8") + "="
                        + URLEncoder.encode(entry.getValue().toString(), "UTF-8"));
            }
            byte[] postData = sj.toString().getBytes(StandardCharsets.UTF_8);
            // 发送请求
            try (OutputStream os = conn.getOutputStream()) {
                os.write(postData);
            }

            // 获取响应
            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) { // 200 表示成功
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                // 解析响应
                JSONObject resJson = new JSONObject(response.toString());

                // 直接获取 "data" 字段
                String accessToken = resJson.getString("data");
                if (accessToken != null) {
                    System.out.println("INFO: get accesstoken success, accesstoken is " + accessToken);
                    return accessToken;  // 返回 token
                } else {
                    System.out.println("ERROR: Unable to fetch accessToken");
                }

            } else {
                System.out.println("ERROR: Server returned status code " + responseCode);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;  // 如果获取失败，返回 null
    }

}



