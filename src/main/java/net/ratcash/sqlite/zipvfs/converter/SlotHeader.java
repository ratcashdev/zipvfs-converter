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
 *
 */
public class SlotHeader {
    public static final int SLOT_HEADER_SIZE = 0x06;
    
    public long pageNumber = 0;
    public long payloadSize = 0; 
    
    
    public SlotHeader(byte[] buffer, int length) {
        if(length < SLOT_HEADER_SIZE)
            throw new IllegalArgumentException("Buffer must be at a minimum " + SLOT_HEADER_SIZE + " bytes.");
        
        
        pageNumber = ZipVfsHeader.getLongBigEndian(0, 4, buffer) >> 1;        // first 31 bits
        payloadSize = ZipVfsHeader.getLongBigEndian(3, 3, buffer) & 0x1FFFF;  // last 17 bits only
    }
    
}
