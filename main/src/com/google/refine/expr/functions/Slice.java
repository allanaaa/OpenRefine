/*

Copyright 2010, Google Inc.
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are
met:

    * Redistributions of source code must retain the above copyright
notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above
copyright notice, this list of conditions and the following disclaimer
in the documentation and/or other materials provided with the
distribution.
    * Neither the name of Google Inc. nor the names of its
contributors may be used to endorse or promote products derived from
this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,           
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY           
THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

*/

package com.google.refine.expr.functions;

import java.util.List;
import java.util.Properties;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.refine.expr.ExpressionUtils;
import com.google.refine.expr.HasFieldsList;
import com.google.refine.grel.Function;

public class Slice implements Function {

    @Override
    public Object call(Properties bindings, Object[] args) {
        if (args.length > 1 && args.length <= 3) {
            Object v = args[0];
            Object from = args[1];
            Object to = (args.length == 3) ? args[2] : null;
            
            if (v != null && from != null && from instanceof Number && (to == null || to instanceof Number)) {
                if (v.getClass().isArray() || v instanceof List<?> || v instanceof HasFieldsList || v instanceof ArrayNode) {
                    int length = 0;
                    if (v.getClass().isArray()) { 
                        length = ((Object[]) v).length;
                    } else if (v instanceof HasFieldsList) {
                        length = ((HasFieldsList) v).length();
                    } else if (v instanceof ArrayNode) {
                        length = ((ArrayNode) v).size();
                    } else {
                        length = ExpressionUtils.toObjectList(v).size();
                    }
                    
                    int start = ((Number) from).intValue();
                    int end = (to != null) ? ((Number) to).intValue() : length;
                                
                    if (start < 0) {
                        start = length + start;
                    }
                    start = Math.min(length, Math.max(0, start));
                    
                    if (end < 0) {
                        end = length + end;
                    }
                    end = Math.min(length, Math.max(start, end));
                    
                    if (v.getClass().isArray()) {
                        Object[] a2 = new Object[end - start];
                        
                        System.arraycopy(v, start, a2, 0, end - start);
                        
                        return a2;
                    } else if (v instanceof HasFieldsList) {
                        return ((HasFieldsList) v).getSubList(start, end);
                    } else if (v instanceof ArrayNode) {
                        ArrayNode a = (ArrayNode) v;
                        Object[] a2 = new Object[end - start];
                        
                        for (int i = 0; i < a2.length; i++) {
                            a2[i] = a.get(start + i);
                        }
                        
                        return a2;
                    } else {
                        return ExpressionUtils.toObjectList(v).subList(start, end);
                    }
                } else {
                    String s = (v instanceof String) ? (String) v : v.toString();
                    
                    int start = ((Number) from).intValue();
                    if (start < 0) {
                        start = s.length() + start;
                    }
                    start = Math.min(s.length(), Math.max(0, start));
                    
                    if (to != null) {
                        int end = ((Number) to).intValue();
                        if (end < 0) {
                            end = s.length() + end;
                        }
                        end = Math.min(s.length(), Math.max(start, end));
                        
                        return s.substring(start, end);
                    } else {
                        return s.substring(start);
                    }
                }
            }
        }
        return null;
    }

    @Override
    public String getDescription() {
        return 
            "Returns the substring of o starting from character or array index from, and up to (excluding) character or array index to. If the to argument is omitted, slice will output to the end of s. For example, "profound".slice(3) returns the string “found”, and "profound".slice(2, 4) returns the string “of”. Remember that character indices start from zero. A negative character index counts from the end of the string. For example, "profound".slice(0, -1) returns the string “profoun”." +
            "If o is an array, returns o[from, to]. "
        ;
    }
    
    @Override
    public String getParams() {
        return "string or array o, number from, number to (optional)";
    }
    
    @Override
    public String getReturns() {
        return "string, array, or array item (number, string, etc.)";
    }
}
