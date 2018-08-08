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

import scott.wemessage.commons.types.DisconnectReason;
import scott.wemessage.server.connection.Device;
import scott.wemessage.server.connection.DeviceManager;
import scott.wemessage.server.events.Event;
import scott.wemessage.server.events.EventManager;

public class DeviceQuitEvent extends Event {

    private DeviceManager deviceManager;
    private Device device;
    private DisconnectReason disconnectReason;

    public DeviceQuitEvent(EventManager eventManager, DeviceManager deviceManager, Device device, DisconnectReason disconnectReason){
        super(eventManager);
        this.deviceManager = deviceManager;
        this.device = device;
        this.disconnectReason = disconnectReason;
    }

    public DeviceManager getDeviceManager() {
        return deviceManager;
    }

    public Device getDevice() {
        return device;
    }

    public DisconnectReason getDisconnectReason() {
        return disconnectReason;
    }
}