/**
 * Copyright (c) 2016-2020, Michael Yang 杨福海 (fuhai999@gmail.com).
 * <p>
 * Licensed under the GNU Lesser General Public License (LGPL) ,Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.gnu.org/licenses/lgpl-3.0.txt
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.jboot.objects.counter.impl;

import io.jboot.Jboot;
import io.jboot.objects.counter.JbootCounter;
import io.jboot.support.redis.JbootRedis;
import io.jboot.utils.StrUtil;

/**
 * @author michael yang (fuhai999@gmail.com)
 * @Date: 2020/3/7
 */
public class JbootRedicCounter implements JbootCounter {

    private JbootRedis redis = Jboot.getRedis();
    private String name;

    public JbootRedicCounter(String name) {
        this.name = name;
    }

    @Override
    public void increment() {
        redis.incr(name);
    }

    @Override
    public void decrement() {
        redis.decr(name);
    }

    @Override
    public Long get() {
        String value = redis.getWithoutSerialize(name);
        return StrUtil.isNotBlank(value) ? null : Long.valueOf(value);
    }

    @Override
    public void set(long newValue) {
        redis.setWithoutSerialize(name, newValue);
    }
}
