package com.fhzn.bianpovisualization.controller;

import com.fhzn.bianpovisualization.dao.DataSelect;
import com.fhzn.bianpovisualization.service.CollectionService;
import com.fhzn.bianpovisualization.util.DeviceListUtil;
import com.fhzn.bianpovisualization.util.AccessToken;
import com.fhzn.bianpovisualization.util.DeviceStatusGet;
import com.fhzn.bianpovisualization.util.DeviceStatusUpdater;
import com.influxdb.client.InfluxDBClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.fhzn.bianpovisualization.POJO.Device;
import org.springframework.core.io.ClassPathResource;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;


import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class DeviceController {
    @Value("${fhzn.username}")
    private String username;
    @Value("${fhzn.password}")
    private String password;

    @Autowired
    private AccessToken accessToken;  // 用于获取Token的工具类
    @Autowired
    private DeviceListUtil deviceListUtil;  // 用于获取设备列表的工具类
    @Autowired
    private CollectionService collectionService;
    @Autowired
    private DeviceStatusUpdater deviceStatusUpdater;
    @Autowired
    private DeviceStatusGet deviceStatusGet;
    @Autowired
    private DataSelect dataSelect;

    final int maxRetries = 3; // 最大重试次数

//    // 接收前端请求，生成 token 并获取设备列表
//    @GetMapping("/device_serial_map")
//    public ResponseEntity<List<Map<String, Object>>> getDeviceSerialMap() {
//        int attempt=0;
//        while (attempt < maxRetries) {
//            try {
//                // 第一步：生成 token
//                String token = accessToken.getAccessToken(username, password);
//                System.out.println("获取token成功");
//
//                // 第二步：通过 token 获取设备列表
//                List<Map<String, Object>> deviceList = deviceListUtil.getDeviceList(token);
//
//                // 第三步：返回设备列表给前端
//                return new ResponseEntity<>(deviceList, HttpStatus.OK);
//            } catch (Exception e) {
//                attempt++;
//                e.printStackTrace();
//                System.out.println("获取设备列表失败，尝试次数: " + attempt);
//
//                // 如果达到最大重试次数，则抛出异常
//                if (attempt >= maxRetries) {
//                    throw new RuntimeException("获取设备列表失败，已达到最大重试次数");
//                }
//
//                try {
//                    Thread.sleep(1000); // 等待1秒后重试
//                } catch (InterruptedException ie) {
//                    Thread.currentThread().interrupt();
//                }
//            }
//        }
//        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
//    }

    @GetMapping("/device_map")
    public ResponseEntity<Map<String, Map<String, String>>> getDeviceMap() {
        // 存储设备名到设备ID的映射
        Map<String, String> deviceNameToId = new HashMap<>();
        // 存储设备ID到设备名的映射
        Map<String, String> deviceIdToName = new HashMap<>();

        try {
            // 读取 map.csv 文件
            ClassPathResource resource = new ClassPathResource("map.csv");
            BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()));

            String line;
            reader.readLine(); // 跳过表头
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                String deviceName = data[0].trim();
                String deviceId = data[1].trim();

                // 构建两个映射
                deviceNameToId.put(deviceName, deviceId);
                deviceIdToName.put(deviceId, deviceName);
            }
            reader.close();

            // 返回包含两个映射的结果
            Map<String, Map<String, String>> result = new HashMap<>();
            result.put("deviceNameToId", deviceNameToId);
            result.put("deviceIdToName", deviceIdToName);

            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @EventListener(ApplicationReadyEvent.class)
    public void deviceCollection(){
        collectionService.startCollecting();
    }

//    @PostMapping("/close")
//    public void closeCollection(){
//        collectionService.stopCollecting();
//    }

    @PostMapping("/data")
    public List<Device> deviceData(@RequestBody Map<String, Object> requestData){
        String deviceSerial = (String) requestData.get("device_id");
        String end = (String) requestData.get("end");
        String every = (String) requestData.get("every");
        String start = (String) requestData.get("start");
        String field = (String) requestData.get("field");

        InfluxDBClient client = dataSelect.initclient();
        List<Device> list = dataSelect.queryDeviceData(client,start,end,field,deviceSerial,every);
        dataSelect.closeclient(client);
        return list;
    }
}
