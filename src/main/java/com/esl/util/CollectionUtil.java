package com.esl.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class CollectionUtil {
    private static Logger log = LoggerFactory.getLogger(CollectionUtil.class);

    public static <T> List<T> concatLists(int size, T base, List<T> lowerList, List<T> higherList)
    {
        int minSize = (int) Math.floor(size / 2);
        List<T> subResults = new ArrayList<>();

        lowerList.remove(base);
        higherList.remove(base);

        int lowerSize = lowerList.size();
        int higherSize = higherList.size();

        log.info("concat size {}, lower list size {}, higher list size {}", size, lowerSize, higherSize);

        // do not have enough results for both list
        if (lowerSize < minSize && higherSize < minSize) {
            log.info("both lists are less than {}", minSize);
            subResults.addAll(lowerList);
            subResults.add(base);
            subResults.addAll(higherList);
        }
        // lower is less than minreq
        else if (lowerSize < minSize) {
            log.info("lower list is less than {}", minSize);
            subResults.addAll(lowerList);

            int index = size - subResults.size() - 1;
            if (index > higherSize) index = higherSize;
            log.info("get highList index {}", index);
            subResults.add(base);
            subResults.addAll(higherList.subList(0, index));
        }
        // higher is less than minreq
        else if (higherSize < minSize) {
            log.info("higher list is less than {}", minSize);
            int index = size - subResults.size() - 1;
            if (index > lowerSize) index = lowerSize;
            log.info("get lowerList index {}", index);
            subResults.addAll(lowerList.subList(0, index));
            subResults.add(base);
            subResults.addAll(higherList);
        }
        // Normal cases
        else {
            log.info("both list >= {}", minSize);
            subResults.addAll(lowerList.subList(0, minSize));
            subResults.add(base);
            subResults.addAll(higherList.subList(0, minSize));
        }

        return subResults;
    }
}
