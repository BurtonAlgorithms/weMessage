package scott.wemessage.server.messages.chat;

import scott.wemessage.server.messages.Handle;

public class PeerChat extends ChatBase {

    private Handle peer;

    public PeerChat(){
        this(null, -1L, null, null, null);
    }

    public PeerChat(String guid, long rowID, String groupID, String chatIdentifier, Handle peer){
        super(guid, rowID, groupID, chatIdentifier);
        this.peer = peer;
    }

    public Handle getPeer() {
        return peer;
    }

    public PeerChat setPeer(Handle peer) {
        this.peer = peer;
        return this;
    }
}