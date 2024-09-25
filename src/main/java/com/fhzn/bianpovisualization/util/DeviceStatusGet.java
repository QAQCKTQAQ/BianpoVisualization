package com.fhzn.bianpovisualization.util;

import com.fhzn.bianpovisualization.POJO.Device;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Component
public class DeviceStatusGet {
    @Value("${fhzn.get_url}")
    String url;

    private final RestTemplate restTemplate;

    public DeviceStatusGet(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Device getStatus(String accessToken, String serial) {
        int attempt = 0;

        while (attempt < 3) { // 最大重试次数为 3
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

                // 解析响应
                JSONObject jsonResponse = new JSONObject(responseEntity.getBody());
                if (jsonResponse.getInt("code") == 1000) {
                    JSONObject data = jsonResponse.getJSONObject("data");
                    System.out.println(data);
                    String deviceSerial = data.getString("serial");
                    double solarPanelPower = data.getDouble("solar_panel_power");
                    double ledPower = data.getDouble("led_power");
                    double batteryPercent = data.getDouble("battery_percent");
                    long timestamp = data.getLong("timestamp");

                    // 创建并返回 Device 对象
                    return new Device(deviceSerial, solarPanelPower, ledPower, batteryPercent, timestamp);
                }

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }

        }
        return null;
    }
}

