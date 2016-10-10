package ru.radiationx.meizubattery;

class BatteryInfo {
    final static String PRESENT = "PRESENT";
    final static String CAPACITY = "CAPACITY";
    final static String TEMP = "TEMP";
    final static String VOLTAGE_NOW = "VOLTAGE_NOW";
    final static String VOLTAGE_AVG = "VOLTAGE_AVG";
    final static String CURRENT_NOW = "CURRENT_NOW";
    final static String CURRENT_AVG = "CURRENT_AVG";
    final static String HEALTH = "HEALTH";
    final static String MANUFACTURER = "MANUFACTURER";
    private int present, capacity;
    private float temp, voltageNow, currentNow, voltageAvg, currentAvg, currentFull;
    private CharSequence health, manufacturer;

    int getPresent() {
        return present;
    }

    void setPresent(int present) {
        this.present = present;
    }

    int getCapacity() {
        return capacity;
    }

    void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    float getTemp() {
        return temp;
    }

    void setTemp(float temp) {
        this.temp = temp;
    }

    float getVoltageNow() {
        return voltageNow;
    }

    void setVoltageNow(float voltageNow) {
        this.voltageNow = voltageNow;
    }

    float getCurrentNow() {
        return currentNow;
    }

    void setCurrentNow(float currentNow) {
        this.currentNow = currentNow;
    }

    float getVoltageAvg() {
        return voltageAvg;
    }

    void setVoltageAvg(float voltageAvg) {
        this.voltageAvg = voltageAvg;
    }

    float getCurrentAvg() {
        return currentAvg;
    }

    void setCurrentAvg(float currentAvg) {
        this.currentAvg = currentAvg;
    }

    float getCurrentFull() {
        return currentFull;
    }

    void setCurrentFull(float currentFull) {
        this.currentFull = currentFull;
    }

    CharSequence getHealth() {
        return health;
    }

    void setHealth(CharSequence health) {
        this.health = health;
    }

    CharSequence getManufacturer() {
        return manufacturer;
    }

    void setManufacturer(CharSequence manufacturer) {
        this.manufacturer = manufacturer;
    }
}
