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