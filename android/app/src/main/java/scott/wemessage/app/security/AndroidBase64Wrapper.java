package scott.wemessage.app.security;

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
