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

package scott.wemessage.commons.types;

import scott.wemessage.commons.utils.StringUtils;

public enum MessageEffect {

    INVISIBLE_INK("invisibleink"),
    GENTLE("gentle"),
    LOUD("loud"),
    CONFETTI("confetti"),
    FIREWORKS("fireworks"),
    SHOOTING_STAR("star"),
    NONE("NONE");

    private String effectName;

    MessageEffect(String effectName){
        this.effectName = effectName;
    }

    public String getEffectName() {
        return effectName;
    }

    public static MessageEffect from(String name){
        if (StringUtils.isEmpty(name)) return NONE;

        for (MessageEffect effect : MessageEffect.values()){
            if (StringUtils.containsIgnoreCase(name, effect.getEffectName())){
                return effect;
            }
        }
        return NONE;
    }
}