/*
 * Copyright (c) 2009 Sun Microsystems, Inc., 4150 Network Circle, Santa
 * Clara, California 95054, U.S.A. All rights reserved.
 *
 * U.S. Government Rights - Commercial software. Government users are
 * subject to the Sun Microsystems, Inc. standard license agreement and
 * applicable provisions of the FAR and its supplements.
 *
 * Use is subject to license terms.
 *
 * This distribution may include materials developed by third parties.
 *
 * Parts of the product may be derived from Berkeley BSD systems,
 * licensed from the University of California. UNIX is a registered
 * trademark in the U.S.  and in other countries, exclusively licensed
 * through X/Open Company, Ltd.
 *
 * Sun, Sun Microsystems, the Sun logo and Java are trademarks or
 * registered trademarks of Sun Microsystems, Inc. in the U.S. and other
 * countries.
 *
 * This product is covered and controlled by U.S. Export Control laws and
 * may be subject to the export or import laws in other
 * countries. Nuclear, missile, chemical biological weapons or nuclear
 * maritime end uses or end users, whether direct or indirect, are
 * strictly prohibited. Export or reexport to countries subject to
 * U.S. embargo or to entities identified on U.S. export exclusion lists,
 * including, but not limited to, the denied persons and specially
 * designated nationals lists is strictly prohibited.
 *
 */
package com.sun.max.vm.prototype;

import java.io.*;

import com.sun.max.elf.*;
import com.sun.max.program.Trace;

/**
 * A program to convert a Maxine VM image into a (sequence of) assembler file(s) or ELF file(s).
 *
 * @author Mick Jordan
 * @author Pradeep Natajaran
 *
 */
public class MemoryBootImage {

    private static final int MEGABYTE = 1024 * 1024;
    private static final int AS_CHUNK_SIZE = MEGABYTE * 100;
    private static final int MAXLL = 16;
    private static final String IMAGE_START = "maxvm_image_start";
    private static final String IMAGE_END = "maxvm_image_end";

    /**
     * @param args
     */
    public static void main(String[] args) {
        try {
            int chunkSize = AS_CHUNK_SIZE;
            String imageFileName = null;
            boolean elf = true;
            // Checkstyle: stop modified control variable check
            for (int i = 0; i < args.length; i++) {
                final String arg = args[i];
                if (arg.equals("-chunksize")) {
                    final String css = args[++i];
                    final char m = css.charAt(css.length() - 1);
                    final int cs = Integer.parseInt(css.substring(0, css.length() - 1));
                    if (m == 'm') {
                        chunkSize = cs * MEGABYTE;
                    } else {
                        throw new Exception("invalid scale character: " + m);
                    }
                } else if (arg.equals("-image")) {
                    imageFileName = args[++i];
                } else if (arg.equals("-asm")) {
                    elf = false;
                }
            }
            // Checkstyle: resume modified control variable check
            File bootImage = null;
            if (imageFileName == null) {
                bootImage = BootImageGenerator.getBootImageFile(BootImageGenerator.getDefaultVMDirectory());
            } else {
                bootImage = new File(imageFileName);
            }
            if (elf) {
                writeELFFile(bootImage, chunkSize);
            } else {
                writeAsmFile(bootImage, chunkSize);
            }

        } catch (Exception ex) {
            System.out.println(ex);
        }
    }

    private static File asFile(File bootImage, int index) {
        final String parent = bootImage.getParent();
        final String name = bootImage.getName();
        return new File(parent, name + "." + index + ".s");
    }

    private static void flushLine(PrintWriter wr, byte[] line, int ll) {
        wr.print(".byte ");
        for (int i = 0; i < ll; i++) {
            final int v = line[i];
            wr.print("0x" + Integer.toHexString(v & 0xFF));
            if (i != ll - 1) {
                wr.print(",");
            }
        }
        wr.println();
    }


