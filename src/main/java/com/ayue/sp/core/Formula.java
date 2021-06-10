package com.ayue.sp.core;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 2020年8月21日
 *
 * @author ayue
 */
public class Formula {
        public static <T> boolean isEmptyCollection(Collection<T> collection) {
                if (collection == null || collection.isEmpty()){
                        return true;}
                return false;
        }

        public static <T, Q> boolean isEmptyMap(Map<T, Q> map) {
                if (map == null || map.isEmpty()){
                        return true;}
                return false;
        }

        public static boolean isEmptyString(String string) {
                if (string == null || string == ""){
                        return true;}
                return false;
        }

        /**
         * 简便方法用于列表转哈希表
         */
        public static <A, B> Map<A, B> list2map(Collection<B> list, Function<B, A> mapper) {
                if (isEmptyCollection(list))
                        return Collections.emptyMap();
                Map<A, B> result = new HashMap<>(list.size());
                for (B item : list) {
                        result.put(mapper.apply(item), item);
                }
                return result;
        }

        /**
         * 简便方法用于获取列表成员属性的列表
         */
        public static <A, B> List<A> listDistinct(Collection<B> collections, Function<B, A> mapper) {
                if (isEmptyCollection(collections))
                        return Collections.emptyList();
                return collections.stream().map(mapper).distinct().collect(Collectors.toList());
        }

        /**
         * 简便方法用于列表转含列表的哈希表
         */
        public static <A, B> Map<A, List<B>> listGrouping(Collection<B> collections, Function<B, A> mapper) {
                if (isEmptyCollection(collections))
                        return Collections.emptyMap();
                return collections.stream().collect(Collectors.groupingBy(mapper));
        }

}
