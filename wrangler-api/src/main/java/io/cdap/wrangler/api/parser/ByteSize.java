/*
 * Copyright © 2024 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */


package io.cdap.wrangler.api.parser;

import java.util.Locale;

import com.google.gson.JsonElement;

public class ByteSize implements Token{
    private final double value;
    private final String unit;

    public ByteSize(String raw){
        raw = raw.trim().toUpperCase(Locale.ROOT);
        int index = findFirstLetter(raw);
        this.value = Double.parseDouble(raw.substring(0, index));
        this.unit = raw.substring(index);

        if(!unit.matches("KB|MB|GB|TB|PB")){
            throw new IllegalArgumentException("Invalid byte unit: " + unit);
        }
    }

    public long getBytes() {
        switch (unit) {
            case "KB" : return (long) (value * 1024);
            case "MB" : return (long) (value * 1024 * 1024);
            case "GB" : return (long) (value * 1024 * 1024 * 1024);
            case "TB" : return (long) (value * 1024L * 1024 * 1024 * 1024);
            case "PB" : return (long) (value * 1024L * 1024 * 1024 * 1024 * 1024);
            default: throw new RuntimeException("Unknown unit");
        }
    }

    @Override
    public String toString() {
        return value + unit;
    }

    private int findFirstLetter(String input) {
        for (int i = 0; i < input.length(); i++){
            if(!Character.isDigit(input.charAt(i)) && input.charAt(i) != '.'){
                return i;
            }
        }
        throw new IllegalArgumentException("Invalid byte size string: " + input);
    }

    @Override
    public Object value() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'value'");
    }

    @Override
    public TokenType type() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'type'");
    }

    @Override
    public JsonElement toJson() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'toJson'");
    }
}
