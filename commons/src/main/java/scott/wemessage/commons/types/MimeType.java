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

import java.util.Arrays;

public enum MimeType {

    IMAGE(new MimeExtension[]{ MimeExtension.GIF, MimeExtension.JPG, MimeExtension.PNG }),
    AUDIO(new MimeExtension[]{ MimeExtension.AMR, MimeExtension.MIDI, MimeExtension.MP3, MimeExtension.OGG, MimeExtension.WAV }),
    VIDEO(new MimeExtension[]{ MimeExtension.MP4, MimeExtension.WEBM, MimeExtension.GPP_3 }),
    UNDEFINED(new MimeExtension[]{ MimeExtension.UNDEFINED });

    private MimeExtension[] extensions;

    MimeType(MimeExtension[] extensions){
        this.extensions = extensions;
    }

    public MimeExtension[] getSupportedExtensions(){
        return extensions;
    }

    public static MimeType getTypeFromString(String mimeTypeString){
        MimeExtension mimeExtension = MimeExtension.getExtensionFromString(mimeTypeString);

        for (MimeType mimeType : MimeType.values()){
            if (Arrays.asList(mimeType.getSupportedExtensions()).contains(mimeExtension)){
                return mimeType;
            }
        }
        return UNDEFINED;
    }

    public enum MimeExtension {
        GIF("image/gif"),
        JPG("image/jpeg"),
        PNG("image/png"),
        AMR("audio/amr"),
        MIDI("audio/midi"),
        MP3("audio/mpeg"),
        OGG("audio/ogg"),
        WAV("audio/x-wav"),
        MP4("video/mp4"),
        WEBM("video/webm"),
        GPP_3("video/3gpp"),
        MOV("video/quicktime"),
        UNDEFINED("undefined");

        private String mimeTypeString;

        MimeExtension(String mimeTypeString){
            this.mimeTypeString = mimeTypeString;
        }

        public String getTypeString(){
            return mimeTypeString;
        }

        public static MimeExtension getExtensionFromString(String mimeTypeString){
            for (MimeExtension ext : MimeExtension.values()){
                if (ext.getTypeString().equals(mimeTypeString)){
                    return ext;
                }
            }
            return UNDEFINED;
        }
    }
}