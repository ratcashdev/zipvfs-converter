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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.util.logging.Level;
import java.util.logging.Logger;
//import net.sf.jazzlib.DataFormatException;
//import net.sf.jazzlib.Inflater;
//import net.sf.jazzlib.InflaterInputStream;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

public class NdsDecompressor {

    public static void main(String[] args) {
        try {
            new NdsDecompressor().decodeNDS(args[0]);
        } catch (IOException | DataFormatException ex) {
            Logger.getLogger(NdsDecompressor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void decodeNDS(String filepath) throws FileNotFoundException, IOException, DataFormatException {
        File dbFile = new File(filepath);
        String convertedFile = dbFile.getAbsolutePath() + ".sqlite";
        FileOutputStream fos = new FileOutputStream(new File(convertedFile));

        
        int bytesRead = 0;
        
        try (FileChannel fc = FileChannel.open(dbFile.toPath(), StandardOpenOption.READ)) {
            
            // reading the header
            ByteBuffer headerBuffer = ByteBuffer.allocate(200);
            bytesRead = fc.read(headerBuffer);
            headerBuffer.flip();
            if(headerBuffer.limit()!= 200 && bytesRead != 200)
                 throw new IllegalArgumentException("malformed file...");
            
            ZipVfsHeader zipVfsHeader = new ZipVfsHeader(headerBuffer.array());
//            System.out.println("Allocating buffer for the pageMaps. Need " + (zipVfsHeader.dataStart - 200) + " bytes");
            // reading the page Maps
            ByteBuffer pageMapBuffer = ByteBuffer.allocate((int) zipVfsHeader.dataStart - ZipVfsHeader.PAGE_MAP_START);
            bytesRead = fc.read(pageMapBuffer);
            pageMapBuffer.flip();
            zipVfsHeader.initPageMap(pageMapBuffer.array(), 0, pageMapBuffer.capacity());
            
            
            ByteBuffer pageBuffer = ByteBuffer.allocate(64*1024);
            Inflater inflater = new Inflater(false);
            byte[] outBytes = new byte[64*1024];
            
            // read and compress the pages
            for (ZipVfsPageInfo pageInfo : zipVfsHeader.getPageMap()) {
                // read the data area
                bytesRead = fc.read(pageBuffer, pageInfo.offset);
                pageBuffer.flip();
                
                // make sure we read the correct number of bytes
                assert(bytesRead >= pageInfo.size);
                assert(pageBuffer.limit() >= pageInfo.size);
                pageBuffer.limit((int) pageInfo.size);
                
                // make sure the buffer contains a ZLIB stream, i.e. contains the magic header
                SlotHeader sh = new SlotHeader(pageBuffer.array(), pageBuffer.limit());
//                System.out.println("sh.pageNumber = " + sh.pageNumber);
//                System.out.println("sh.payloadSize = " + sh.payloadSize);
                int magicOffset = getZLibMagicOffset(pageBuffer.array(), pageBuffer.limit());
                if(magicOffset != SlotHeader.SLOT_HEADER_SIZE) {
                    // this may be a bTree Freelist slot or an encrypted ZipVFS slot
                    System.out.println("Skipping offset " + pageInfo.offset + ". Readable ZLIB header was not identified.");
                    continue;
                }
                
                inflater.setInput(pageBuffer.array(), SlotHeader.SLOT_HEADER_SIZE, pageBuffer.limit());
                int bytesWritten;
                while ((bytesWritten = inflater.inflate(outBytes)) > 0) {
                    fos.write(outBytes, 0, bytesWritten);
                }
                
                // since we read a whole chunk, the compressor must be finished
                assert(inflater.finished() == true);
                inflater.reset();
                pageBuffer.clear();
            }
            System.out.println("Conversion done.\nOpen '" + convertedFile + "' in your faviroute Sqlite Front-End.");
        } 
    }
    
    public int getZLibMagicOffset(byte[] buffer, int len) {
        int offset;
        for (offset = SlotHeader.SLOT_HEADER_SIZE; offset < len; offset++) {
            if ((buffer[offset] & 0xFF) == 0x78 && (offset < len - 1)
                    && (((buffer[offset + 1] & 0xFF) == 0x01) // no compression
                    || ((buffer[offset + 1] & 0xFF) == 0x9C) // default
                    || ((buffer[offset + 1] & 0xFF) == 0xDA))) {  // high compression
                return offset;
            }
        }
        return -1;
    }
}
