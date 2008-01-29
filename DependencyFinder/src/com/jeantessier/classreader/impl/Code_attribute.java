/*
 *  Copyright (c) 2001-2008, Jean Tessier
 *  All rights reserved.
 *  
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions
 *  are met:
 *  
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *  
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *  
 *      * Neither the name of Jean Tessier nor the names of his contributors
 *        may be used to endorse or promote products derived from this software
 *        without specific prior written permission.
 *  
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *  A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR
 *  CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 *  EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 *  PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 *  PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 *  LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 *  NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.jeantessier.classreader.impl;

import java.io.*;
import java.util.*;

import org.apache.log4j.*;

import com.jeantessier.classreader.Instruction;
import com.jeantessier.classreader.*;
import com.jeantessier.text.*;

public class Code_attribute extends Attribute_info implements Iterable<Instruction>, com.jeantessier.classreader.Code_attribute {
    private int maxStack;
    private int maxLocals;
    private byte[] code;
    private Collection<ExceptionHandler> exceptionHandlers = new LinkedList<ExceptionHandler>();
    private Collection<Attribute_info> attributes = new LinkedList<Attribute_info>();

    public Code_attribute(Classfile classfile, Visitable owner, DataInputStream in) throws IOException {
        super(classfile, owner);

        int byteCount = in.readInt();
        Logger.getLogger(getClass()).debug("Attribute length: " + byteCount);

        maxStack = in.readUnsignedShort();
        Logger.getLogger(getClass()).debug("Code max stack: " + maxStack);

        maxLocals = in.readUnsignedShort();
        Logger.getLogger(getClass()).debug("Code max locals: " + maxLocals);

        int codeLength = in.readInt();
        Logger.getLogger(getClass()).debug("Code length: " + codeLength);
        
        code = new byte[codeLength];
        int bytesRead = in.read(code);

        if (Logger.getLogger(getClass()).isDebugEnabled()) {
            Logger.getLogger(getClass()).debug("Read " + bytesRead + " byte(s): " + Hex.toString(code));

            for (Instruction instr : this) {
                int start = instr.getStart();
                int index = instr.getIndex();

                switch (instr.getOpcode()) {
                    case 0x12: // ldc
                    case 0x13: // ldc_w
                    case 0x14: // ldc2_w
                    case 0xb2: // getstatic
                    case 0xb3: // putstatic
                    case 0xb4: // getfield
                    case 0xb5: // putfield
                    case 0xb6: // invokevirtual
                    case 0xb7: // invokespecial
                    case 0xb8: // invokestatic
                    case 0xb9: // invokeinterface
                    case 0xbb: // new
                    case 0xbd: // anewarray
                    case 0xc0: // checkcast
                    case 0xc1: // instanceof
                    case 0xc5: // multianewarray
                        Logger.getLogger(getClass()).debug("    " + start + ": " + instr + " " + index + " (" + instr.getIndexedConstantPoolEntry() + ")");
                        break;
                    default:
                        Logger.getLogger(getClass()).debug("    " + start + ": " + instr + " (" + instr.getLength() + " byte(s))");
                        break;
                }
            }
        }

        int exceptionTableLength = in.readUnsignedShort();
        Logger.getLogger(getClass()).debug("Reading " + exceptionTableLength + " exception handler(s) ...");
        for (int i=0; i<exceptionTableLength; i++) {
            Logger.getLogger(getClass()).debug("Exception handler " + i + ":");
            exceptionHandlers.add(new ExceptionHandler(this, in));
        }

        int attributeCount = in.readUnsignedShort();
        Logger.getLogger(getClass()).debug("Reading " + attributeCount + " code attribute(s)");
        for (int i=0; i<attributeCount; i++) {
            Logger.getLogger(getClass()).debug("code attribute " + i + ":");
            attributes.add(AttributeFactory.create(getClassfile(), this, in));
        }
    }

    public int getMaxStack() {
        return maxStack;
    }

    public int getMaxLocals() {
        return maxLocals;
    }

    public byte[] getCode() {
        return code;
    }

    public Iterator<Instruction> iterator() {
        return new CodeIterator(this, code);
    }

    public Collection<ExceptionHandler> getExceptionHandlers() {
        return exceptionHandlers;
    }

    public Collection<Attribute_info> getAttributes() {
        return attributes;
    }

    public String toString() {
        return "Code";
    }

    public void accept(Visitor visitor) {
        visitor.visitCode_attribute(this);
    }
}