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
package org.joda.beans.ser;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.Objects;

/**
 * An optimised byte array output stream.
 * <p>
 * This class holds a number of smaller byte arrays internally.
 * Each array is typically 1024 bytes, but if a large byte array is written
 * the class will hold it as a single large array.
 * <p>
 * Calling {@link #toByteArray()} returns a single combined byte array.
 * Calling {@link #writeTo(OutputStream)} writes the internal arrays without needing to create a combined array.
 * <p>
 * This class is not thread-safe.
 */
public class LinkedByteArrayOutputStream extends OutputStream {

    // segment holding one byte array, the current position in the array, and the next segment when it is full
    private static final class ByteSegment {
        private final byte[] bytes;
        private int pos;
        private ByteSegment next;

        private ByteSegment(byte[] bytes) {
            this.bytes = bytes;
        }
    }

    // the head/root segment
    private final ByteSegment head = new ByteSegment(new byte[1024]);
    // the current tail
    private ByteSegment tail = head;
    // the total number of bytes written
    private int total;

    /**
     * Creates an instance.
     */
    public LinkedByteArrayOutputStream() {
    }

    //-------------------------------------------------------------------------
    /**
     * Writes a single byte to the output stream.
     * 
     * @param val  the value
     */
    @Override
    public void write(int val) {
        var tailRemaining = tail.bytes.length - tail.pos;
        if (tailRemaining == 0) {
            tail.next = new ByteSegment(new byte[1024]);
            tail = tail.next;
        }
        tail.bytes[tail.pos] = (byte) val;
        tail.pos++;
        total++;
    }

    /**
     * Writes all or part of a byte array to the output stream.
     * 
     * @param bytes  the byte array to write, not null
     * @param offset  the offset from the start of the array
     * @param length  the number of bytes to write
     * @throws IndexOutOfBoundsException if the offset or length is invalid
     */
    @Override
    public void write(byte[] bytes, int offset, int length) {
        Objects.checkFromIndexSize(offset, length, bytes.length);
        var tailRemaining = tail.bytes.length - tail.pos;
        // first part
        var firstPartLength = Math.min(tailRemaining, length);
        System.arraycopy(bytes, offset, tail.bytes, tail.pos, firstPartLength);
        tail.pos += firstPartLength;
        // remainder
        var newLength = length - firstPartLength;
        if (newLength > 0) {
            var newOffset = offset + firstPartLength;
            if (newLength >= 1024) {
                tail.next = new ByteSegment(Arrays.copyOfRange(bytes, newOffset, length));
            } else {
                tail.next = new ByteSegment(new byte[1024]);
                System.arraycopy(bytes, newOffset, tail.next.bytes, 0, newLength);
            }
            tail = tail.next;
            tail.pos = newLength;
        }
        total += length;
    }

    /**
     * Writes a byte array to the output stream.
     * 
     * @param bytes  the byte array to write, not null
     */
    @Override
    public void write(byte[] bytes) {
        write(bytes, 0, bytes.length);
    }

    /**
     * Writes all the bytes to the specified output stream.
     * 
     * @param out  the output stream to write to
     * @throws IOException if an IO error occurs
     */
    public void writeTo(OutputStream out) throws IOException {
        for (var segment = head; segment != null; segment = segment.next) {
            out.write(segment.bytes, 0, segment.pos);
        }
    }

    /**
     * Returns a single byte array containing all the bytes written to the output stream.
     * <p>
     * The returned array contains a copy of the internal state of this class.
     * <p>
     * It is not expected that callers will call this method multiple times, although it is safe to do so.
     * 
     * @return the combined byte array
     */
    public byte[] toByteArray() {
        var result = new byte[total];
        var pos = 0;
        for (var segment = head; segment != null; segment = segment.next) {
            System.arraycopy(segment.bytes, 0, result, pos, segment.pos);
            pos += segment.pos;
        }
        return result;
    }

    /**
     * Gets the current number of bytes written.
     * 
     * @return the number of bytes written
     */
    public int size() {
        return total;
    }

    /**
     * A no-op, as this class does not need flushing.
     */
    @Override
    public void flush() {
    }

    /**
     * A no-op, as this class does not need closing.
     */
    @Override
    public void close() {
    }

    /**
     * Returns a hex-formatted string of the bytes that have been written.
     */
    @Override
    public String toString() {
        var hex = HexFormat.of();
        var buf = new StringBuilder(total * 2);
        for (var segment = head; segment != null; segment = segment.next) {
            hex.formatHex(buf, segment.bytes, 0, segment.pos);
        }
        return buf.toString();
    }
}
