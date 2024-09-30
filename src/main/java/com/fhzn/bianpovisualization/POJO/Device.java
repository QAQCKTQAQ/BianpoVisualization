package com.fhzn.bianpovisualization.POJO;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Device {
    private String deviceSerial;     // 设备编号
    private double solarPanelPower;   // 太阳能板功率
    private double ledPower;          // LED 功率
    private double batteryPercent;     // 电池百分比
    private long timestamp;            // 时间戳

    // 构造函数
    public Device(String deviceSerial, double solarPanelPower, double ledPower, double batteryPercent, long timestamp) {
        this.deviceSerial = deviceSerial;
        this.solarPanelPower = solarPanelPower;
        this.ledPower = ledPower;
        this.batteryPercent = batteryPercent;
        this.timestamp = timestamp;
    }

    // Getters 和 Setters
    public String getDeviceSerial() {
        return deviceSerial;
    }

    public void setDeviceSerial(String deviceSerial) {
        this.deviceSerial = deviceSerial;
    }

    public double getSolarPanelPower() {
        return solarPanelPower;
    }

    public void setSolarPanelPower(double solarPanelPower) {
        this.solarPanelPower = solarPanelPower;
    }

    public double getLedPower() {
        return ledPower;
    }

    public void setLedPower(double ledPower) {
        this.ledPower = ledPower;
    }

    public double getBatteryPercent() {
        return batteryPercent;
    }

    public void setBatteryPercent(double batteryPercent) {
        this.batteryPercent = batteryPercent;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}

