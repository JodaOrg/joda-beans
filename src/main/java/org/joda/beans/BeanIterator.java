/*
 *  Copyright 2001-present Stephen Colebourne
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.joda.beans;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.joda.beans.ser.SerIteratorFactory;

/**
 * An iterator over beans.
 */
final class BeanIterator implements Iterator<Bean> {

    /**
     * The stack of beans.
     */
    private final List<Bean> stack = new ArrayList<>(32);

    /**
     * Creates an instance.
     * 
     * @param root  the bean to iterate over
     */
    BeanIterator(Bean root) {
        this.stack.add(root);
    }

    @Override
    public boolean hasNext() {
        return !stack.isEmpty();
    }

    @Override
    public Bean next() {
        if (!hasNext()) {
            throw new NoSuchElementException("No more elements in the iterator");
        }
        // next bean to return is head of the stack
        var currentBean = stack.remove(stack.size() - 1);
        // temp used to reverse the order of child beans to match depth-first order
        // alternative is to insert into stack at a fixed index (lots of array copying)
        var temp = new ArrayDeque<Bean>(32);
        for (var mp : currentBean.metaBean().metaPropertyIterable()) {
            findChildBeans(mp.get(currentBean), mp, currentBean.getClass(), temp);
        }
        stack.addAll(temp);
        return currentBean;
    }

    // find child beans, including those in collections
    private void findChildBeans(Object obj, MetaProperty<?> mp, Class<?> beanClass, Deque<Bean> temp) {
        if (obj != null) {
            if (obj instanceof Bean bean) {
                temp.addFirst(bean);
            } else {
                var it = SerIteratorFactory.INSTANCE.create(obj, mp, beanClass);
                if (it != null) {
                    while (it.hasNext()) {
                        it.next();
                        findChildBeans(it.key(), mp, Object.class, temp);
                        findChildBeans(it.value(), mp, Object.class, temp);
                        findChildBeans(it.column(), mp, Object.class, temp);
                    }
                }
            }
        }
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("BeanIterator does not support remove()");
    }

}
