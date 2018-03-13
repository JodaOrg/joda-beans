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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.util.Iterator;

import org.joda.beans.impl.flexi.FlexiBean;
import org.joda.beans.sample.Address;
import org.joda.beans.sample.ImmEmpty;
import org.joda.beans.sample.ImmTreeNode;
import org.junit.Test;

import com.google.common.collect.ImmutableList;

/**
 * Test BeanIterator.
 */
public class TestBeanIterator {

    @Test
    public void test_iteration_noChildren() {
        ImmEmpty bean = ImmEmpty.builder().build();
        Iterator<Bean> it = JodaBeanUtils.beanIterator(bean);
        assertEquals(it.hasNext(), true);
        assertSame(it.next(), bean);
        assertEquals(it.hasNext(), false);
    }

    @Test
    public void test_iteration_nullChild() {
        Address bean = new Address();
        Iterator<Bean> it = JodaBeanUtils.beanIterator(bean);
        assertEquals(it.hasNext(), true);
        assertSame(it.next(), bean);
        assertEquals(it.hasNext(), false);
    }

    @Test
    public void test_iteration_childWithChildren() {
        ImmTreeNode node1 = ImmTreeNode.builder().name("1").build();
        ImmTreeNode node2 = ImmTreeNode.builder().name("2").build();
        ImmTreeNode root = ImmTreeNode.builder()
            .name("root")
            .child1(node1)
            .child2(node2)
            .build();
        
        Iterator<Bean> it = JodaBeanUtils.beanIterator(root);
        assertEquals(it.hasNext(), true);
        assertSame(it.next(), root);
        assertEquals(it.hasNext(), true);
        assertSame(it.next(), node1);
        assertEquals(it.hasNext(), true);
        assertSame(it.next(), node2);
        assertEquals(it.hasNext(), false);
    }

    @Test
    public void test_iteration_childWithChildrenOfChildren() {
        ImmTreeNode node1 = ImmTreeNode.builder().name("1").build();
        ImmTreeNode node2 = ImmTreeNode.builder().name("2").build();
        ImmTreeNode node3 = ImmTreeNode.builder()
            .name("3")
            .child1(node1)
            .child2(node2)
            .build();
        ImmTreeNode root = ImmTreeNode.builder()
            .name("root")
            .child1(node3)
            .build();
        
        Iterator<Bean> it = JodaBeanUtils.beanIterator(root);
        assertEquals(it.hasNext(), true);
        assertSame(it.next(), root);
        assertEquals(it.hasNext(), true);
        assertSame(it.next(), node3);
        assertEquals(it.hasNext(), true);
        assertSame(it.next(), node1);
        assertEquals(it.hasNext(), true);
        assertSame(it.next(), node2);
        assertEquals(it.hasNext(), false);
    }

    @Test
    public void test_iteration_childWithListOfChildren() {
        ImmTreeNode node1a = ImmTreeNode.builder().name("1a").build();
        ImmTreeNode node1b = ImmTreeNode.builder().name("1b").build();
        ImmTreeNode node1 = ImmTreeNode.builder()
            .name("1")
            .child1(node1a)
            .child2(node1b)
            .build();
        ImmTreeNode node2a = ImmTreeNode.builder().name("2a").build();
        ImmTreeNode node2b = ImmTreeNode.builder().name("2b").build();
        ImmTreeNode node2 = ImmTreeNode.builder()
            .name("2")
            .child1(node2a)
            .child2(node2b)
            .build();
        ImmTreeNode node3 = ImmTreeNode.builder().name("3").build();
        ImmTreeNode root = ImmTreeNode.builder()
            .name("root")
            .child1(node3)
            .childList(ImmutableList.of(node1, node2))
            .build();
        
        Iterator<Bean> it = JodaBeanUtils.beanIterator(root);
        assertEquals(it.hasNext(), true);
        assertSame(it.next(), root);
        assertEquals(it.hasNext(), true);
        assertSame(it.next(), node3);
        assertEquals(it.hasNext(), true);
        assertSame(it.next(), node1);
        assertEquals(it.hasNext(), true);
        assertSame(it.next(), node1a);
        assertEquals(it.hasNext(), true);
        assertSame(it.next(), node1b);
        assertEquals(it.hasNext(), true);
        assertSame(it.next(), node2);
        assertEquals(it.hasNext(), true);
        assertSame(it.next(), node2a);
        assertEquals(it.hasNext(), true);
        assertSame(it.next(), node2b);
        assertEquals(it.hasNext(), false);
    }

    //-----------------------------------------------------------------------
    @Test
    public void test_iteration_childWithNoChildren_FlexiBean() {
        FlexiBean bean1 = new FlexiBean();
        Iterator<Bean> it = JodaBeanUtils.beanIterator(bean1);
        assertEquals(it.hasNext(), true);
        assertSame(it.next(), bean1);
        assertEquals(it.hasNext(), false);
    }

    @Test
    public void test_iteration_childWithOneChild_FlexiBean() {
        FlexiBean bean1 = new FlexiBean();
        FlexiBean bean2 = new FlexiBean();
        bean1.set("a", bean2);
        Iterator<Bean> it = JodaBeanUtils.beanIterator(bean1);
        assertEquals(it.hasNext(), true);
        assertSame(it.next(), bean1);
        assertEquals(it.hasNext(), true);
        assertSame(it.next(), bean2);
        assertEquals(it.hasNext(), false);
    }

}
