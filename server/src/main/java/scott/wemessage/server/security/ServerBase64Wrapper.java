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

package scott.wemessage.server.security;

import org.apache.commons.codec.binary.Base64;

import scott.wemessage.commons.crypto.Base64Wrapper;

public class ServerBase64Wrapper extends Base64Wrapper {

    @Override
    public byte[] decodeString(String string) {
        return Base64.decodeBase64(string);
    }

    @Override
    public String encodeToString(byte[] bytes) {
        return Base64.encodeBase64String(bytes);
    }
}