    private static void writeELFFile(File bootImage, int chunkSize) {

        int index = 0;
        long offsetCount = 0;
        try {
            final long startTime = System.currentTimeMillis();
            Trace.line(1, "Writing Elf File....");
            final long size = bootImage.length(); // The size of the image file that is stored in a section.
            final ELFHeader imageElfHeader = new ELFHeader();
            imageElfHeader.WriteHeader64(size);
            final ELFSectionHeaderTable imageElfSectionHdr = new ELFSectionHeaderTable(imageElfHeader);
            ELFSymbolTable imageSymTab;
            final BufferedInputStream is = new BufferedInputStream(
                    new FileInputStream(bootImage));

            // Set the string table .
            final ELFStringTable strtab = new ELFStringTable(imageElfHeader);
            strtab.addStringInTable("maxvm_image");
            strtab.addStringInTable(".shstrtab");
            strtab.addStringInTable(".symtab");
            strtab.addStringInTable(".strtab");
            imageElfSectionHdr.setStringTable(strtab);


            // to create a new file to write the ELF object file.
            final String parent = bootImage.getParent();
            final String name = bootImage.getName();
            final File f = new File(parent, name + ".0.o");
            // Open the file for writing.
            final RandomAccessFile fis = new RandomAccessFile(f, "rw");
            final ELFDataOutputStream os = new ELFDataOutputStream(imageElfHeader, fis);
            imageElfHeader.writeELFHeader64ToFile(os, fis);
            writeImageToFile(os, fis, size, is);
            imageElfSectionHdr.write(size);
            final ELFSectionHeaderTable.Entry e = imageElfSectionHdr.entries[imageElfHeader.e_shstrndx];
            imageElfSectionHdr.getStringTable().setSection(e);
            imageElfSectionHdr.getStringTable().write64ToFile(os, fis);

            // Creating Symbol Table and the string table.
            for (int cntr = 0; cntr < imageElfSectionHdr.entries.length; cntr++) {
                final ELFSectionHeaderTable.Entry e1 = imageElfSectionHdr.entries[cntr];
                if (e1.isSymbolTable()) {
                    imageSymTab = new ELFSymbolTable(imageElfHeader, e1);
                    final ELFSectionHeaderTable.Entry strent = imageElfSectionHdr.entries[e1.getLink()];
                    if (strent.isStringTable()) {
                        final ELFStringTable symStrTab = new ELFStringTable(imageElfHeader, strent);
                        // To add the values of the string names that will be used in the symbol table
                        symStrTab.addStringInTable("maxvm_image_start");
                        symStrTab.addStringInTable("maxvm_image_end");
                        imageSymTab.setStringTable(symStrTab);
                        index = imageElfSectionHdr.getStringTable().getIndex("maxvm_image");
                        imageSymTab.setSymbolTableEntries(index, size);
                        //set the size of the string table section.
                        strent.setEntrySize(symStrTab.getStringLength());
                        // Now we need to write the symbol table onto the file.
                        // It should be aligned to 8 bits, so we increment the offsetcount to be aligned to 8 bits and then
                        // seek to that position to write the symbol.
                        offsetCount = fis.getFilePointer();
                        if (offsetCount % 8 != 0) {
                            offsetCount += 8 - (offsetCount % 8);
                        }
                        fis.seek(offsetCount);
                        imageSymTab.write64ToFile(os, fis);
                        // Write the symbol table's string table on to the file
                        imageSymTab.getStringTable().write64ToFile(os, fis);
                    }
                    break;
                }
            }
            // We have written all the sections. We need to write the section headers now.
            //Lets update the size of the file till this point in the section header.
            offsetCount = fis.getFilePointer();
            imageElfSectionHdr.setOffsetCount(offsetCount);


            //Before that we need to retrieve the ELF header and update its e_shoff to give the correct value for the offset of the section Header
            fis.seek(0); // seek to the beginning of the file.

            final ELFHeader header = ELFLoader.readELFHeader(fis);

            // Set the offset Count for the section header in the ELF header.
            header.setShOff(offsetCount);

            fis.seek(0); // Seek to beginning of the file again to write this header.
            header.writeELFHeader64ToFile(os, fis);
            // Now seek to end of the sections to write the section header.

            fis.seek(offsetCount);

            imageElfSectionHdr.writeSectionHeadersToFile64(os, fis);
            fis.close();
            Trace.line(1, "Wrote ELF file in: " + ((System.currentTimeMillis() - startTime) / 1000.0f) + " seconds");

        } catch (Exception ex) {
            System.out.println(ex);
        }

    }

    private static void writeImageToFile(ELFDataOutputStream os, RandomAccessFile fis, long size, BufferedInputStream is) throws IOException {
        final byte[] buffer = new byte[8192];
        while (size > 0) {
            final int n = is.read(buffer, 0, buffer.length);
            fis.write(buffer, 0, n);
            // CheckStyle: stop parameter assignment check
            size -= n;
            // CheckStyle: resume parameter assignment check
        }
    }

    private static void writeAsmFile(File bootImage, int chunkSize) {
        try {
            long size = bootImage.length();
            int fx = 0;
            int chunkCount = 0;
            final BufferedInputStream is = new BufferedInputStream(
                    new FileInputStream(bootImage));

            final long startTime = System.currentTimeMillis();
            while (size > 0) {
                final boolean lastFile = size <= chunkSize;
                final File f = asFile(bootImage, fx);
                final PrintWriter wr = new PrintWriter(new BufferedWriter(new FileWriter(f)));
                System.out.println(" writing  " + f);
                wr.println(".section maxvm_image, \"a\"");
                if (fx == 0) {
                    wr.println(".global " + IMAGE_START);
                    wr.println(IMAGE_START + ":");
                }
                if (lastFile) {
                    wr.println(".global " + IMAGE_END);
                }
                int count = 0;
                final byte[] line = new byte[MAXLL];
                while (chunkCount < chunkSize) {
                    final int b = is.read();
                    if (b < 0) {
                        break;
                    }
                    line[count++] = (byte) b; chunkCount++;
                    if (count == MAXLL) {
                        flushLine(wr, line, MAXLL);
                        count = 0;
                    }
                }
                if (count > 0) {
                    flushLine(wr, line, count);
                }
                if (lastFile) {
                    wr.println(IMAGE_END + ":");
                }
                wr.close();
                size -= chunkSize;
                chunkCount = 0;
                fx++;
            }
            is.close();
            Trace.line(1, "Wrote ASM file in: " + ((System.currentTimeMillis() - startTime) / 1000.0f) + " seconds");
        } catch (Exception ex) {
            System.out.println(ex);
        }
    }
}
