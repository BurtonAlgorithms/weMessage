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