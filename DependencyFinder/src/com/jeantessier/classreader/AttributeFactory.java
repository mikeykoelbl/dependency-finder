/*
 *  Copyright (c) 2001-2004, Jean Tessier
 *  All rights reserved.
 *  
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions
 *  are met:
 *  
 *  	* Redistributions of source code must retain the above copyright
 *  	  notice, this list of conditions and the following disclaimer.
 *  
 *  	* Redistributions in binary form must reproduce the above copyright
 *  	  notice, this list of conditions and the following disclaimer in the
 *  	  documentation and/or other materials provided with the distribution.
 *  
 *  	* Neither the name of Jean Tessier nor the names of his contributors
 *  	  may be used to endorse or promote products derived from this software
 *  	  without specific prior written permission.
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

package com.jeantessier.classreader;

import java.io.*;

import org.apache.log4j.*;

public class AttributeFactory {
	private static final String CONSTANT_VALUE       = "ConstantValue";
	private static final String CODE                 = "Code";
	private static final String EXCEPTIONS           = "Exceptions";
	private static final String INNER_CLASSES        = "InnerClasses";
	private static final String SYNTHETIC            = "Synthetic";
	private static final String SOURCE_FILE          = "SourceFile";
	private static final String LINE_NUMBER_TABLE    = "LineNumberTable";
	private static final String LOCAL_VARIABLE_TABLE = "LocalVariableTable";
	private static final String DEPRECATED           = "Deprecated";

	public static Attribute_info create(Classfile classfile, Visitable owner, DataInputStream in) throws IOException {
		Attribute_info result = null;

		int nameIndex = in.readUnsignedShort();
		if (nameIndex > 0) {
			Object entry = classfile.getConstantPool().get(nameIndex);

			if (entry instanceof UTF8_info) {
				String name = ((UTF8_info) entry).getValue();
				Logger.getLogger(AttributeFactory.class).debug("Attribute name index: " + nameIndex + " (" + name + ")");
				
				if (CONSTANT_VALUE.equals(name)) {
					result = new ConstantValue_attribute(classfile, owner, in);
				} else if (CODE.equals(name)) {
					result = new Code_attribute(classfile, owner, in);
				} else if (EXCEPTIONS.equals(name)) {
					result = new Exceptions_attribute(classfile, owner, in);
				} else if (INNER_CLASSES.equals(name)) {
					result = new InnerClasses_attribute(classfile, owner, in);
				} else if (SYNTHETIC.equals(name)) {
					result = new Synthetic_attribute(classfile, owner, in);
				} else if (SOURCE_FILE.equals(name)) {
					result = new SourceFile_attribute(classfile, owner, in);
				} else if (LINE_NUMBER_TABLE.equals(name)) {
					result = new LineNumberTable_attribute(classfile, owner, in);
				} else if (LOCAL_VARIABLE_TABLE.equals(name)) {
					result = new LocalVariableTable_attribute(classfile, owner, in);
				} else if (DEPRECATED.equals(name)) {
					result = new Deprecated_attribute(classfile, owner, in);
				} else {
					Logger.getLogger(AttributeFactory.class).warn("Unknown attribute name \"" + name + "\"");
					result = new Custom_attribute(name, classfile, owner, in);
				}
			} else {
				Logger.getLogger(AttributeFactory.class).debug("Attribute name: " + entry);

				Logger.getLogger(AttributeFactory.class).warn("Unknown attribute with invalid name");
				result = new Custom_attribute(classfile, owner, in);
			}
		} else {
			Logger.getLogger(AttributeFactory.class).debug("Attribute name index: " + nameIndex);

			Logger.getLogger(AttributeFactory.class).warn("Unknown attribute with no name");
			result = new Custom_attribute(classfile, owner, in);
		}

		return result;
	}
}
