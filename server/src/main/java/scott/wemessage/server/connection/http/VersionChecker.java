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

package scott.wemessage.server.connection.http;

import com.google.gson.Gson;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import scott.wemessage.commons.connection.FirebaseVersionMessage;
import scott.wemessage.commons.utils.StringUtils;
import scott.wemessage.server.MessageServer;
import scott.wemessage.server.ServerLogger;
import scott.wemessage.server.weMessage;

public class VersionChecker extends Thread {

    private MessageServer messageServer;

    public VersionChecker(MessageServer messageServer){
        this.messageServer = messageServer;
    }

    @Override
    public void run() {
        try {
            if (!messageServer.getConfiguration().getConfigJSON().getConfig().getCheckForUpdates()) return;
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(weMessage.GET_VERSION_FUNCTION_URL).build();
            Response response = client.newCall(request).execute();
            FirebaseVersionMessage versionMessage = new Gson().fromJson(response.body().string(), FirebaseVersionMessage.class);

            if (!versionMessage.getLatestVersion().equals(weMessage.WEMESSAGE_VERSION)) {
                ServerLogger.log(ServerLogger.Level.INFO, "A new weServer version has been found! Download the latest one off of the website: https://wemessageapp.com");
                ServerLogger.emptyLine();
                ServerLogger.log(StringUtils.toFixedString("", ServerLogger.Level.WARNING.getPrefix().length()) + " Current Version: " + weMessage.WEMESSAGE_VERSION + " \t Latest Version: " + versionMessage.getLatestVersion());
                ServerLogger.emptyLine();
            }

            response.close();
        }catch (Exception ex){
            ServerLogger.error("An error occurred while checking for updates", ex);
        }
    }
}