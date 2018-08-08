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

package scott.wemessage.server.listeners.connection;

import scott.wemessage.commons.connection.json.action.JSONAction;
import scott.wemessage.commons.connection.json.message.JSONMessage;
import scott.wemessage.server.ServerLogger;
import scott.wemessage.server.connection.Device;
import scott.wemessage.server.events.Event;
import scott.wemessage.server.events.Listener;
import scott.wemessage.server.events.connection.ClientMessageReceivedEvent;

public class ClientMessageReceivedListener extends Listener {

    public ClientMessageReceivedListener(){
        super(ClientMessageReceivedEvent.class);
    }

    public void onEvent(Event event){
        ClientMessageReceivedEvent e = (ClientMessageReceivedEvent) event;

        if (e.getWasActionSuccessful() != null && e.getWasActionSuccessful()) {
            try {
                if (e.getClientMessage().isJsonOfType(JSONAction.class)) {
                    e.getDeviceManager().getMessageServer().getDatabaseManager().queueAction((JSONAction) e.getClientMessage().getIncoming(JSONAction.class));
                }
            } catch (Exception ex) {
                ServerLogger.error("An error occurred while listening for a client message receive event", ex);
            }
        }

        for (Device device : e.getDeviceManager().getDevices().values()){
            if (!e.getDevice().getAddress().equals(device.getAddress())){
                try {
                    if (e.getClientMessage().isJsonOfType(JSONAction.class)) {
                        if (e.getWasActionSuccessful() != null && e.getWasActionSuccessful()) {
                            e.getDevice().sendOutgoingAction((JSONAction) e.getClientMessage().getIncoming(JSONAction.class));
                        }
                    } else if (e.getClientMessage().isJsonOfType(JSONMessage.class)) {
                        e.getDevice().sendOutgoingMessage((JSONMessage) e.getClientMessage().getIncoming(JSONMessage.class));
                    }
                }catch(Exception ex){
                    ServerLogger.error("An error occurred while listening for a client message receive event", ex);
                }
            }
        }
    }
}