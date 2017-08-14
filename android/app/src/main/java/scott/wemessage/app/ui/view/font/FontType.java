package scott.wemessage.app.ui.view.font;

public enum FontType {

    ORKNEY_LIGHT("orkney_light.ttf", "OrkneyLight"),
    ORKNEY_MEDIUM("orkney_medium.ttf", "OrkneyMedium"),
    ORKNEY_BOLD("orkney_bold.ttf", "OrkneyBold");

    private String fontFile;
    private String fontName;

    FontType(String fontFile, String fontName){
        this.fontFile = fontFile;
        this.fontName = fontName;
    }

    public String getFontFile() {
        return fontFile;
    }

    public String getFontName() {
        return fontName;
    }

    public static FontType getTypeFromName(String fontName){
        for (FontType fontType : FontType.values()){
            if (fontType.getFontName().equalsIgnoreCase(fontName)){
                return fontType;
            }
        }
        return null;
    }

    public static FontType getTypeFromFileName(String fileName){
        for (FontType fontType : FontType.values()){
            if (fontType.getFontFile().equalsIgnoreCase(fileName)){
                return fontType;
            }
        }
        return null;
    }
}