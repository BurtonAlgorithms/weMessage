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

import java.io.InputStream;
import java.util.Properties;

import io.sentry.event.EventBuilder;
import io.sentry.event.helper.EventBuilderHelper;
import io.sentry.event.interfaces.DebugMetaInterface;
import io.sentry.util.Util;

import scott.wemessage.server.ServerLogger;

public class SentryEventHelper implements EventBuilderHelper {

    private static String[] cachedProGuardUuids = null;

    @Override
    public void helpBuildingEvent(EventBuilder eventBuilder) {
        String[] proGuardsUuids = getProGuardUuids();

        if (proGuardsUuids != null && proGuardsUuids.length > 0) {
            DebugMetaInterface debugMetaInterface = new DebugMetaInterface();

            for (String proGuardsUuid : proGuardsUuids) {
                debugMetaInterface.addDebugImage(new DebugMetaInterface.DebugImage(proGuardsUuid));
            }

            eventBuilder.withSentryInterface(debugMetaInterface);
        }
    }

    private static String[] getProGuardUuids() {
        if (cachedProGuardUuids != null) {
            return cachedProGuardUuids;
        }

        String[] retVal = new String[0];

        try {
            InputStream is = SentryEventHelper.class.getClassLoader().getResourceAsStream("sentry-meta.properties");
            Properties properties = new Properties();
            properties.load(is);
            is.close();

            String uuid = properties.getProperty("io.sentry.ProguardUuids");
            if (!Util.isNullOrEmpty(uuid)) {
                retVal = uuid.split("\\|");
            }
        } catch (Exception ex) {
            ServerLogger.error("An error occurred while getting Proguard UUIDs", ex, false);
        }

        cachedProGuardUuids = retVal;
        return retVal;
    }
}