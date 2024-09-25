package com.fhzn.bianpovisualization.dao;

import com.fhzn.bianpovisualization.POJO.Device;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DataInput {
    @Value("${influx.url}")
    String url;
    @Value("${influx.token}")
    String token;
    @Value("${influx.data_bucket}")
    String bucket;
    @Value("${influx.org}")
    String org;

    public InfluxDBClient initclient() {
        InfluxDBClient client = InfluxDBClientFactory.create(url, token.toCharArray(), org, bucket);
        return client;
    }

    public void closeclient(InfluxDBClient client){
        client.close();
    }

    public void save(Device data, InfluxDBClient client) {
        Point point = Point.measurement("device_data") // 指定测量名
                .addTag("device_serial", data.getDeviceSerial())
                .time(data.getTimestamp(), WritePrecision.MS) // 设置时间戳
                .addField("solar_panel_power", data.getSolarPanelPower())
                .addField("led_power", data.getLedPower())
                .addField("battery_percent", data.getBatteryPercent());

        client.getWriteApiBlocking().writePoint(point);
        System.out.println("Data saved to InfluxDB: " + data.getDeviceSerial());
    }
}
