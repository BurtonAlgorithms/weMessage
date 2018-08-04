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