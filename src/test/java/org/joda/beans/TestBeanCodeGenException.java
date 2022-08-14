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

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;

import org.joda.beans.gen.BeanCodeGenException;
import org.junit.Test;

/**
 * Test exception, message is parsed by plugins.
 */
public class TestBeanCodeGenException {

    private static final File FILE = new File(".");

    @Test
    public void testMessage() {
        BeanCodeGenException test = new BeanCodeGenException("rubbish", FILE, 123);
        assertThat(test.getMessage()).isEqualTo("Error in bean: " + FILE.toString() + ", Line: 123, Message: rubbish");
    }

}
