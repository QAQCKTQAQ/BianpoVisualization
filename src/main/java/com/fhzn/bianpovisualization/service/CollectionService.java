package com.fhzn.bianpovisualization.service;

import ch.qos.logback.core.net.server.Client;
import com.fhzn.bianpovisualization.POJO.Device;
import com.fhzn.bianpovisualization.dao.DataInput;
import com.fhzn.bianpovisualization.util.AccessToken;
import com.fhzn.bianpovisualization.util.DeviceListUtil;
import com.fhzn.bianpovisualization.util.DeviceStatusGet;
import com.fhzn.bianpovisualization.util.DeviceStatusUpdater;
import com.influxdb.client.InfluxDBClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


/**采集运行逻辑
 * 1.获取token！
 * 2.获取设备列表！
 * 3.遍历列表进行一轮设备数据更新
 * 4.开始采集
 * 5.获取token
 * 6.获取设备列表
 * 7.多线程进行一轮设备数据获取！
 * 8.获取到数据后存入数据库！
 *
 * 前端界面优化
 * 1.后端出现问题时，更直观的前端显示
 */
@Service
public class CollectionService {

    private boolean collecting = false;

    @Value("${fhzn.username}")
    private String username;
    @Value("${fhzn.password}")
    private String password;

    @Autowired
    private AccessToken accessToken;  // 用于获取Token的工具类
    @Autowired
    private DeviceListUtil deviceListUtil;  // 用于获取设备列表的工具类
    @Autowired
    private DeviceStatusUpdater deviceStatusUpdater; // 用于更新设备状态的工具类
    @Autowired
    private DeviceStatusGet deviceStatusGet;
    @Autowired
    private DataInput dataInput;

    int maxRetries = 3; // 最大重试次数

    public void startCollecting() {
        collecting = true;
        int attempt = 0;
        while (attempt < maxRetries) {
            try {
                // 第一步：生成 token
                String token = accessToken.getAccessToken(username, password);
                // 第二步：通过 token 获取设备列表
                List<Map<String, Object>> deviceList = deviceListUtil.getDeviceList(token);
                System.out.println("获取用户列表成功");
                // 遍历设备列表并更新状态
                for (Map<String, Object> device : deviceList) {
                    String serial = (String) device.get("serial"); // 假设设备序列号在设备映射中
                    int updateAttempt = 0; // 每个设备的重试计数

                    while (updateAttempt < maxRetries) {
                        try {
                            boolean success = deviceStatusUpdater.updateStatus(token, serial);
                            if (success) {
                                System.out.println("设备 " + serial + " 状态更新成功");
                            } else {
                                System.out.println("设备 " + serial + " 状态更新失败");
                            }
                            break; // 成功后退出重试循环
                        } catch (Exception e) {
                            e.printStackTrace();
                            updateAttempt++;
                            try {
                                Thread.sleep(1000); // 等待1秒后重试
                            } catch (InterruptedException ie) {
                                Thread.currentThread().interrupt();
                            }
                        }
                    }
                }
                break;
            } catch (Exception e) {
                attempt++;
                e.printStackTrace();
                // TODO: 2024/9/24   重试次数过多错误
                try {
                    Thread.sleep(1000); // 等待1秒后重试
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        // 启动定时任务每五分钟调用一次 collectingAndSave
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(this::collectingAndSave, 0, 5, TimeUnit.MINUTES);
    }

    public void collectingAndSave() {
        int attempt = 0;
        // 创建线程池

        ExecutorService executorService = Executors.newFixedThreadPool(20); // 根据需要调整线程池大小

                // 第一步：生成 token
                String token = accessToken.getAccessToken(username, password);
                // 第二步：通过 token 获取设备列表
                List<Map<String, Object>> deviceList = deviceListUtil.getDeviceList(token);
                System.out.println("获取用户列表成功");
                // 遍历设备列表并更新状态


        // 遍历设备列表并为每个设备启动一个新线程
        for (Map<String, Object> device : deviceList) {
            String serial = (String) device.get("serial");

            executorService.submit(() -> {
                Device updatedDevice = deviceStatusGet.getStatus(token, serial);
                InfluxDBClient client=dataInput.initclient();
                dataInput.save(updatedDevice,client);
                dataInput.closeclient(client);
            });
        }

        // 关闭线程池，等待所有任务完成
        executorService.shutdown();
        while (!executorService.isTerminated()) {
            // 等待所有任务完成
        }
        System.out.println("一轮采集完成");

    }

    public void stopCollecting () {
        collecting = false;
    }

    public boolean isCollecting () {
        return this.collecting;
    }

}