/*
 *  weMessage - iMessage for Android
 *  Copyright (C) 2018 Roman Scott
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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
        try {
            String[] strings = s.split(",");

            return new Device(strings[0], strings[1], strings[2]);
        }catch (Exception ex){
            return null;
        }
    }
}