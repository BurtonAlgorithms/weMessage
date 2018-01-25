package scott.wemessage.commons.utils;

import java.util.ArrayList;
import java.util.List;

public class ListUtils {

    public static List<ObjectContainer> findDifference(List<? extends Object> original, List<? extends Object> newList){
        List<ObjectContainer> differences = new ArrayList<>();

        for (Object o : original){
            if (!newList.contains(o)){
                differences.add(new ObjectContainer(o, ListStatus.REMOVED));
            }
        }

        for (Object o : newList){
            if (!original.contains(o)) {
                differences.add(new ObjectContainer(o, ListStatus.ADDED));
            }
        }

        return differences;
    }

    public static class ObjectContainer {
        private Object object;
        private ListStatus status;

        public ObjectContainer(Object object, ListStatus status) {
            this.object = object;
            this.status = status;
        }

        public Object getObject() {
            return object;
        }

        public void setObject(Object object) {
            this.object = object;
        }

        public ListStatus getStatus() {
            return status;
        }

        public void setStatus(ListStatus status) {
            this.status = status;
        }
    }

    public enum ListStatus {
        ADDED,
        REMOVED
    }
}