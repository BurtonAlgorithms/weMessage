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

package scott.wemessage.server.utils;

public class SentryConfig {

    private String dsn;
    private String release;
    private String environment;

    public SentryConfig(String dsn, String release, String environment){
        this.dsn = dsn;
        this.release = release;
        this.environment = environment;
    }

    public String build(){
        return dsn + "?" + "release=" + release.replaceAll(" ", "+") + "&environment=" + environment + "&servername=weServer&stacktrace.app.packages=";
    }
}
