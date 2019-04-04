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
package org.joda.beans.ser.bin;

import java.io.IOException;
import java.io.OutputStream;

import org.joda.beans.Bean;
import org.joda.beans.ser.JodaBeanSer;

/**
 * Provides the ability for a Joda-Bean to written to the standard binary format.
 */
class JodaBeanStandardBinWriter extends AbstractBinWriter {
    // this binary design is not the smallest possible
    // however, placing the 'ext' for the additional type info within
    // the bean data is much more friendly for dynamic languages using
    // a standalone MessagePack parser

    // creates an instance
    JodaBeanStandardBinWriter(JodaBeanSer settings, OutputStream output) {
        super(settings, output);
    }

    //-----------------------------------------------------------------------
    // writes the bean
    void write(Bean bean, boolean rootType) throws IOException {
        output.writeArrayHeader(2);
        output.writeInt(1);  // version 1
        writeRootBean(bean, rootType);
    }

}
