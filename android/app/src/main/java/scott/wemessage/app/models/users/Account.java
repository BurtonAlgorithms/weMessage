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

package scott.wemessage.app.models.users;

import java.util.UUID;

import scott.wemessage.app.weMessage;

public class Account {

    private UUID uuid;
    private String email;
    private String encryptedPassword;

    public Account(){

    }

    public Account(UUID uuid, String email, String encryptedPassword){
        this.uuid = uuid;
        this.email = email;
        this.encryptedPassword = encryptedPassword;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getEmail() {
        return email;
    }

    public String getEncryptedPassword() {
        return encryptedPassword;
    }

    public Handle getHandle(){
        return weMessage.get().getMessageDatabase().getHandleByHandleID(getEmail());
    }

    public Account setUuid(UUID uuid) {
        this.uuid = uuid;
        return this;
    }

    public Account setEmail(String email) {
        this.email = email;
        return this;
    }

    public Account setEncryptedPassword(String encryptedPassword) {
        this.encryptedPassword = encryptedPassword;
        return this;
    }
}