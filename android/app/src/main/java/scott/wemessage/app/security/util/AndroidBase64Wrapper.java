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

package scott.wemessage.app.security.util;

import android.util.Base64;

import scott.wemessage.commons.crypto.Base64Wrapper;

public class AndroidBase64Wrapper extends Base64Wrapper {

    private final int BASE64_FLAGS = Base64.NO_WRAP;

    @Override
    public byte[] decodeString(String string) {
        return Base64.decode(string, BASE64_FLAGS);
    }

    @Override
    public String encodeToString(byte[] bytes) {
        return Base64.encodeToString(bytes, BASE64_FLAGS);
    }
}