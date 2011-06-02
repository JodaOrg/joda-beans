/*
 *  Copyright 2001-2011 Stephen Colebourne
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

import java.util.HashMap;

/**
 * Checks for hash code clashes that might affect the code generation.
 * 
 * @author Stephen Colebourne
 */
@BeanDefinition
public class CheckHashCode {
    
    static String letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789_";
    static int letterCount = letters.length();

    public static void main(String[] args) {
        size1();
        size2();
        size3();
        size4();
    }

    private static void size1() {
        int size = 1;
        HashMap<String, Integer> map = new HashMap<String, Integer>(1024 * 1024);
        for (int i = size; i < size + 1; i++) {
            StringBuilder buf = new StringBuilder("                    ");
            buf.setLength(size);
            for (int a = 0; a < letterCount; a++) {
                buf.setCharAt(0, letters.charAt(a));
                String str = buf.toString();
                int hash = str.hashCode();
                Integer old = map.put(str, hash);
                if (old != null) {
                    System.out.println(old + " " + hash);
                }
            }
        }
        System.out.println("Map: " + map.size() + " Expected: " + (int) Math.pow(letterCount, size));
        System.out.println("!!DONE!!");
    }

    private static void size2() {
        int size = 2;
        HashMap<String, Integer> map = new HashMap<String, Integer>(1024 * 1024);
        for (int i = size; i < size + 1; i++) {
            StringBuilder buf = new StringBuilder("                    ");
            buf.setLength(size);
            for (int a = 0; a < letterCount; a++) {
                for (int b = 0; b < letterCount; b++) {
                    buf.setCharAt(0, letters.charAt(a));
                    buf.setCharAt(1, letters.charAt(b));
                    String str = buf.toString();
                    int hash = str.hashCode();
                    Integer old = map.put(str, hash);
                    if (old != null) {
                        System.out.println(old + " " + hash);
                    }
                }
            }
        }
        System.out.println("Map: " + map.size() + " Expected: " + (int) Math.pow(letterCount, size));
        System.out.println("!!DONE!!");
    }

    private static void size3() {
        int size = 3;
        HashMap<String, Integer> map = new HashMap<String, Integer>(1024 * 1024);
        for (int i = size; i < size + 1; i++) {
            StringBuilder buf = new StringBuilder("                    ");
            buf.setLength(size);
            for (int a = 0; a < letterCount; a++) {
                for (int b = 0; b < letterCount; b++) {
                    for (int c = 0; c < letterCount; c++) {
                        buf.setCharAt(0, letters.charAt(a));
                        buf.setCharAt(1, letters.charAt(b));
                        buf.setCharAt(2, letters.charAt(c));
                        String str = buf.toString();
                        int hash = str.hashCode();
                        Integer old = map.put(str, hash);
                        if (old != null) {
                            System.out.println(old + " " + hash);
                        }
                    }
                }
            }
        }
        System.out.println("Map: " + map.size() + " Expected: " + (int) Math.pow(letterCount, size));
        System.out.println("!!DONE!!");
    }

    private static void size4() {
        int size = 4;
        String[] array = new String[Integer.MAX_VALUE / 16];
        for (int i = size; i < size + 1; i++) {
            StringBuilder buf = new StringBuilder("                    ");
            buf.setLength(size);
            for (int a = 0; a < letterCount; a++) {
                for (int b = 0; b < letterCount; b++) {
                    for (int c = 0; c < letterCount; c++) {
                        for (int d = 0; d < letterCount; d++) {
                            buf.setCharAt(0, letters.charAt(a));
                            buf.setCharAt(1, letters.charAt(b));
                            buf.setCharAt(2, letters.charAt(c));
                            buf.setCharAt(3, letters.charAt(d));
                            String str = buf.toString();
                            int hash = str.hashCode();
                            if (hash >= 0 && hash < Integer.MAX_VALUE / 16) {
                                if (array[hash] != null) {
                                    System.out.println(array[hash]+ " " + array[hash].hashCode() + " " + str + " " + str.hashCode() + " " + hash);
                                }
                                array[hash] = str;
                            }
                        }
                    }
                }
            }
        }
        System.out.println("Expected: " + (int) Math.pow(letterCount, size));
        System.out.println("!!DONE!!");
    }

}
