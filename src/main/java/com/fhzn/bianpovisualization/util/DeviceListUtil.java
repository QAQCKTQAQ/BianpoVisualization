package com.fhzn.bianpovisualization.util;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class DeviceListUtil {

    @Value("${fhzn.list_url}")
    private String apiUrl;  // 设备列表的 URL

    private final RestTemplate restTemplate;

    public DeviceListUtil(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Retryable
    public List<Map<String, Object>> getDeviceList(String accessToken) {
        // 准备请求数据
        MultiValueMap<String, String> deviceListData = new LinkedMultiValueMap<>();
        deviceListData.add("accessToken", accessToken);
        deviceListData.add("pageNumber", "1");
        deviceListData.add("pageSize", "100");

        // 设置请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // 创建 HttpEntity（包含请求体和请求头）
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(deviceListData, headers);

            // 发起 POST 请求
            ResponseEntity<String> response = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

        JSONObject jsonResponse = new JSONObject(response.getBody());
        if (!jsonResponse.getBoolean("success")) {
            throw new RuntimeException("Error: " + jsonResponse.getString("msg"));
        }

        // 获取 data 字段
        JSONObject data = jsonResponse.getJSONObject("data");
        JSONArray deviceList = data.getJSONArray("list");

        List<Map<String, Object>> devices = new ArrayList<>();
        for (int i = 0; i < deviceList.length(); i++) {
            JSONObject device = deviceList.getJSONObject(i);
            Map<String, Object> deviceMap = new HashMap<>();
            deviceMap.put("serial", device.getString("serial"));
            // 可以添加其他需要的字段
            devices.add(deviceMap);
        }

        return devices; // 返回设备列表
    }
}

