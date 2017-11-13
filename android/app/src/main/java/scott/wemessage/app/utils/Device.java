package scott.wemessage.app.utils;

import java.util.ArrayList;

import scott.wemessage.commons.utils.StringUtils;

class Device {

    private String manufacturer, name, model;

    Device(String manufacturer, String name, String model){
        this.manufacturer = manufacturer;
        this.name = name;
        this.model = model;
    }

    String parse(){
        ArrayList<String> parsed = new ArrayList<>();

        parsed.add(manufacturer);
        parsed.add(name);
        parsed.add(model);

        return StringUtils.join(parsed, ",", 1);
    }

    String getManufacturer() {
        return manufacturer;
    }

    String getName() {
        return manufacturer + " " + name;
    }

    String getModel() {
        return model;
    }

    static Device fromString(String s){
        String[] strings = s.split(",");

        return new Device(strings[0], strings[1], strings[2]);
    }
}