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

/**
 * adheres to specs on http://www.sqlite.org/zipvfs/doc/trunk/www/fileformat.wiki
 */
public class ZipVfsPageInfo {
    long offset;
    long size;
    long unusedBytes;

    public ZipVfsPageInfo(byte[] buffer, int start) {
        if(buffer.length - start < 8)
            throw new IllegalArgumentException("Buffer must be at least 8 bytes long from start.");
        
        offset = ZipVfsHeader.getLongBigEndian(start+0, 5, buffer);              // 40 bits
        size = ZipVfsHeader.getLongBigEndian(start+5, 3, buffer) >> 7;           // in total 17 bits
        unusedBytes = ZipVfsHeader.getLongBigEndian(start+7, 1, buffer) & 0x7F;  // last 7 bits only
    }
    
    public ZipVfsPageInfo(long value) {
        offset = value & 0xFFFFFFFFFF000000l;              // 40 bits
        size = value   & 0x0000000000FFFF80l >> 7;           // in total 17 bits
        unusedBytes = value & 0x7F;  // last 7 bits only
    }

    public long getOffset() {
        return offset;
    }

    public long getSize() {
        return size;
    }

    public long getUnusedBytes() {
        return unusedBytes;
    }

    @Override
    public String toString() {
        return String.format("ZipVfsPageInfo{offset=0x%x [%d], size=%d, unusedBytes=%d}",offset, offset,size, unusedBytes);
    }
}
