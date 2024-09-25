package com.fhzn.bianpovisualization.util;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Component
public class DeviceStatusUpdater {
    @Value("${fhzn.update_url}")
    String url;

    private final RestTemplate restTemplate;

    public DeviceStatusUpdater(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public boolean updateStatus(String accessToken, String serial) {
        try {
            MultiValueMap<String, String> deviceStatusData = new LinkedMultiValueMap<>();
            deviceStatusData.add("accessToken", accessToken);
            deviceStatusData.add("serial", serial);

            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/x-www-form-urlencoded");

            // 创建请求实体
            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(deviceStatusData, headers);

            // 发送 POST 请求
            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            // 处理响应
            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                String responseBody = responseEntity.getBody();
                JSONObject jsonResponse = new JSONObject(responseBody);
                return jsonResponse.optBoolean("success", false); // 默认为 false
            } else {
                System.out.println("HTTP 错误代码: " + responseEntity.getStatusCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false; // 出现异常时返回 false
    }
}
