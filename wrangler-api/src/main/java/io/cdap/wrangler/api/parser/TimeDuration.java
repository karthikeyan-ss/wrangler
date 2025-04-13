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

import com.google.gson.JsonElement;

import java.util.Locale;

/**
 * Represents a time duration like 150ms, 2min, or 3seconds.
 */

public class TimeDuration implements Token {
    private final double value;
    private final String unit;

    public TimeDuration(String raw) {
        raw = raw.trim().toLowerCase(Locale.ROOT);
        int index = findFirstLetter(raw);
        this.value = Double.parseDouble(raw.substring(0, index));
        this.unit = raw.substring(index);

        if (!unit.matches("ms|s|sec|seconds|m|min|minutes")) {
            throw new IllegalArgumentException("Invalid time unit: " + unit);
        }
    }

    public long getMilliseconds() {
        switch (unit) {
            case "ms": return (long) value;
            case "s":
            case "sec":
            case "seconds": return (long) (value * 1000);
            case "m":
            case "min":
            case "minutes": return (long) (value * 60_000);
            default: throw new RuntimeException("Unknown unit");
        }
    }

    @Override
    public String toString() {
        return value + unit;
    }

    private int findFirstLetter(String input) {
        for (int i = 0; i < input.length(); i++) {
           if (!Character.isDigit(input.charAt(i)) && input.charAt(i) != '.') {
            return i;
           }
        }

        throw new IllegalArgumentException("Invalid time duration string :" + input);
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
