package scott.wemessage.commons.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
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

    public static <T> boolean areListsEqual(List<T> listOne, List<T> listTwo, Comparator<? super T> comparator) {
        if (listOne.size() != listTwo.size()) return false;

        List<T> copyOne = new ArrayList<>(listOne);
        List<T> copyTwo = new ArrayList<>(listTwo);

        Collections.sort(copyOne, comparator);
        Collections.sort(copyTwo, comparator);

        Iterator<T> iteratorOne = copyOne.iterator();
        Iterator<T> iteratorTwo = copyTwo.iterator();

        while (iteratorOne.hasNext()) {
            T t1 = iteratorOne.next();
            T t2 = iteratorTwo.next();

            if (comparator.compare(t1, t2) != 0) {
                return false;
            }
        }
        return true;
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