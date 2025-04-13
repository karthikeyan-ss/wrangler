/*
 * Copyright © 2024 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */

package io.cdap.directives.aggregates;

import io.cdap.wrangler.api.*;
import io.cdap.wrangler.api.parser.*;
import io.cdap.wrangler.api.annotation.DirectiveClass;
import io.cdap.wrangler.api.Directive.AggregateDirective;
import io.cdap.wrangler.api.Row;
import io.cdap.wrangler.api.ExecutorContext;
import io.cdap.wrangler.api.TransientStore;
import io.cdap.wrangler.api.parser.ColumnName;
import io.cdap.wrangler.api.parser.Text;
import io.cdap.wrangler.api.parser.ByteSize;
import io.cdap.wrangler.api.parser.TimeDuration;
import io.cdap.wrangler.api.TokenGroup;
import io.cdap.wrangler.api.parser.UsageDefinition;
import io.cdap.wrangler.api.DirectiveParseException;
import io.cdap.wrangler.api.DirectiveExecutionException;



import java.util.*;


/**
 * Directive to compute total/average byte size and duration across records.
 */
@DirectiveClass(
  name = "aggregate-stats",
  usage = "aggregate-stats <size_col> <time_col> <output_size_col> <output_time_col> [<size_unit>] [<time_unit>] [<agg_type>]",
  description = "Aggregates byte sizes and durations with optional output units and aggregation type"
)

public class AggregateStats implements AggregateDirective {
    
    private String sourceSizeCol;
    private String sourceTimeCol;
    private String outputSizeCol;
    private String outputTimeCol;
    private String sizeUnit = "bytes";
    private String timeUnit = "ms";
    private String aggType = "total";

    private TransientStore store;

   @Override
   public UsageDefinition define() {
        return UsageDefinition.builder("aggregate-stats")
            .addRequiredArg("size_col", ColumnName.class)
            .addRequiredArg("time_col", ColumnName.class)
            .addRequiredArg("output_size_col", ColumnName.class)
            .addRequiredArg("output_time_col", ColumnName.class)
            .addOptionalArg("size_unit", Text.class)
            .addOptionalArg("time_unit", Text.class)
            .addOptionalArg("agg_type", Text.class)
            .build();
   }

   @Override
   public void initialize(TokenGroup args, ExecutorContext context) throws DirectiveParseException {
        sourceSizeCol = ((ColumnName) args.get(0).value()).value();
        sourceTimeCol = ((ColumnName) args.get(1).value()).value();
        outputSizeCol = ((ColumnName) args.get(2).value()).value();
        outputTimeCol = ((ColumnName) args.get(3).value()).value();

        if (args.size() > 4) sizeUnit = ((Text) args.get(4).value()).value();
        if (args.size() > 5) timeUnit = ((Text) args.get(5).value()).value();
        if (args.size() > 6) aggType = ((Text) args.get(6).value()).value();

        store =  (TransientStore) context.getTransientStore();
        store.put("totalSize", 0L);
        store.put("totalTime", 0L);
        store.put("count", 0L);
   }

   @Override
   public List<Row> execute(Row row, ExecutorContext context) throws DirectiveExecutionException {
        Object sizeObj = row.getValue(sourceSizeCol);
        Object timeObj = row.getValue(sourceTimeCol);

        if (!(sizeObj instanceof ByteSize) || !(timeObj instanceof TimeDuration)) {
            throw new DirectiveExecutionException("Expected ByteSize and TimeDuration types");
        }

        long size = ((ByteSize) sizeObj).getBytes();
        long time = ((TimeDuration) timeObj).getMilliseconds();

        store.put("totalSize", store.getAsLong("totalSize") + size);
        store.put("totalTime", store.getAsLong("totalTime") + time);
        store.put("count", store.getAsLong("count") + 1);

        return Collections.emptyList(); // aggregation happens in finalize()
   }

   @Override
   public List<Row> finalize(ExecutorContext context) throws DirectiveExecutionException {
        long totalSize = store.getAsLong("totalSize");
        long totalTime = store.getAsLong("totalTime");
        long count = store.getAsLong("count");

        if("average".equalsIgnoreCase(aggType)) {
            totalSize = (count > 0) ? totalSize / count : 0;
            totalTime = (count > 0) ? totalTime / count : 0;
        }

        // Convert units if needed
        totalSize = convertSize(totalSize, sizeUnit);
        totalTime = convertTime(totalTime, timeUnit);

        Row result = new Row();
        result.add(outputSizeCol, totalSize);
        result.add(outputTimeCol, totalTime);

        return Collections.singletonList(result);
   }

   private long convertSize(long bytes, String unit) {
        switch (unit.toLowerCase()) {
            case "kb": return bytes / 1024;
            case "mb": return bytes / (1024 * 1024);
            case "gb": return bytes / (1024 * 1024 * 1024);
            default: return bytes;
        }
   }

   private long convertTime(long ms, String unit) {
        switch (unit.toLowerCase()) {
            case "ms":
            case "s":
            case "sec":
            case "seconds": return ms / 1000;
            case "m":
            case "min":
            case "minutes": return ms / (1000 * 60);
            default: return ms;
        }
   }
}
