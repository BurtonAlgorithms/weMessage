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

package scott.wemessage.commons.connection;

import java.io.Serializable;

public class Heartbeat implements Serializable {

    private int type;

    public Heartbeat(Type type){
        this.type = type.getValue();
    }

    public Type from(){
        for (Type t : Type.values()){
            if (t.value == type){
                return t;
            }
        }
        return null;
    }

    public enum Type implements Serializable {
        SERVER(0),
        CLIENT(1);

        private int value;

        Type(int value) {
            this.value = value;
        }

        public int getValue(){
            return value;
        }
    }
}