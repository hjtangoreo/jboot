/**
 * Copyright (c) 2015-2022, Michael Yang 杨福海 (fuhai999@gmail.com).
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.jboot.db.model;


import java.util.HashSet;
import java.util.Set;

public class JbootModelHintManager {

    private static final ThreadLocal<Set<Class>> ID_CACHE_HOLDER = new ThreadLocal<>();
    private static JbootModelHintManager me = new JbootModelHintManager();

    public static JbootModelHintManager me() {
        return me;
    }

    public void closeIdCache(Class clazz) {
        Set<Class> flags = ID_CACHE_HOLDER.get();
        if (flags == null) {
            flags = new HashSet<>();
        }
        flags.add(clazz);
        ID_CACHE_HOLDER.set(flags);
    }


    public boolean isClosedIdCache(Class clazz) {
        Set result = ID_CACHE_HOLDER.get();
        return result != null && result.contains(clazz);
    }


    public void clearIdCacheFlag() {
        ID_CACHE_HOLDER.remove();
    }

}
