/*
 * Copyright 2017 ratcashdev.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.ratcash.sqlite.zipvfs.converter;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * adheres to specs on http://www.sqlite.org/zipvfs/doc/trunk/www/fileformat.wiki
 */
public class ZipVfsHeader {
    public static final int PAGE_MAP_START = 200;       // 0xC8
    public static final int ZIPVFS_HEADER_START = 100;  // 0x64
    
    long dataStart;
    long dataEnd;
    long dbSize;
    int pageSize;
    int version;
    
    List<ZipVfsPageInfo> pageMap;
    
    public ZipVfsHeader(ByteBuffer buf) {
        if(buf.limit() < PAGE_MAP_START)
            throw new IllegalArgumentException("Buffer must be at a minimum 200 bytes.");
        
        
        byte[] bytes = new byte[8];
        buf.get(bytes, 108, 8);
        dataStart = getLongBigEndian(0, 8, bytes);
        
        buf.get(bytes, 116, 8);
        dataEnd = getLongBigEndian(0, 8, bytes);
        
        buf.get(bytes, 140, 8);
        dbSize = getLongBigEndian(0, 8, bytes);
        
        buf.get(bytes, 172, 4);
        pageSize = (int) getLongBigEndian(0, 4, bytes);
        
        buf.get(bytes, 176, 4);
        version = (int) getLongBigEndian(0, 4, bytes);
    }

    public ZipVfsHeader(byte[] buffer) {
        if(buffer.length < PAGE_MAP_START)
            throw new IllegalArgumentException("Buffer must be at a minimum 200 bytes.");
        
        dataStart = getLongBigEndian(108, 8, buffer);
        dataEnd = getLongBigEndian(116, 8, buffer);
        dbSize = getLongBigEndian(140, 8, buffer);
        pageSize = (int) getLongBigEndian(172, 4, buffer);
        version = (int) getLongBigEndian(176, 4, buffer);
    }
    
    public void initPageMap(byte[] buffer, int start, int len) {
        if(len < dataStart - PAGE_MAP_START)
            throw new IllegalArgumentException("Buffer smaller than " + dataStart + ". PageMap can't be read entirely.");
        
        pageMap = new ArrayList<>((int) (dataStart-PAGE_MAP_START) / 8);
        for(int i = start; i < start + len; i+=8) {
            ZipVfsPageInfo pi = new ZipVfsPageInfo(buffer, i);
//            System.out.println(pi.toString());
            if(pi.offset == 0)
                break;
            pageMap.add(pi);
        }
    }
    
    
    public void initPageMap(ByteBuffer buffer) {
        if(buffer.limit() < dataStart - PAGE_MAP_START)
            throw new IllegalArgumentException("Buffer smaller than " + dataStart + ". PageMap can't be read entirely.");
        
        byte[] bytes = new byte[8];
        
        pageMap = new ArrayList<>((int) (dataStart-PAGE_MAP_START) / 8);
        for(int i = 0; i < buffer.limit(); i+=8) {
            buffer.get(bytes, 0, 8);
            ZipVfsPageInfo pi = new ZipVfsPageInfo(ZipVfsHeader.getLongBigEndian(0, 8, bytes));
//            System.out.println(pi.toString());
            if(pi.offset == 0)
                break;
            pageMap.add(pi);
        }
    }

    
    public static long getLongBigEndian(byte[] buffer, int... x) {
        int len = x.length;
        long result = 0;
        int shifter = (x.length-1) * 8;
        for(int i = 0; i<len; i++) {
            //System.out.println(String.format("Shifting number %x by %d", buffer[x[i]], shifter));
            result += (buffer[x[i]] & 0xFF) << shifter;
            shifter -= 8;
        }
        return result;
    }
    
    public static long getLongBigEndian(byte... values) {
        int len = values.length;
        long result = 0;
        int shifter = (values.length-1) * 8;
        for(int i = 0; i<len; i++) {
            //System.out.println(String.format("Shifting number %x by %d", values[i], shifter));
            result += (values[i] & 0xFF) << shifter;
            shifter -= 8;
        }
        return result;
    }
    
    public static long getLongBigEndian(int from, int len, byte[] buffer) {
        int[] indices = new int[len];
        for(int i = from; i< from+len; i++) 
            indices[i-from] = i;
        
        return getLongBigEndian(buffer, indices);
    }
    
    public ZipVfsPageInfo getZipPageInfo(int pageNumber) {
        return pageMap.get(pageNumber);
    }

    public List<ZipVfsPageInfo> getPageMap() {
        return pageMap;
    }
}
