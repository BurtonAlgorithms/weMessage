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

package scott.wemessage.server.events.connection;

import scott.wemessage.commons.connection.ClientMessage;
import scott.wemessage.server.connection.Device;
import scott.wemessage.server.connection.DeviceManager;
import scott.wemessage.server.events.Event;
import scott.wemessage.server.events.EventManager;

public class ClientMessageReceivedEvent extends Event {

    private DeviceManager deviceManager;
    private Device device;
    private ClientMessage clientMessage;
    private Boolean wasActionSuccessful;

    public ClientMessageReceivedEvent(EventManager eventManager, DeviceManager deviceManager, Device device, ClientMessage clientMessage, Boolean wasActionSuccessful){
        super(eventManager);
        this.deviceManager = deviceManager;
        this.device = device;
        this.clientMessage = clientMessage;
        this.wasActionSuccessful = wasActionSuccessful;
    }

    public DeviceManager getDeviceManager() {
        return deviceManager;
    }

    public Device getDevice() {
        return device;
    }

    public ClientMessage getClientMessage() {
        return clientMessage;
    }

    public Boolean getWasActionSuccessful() {
        return wasActionSuccessful;
    }
}
