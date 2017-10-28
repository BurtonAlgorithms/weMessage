package scott.wemessage.app.security;

import scott.wemessage.commons.types.FailReason;

public class FailedCryptoFile extends CryptoFile {

    private FailReason failReason;

    public FailedCryptoFile(FailReason failReason) {
        super(null, null, null);
        this.failReason = failReason;
    }

    public FailReason getFailReason(){
        return failReason;
    }
}