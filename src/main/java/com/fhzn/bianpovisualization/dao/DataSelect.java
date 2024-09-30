package com.fhzn.bianpovisualization.dao;

import com.fhzn.bianpovisualization.POJO.Device;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.QueryApi;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;


@Component
public class DataSelect {
    @Value("${influx.url}")
    String url;
    @Value("${influx.token}")
    String token;
    @Value("${influx.data_bucket}")
    String bucket;
    @Value("${influx.org}")
    String org;

    @Retryable
    public InfluxDBClient initclient() {
        InfluxDBClient client = InfluxDBClientFactory.create(url, token.toCharArray(), org, bucket);
        return client;
    }

    @Retryable
    public void closeclient(InfluxDBClient client){
        client.close();
    }

    @Retryable
    public List<Device> queryDeviceData(InfluxDBClient client, String start, String end, String field, String deviceSerial, String every) {
        QueryApi queryapi = client.getQueryApi();
        String flux = String.format(
                "from(bucket: \"%s\")\n" +
                "  |> range(start: %s, stop: %s)\n" +
                "  |> filter(fn: (r) => r[\"_measurement\"] == \"device_data\")\n" +
                "  |> filter(fn: (r) => r[\"_field\"] == \"%s\")\n" +
                "  |> filter(fn: (r) => r[\"device_serial\"] == \"%s\")\n" +
                "  |> aggregateWindow(every: %s, fn: mean, createEmpty: false)\n" +
                "  |> yield(name: \"mean\")",
                bucket,start,end,field,deviceSerial,every
        );
        List<FluxTable> list = queryapi.query(flux);
        List<Device> deviceList = new ArrayList<>();
        for (FluxTable table : list) {
            for (FluxRecord record : table.getRecords()) {
                String serial = record.getValueByKey("device_serial").toString();
                Long time = record.getTime().toEpochMilli();
                double value = (double) record.getValueByKey("_value");
                if(field.equals("battery_percent")){
                    Device device = new Device(serial,0,0,value,time);
                    deviceList.add(device);
                }
                if(field.equals("led_power")){
                    Device device = new Device(serial,0,value,0,time);
                    deviceList.add(device);
                }
                if(field.equals("solar_panel_power")){
                    Device device = new Device(serial,value,0,0,time);
                    deviceList.add(device);
                }
            }

        }
        return deviceList;
    }
}
