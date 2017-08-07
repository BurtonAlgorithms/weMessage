package scott.wemessage.commons.types;

import java.util.Arrays;

public enum MimeType {

    IMAGE(new MimeExtension[]{ MimeExtension.GIF, MimeExtension.JPG, MimeExtension.PNG }),
    AUDIO(new MimeExtension[]{ MimeExtension.AMR, MimeExtension.MIDI, MimeExtension.MP3, MimeExtension.OGG, MimeExtension.WAV }),
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