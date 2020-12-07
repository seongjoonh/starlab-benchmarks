/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.datasketches.pig.quantiles;

import java.io.IOException;
import java.util.Comparator;

import org.apache.datasketches.ArrayOfStringsSerDe;
import org.apache.datasketches.memory.Memory;
import org.apache.datasketches.quantiles.ItemsSketch;
import org.apache.pig.EvalFunc;
import org.apache.pig.data.DataByteArray;
import org.apache.pig.data.Tuple;

/**
 * This UDF is to get a human-readable summary of a given sketch.
 */
public class StringsSketchToString extends EvalFunc<String> {

  @Override
public java.lang.String exec(final org.apache.pig.data.Tuple input) throws java.io.IOException {
    {
        if (/* NPEX_NULL_EXP */
        input.size() != 1) {
            throw new java.lang.IllegalArgumentException("expected one input");
        }
        if (!(input.get(0) instanceof org.apache.pig.data.DataByteArray)) {
            throw new java.lang.IllegalArgumentException("expected a DataByteArray as a sketch, got " + input.get(0).getClass().getSimpleName());
        }
        final org.apache.pig.data.DataByteArray dba = ((org.apache.pig.data.DataByteArray) (input.get(0)));
        final org.apache.datasketches.quantiles.ItemsSketch<java.lang.String> sketch = org.apache.datasketches.quantiles.ItemsSketch.getInstance(org.apache.datasketches.memory.Memory.wrap(dba.get()), java.util.Comparator.naturalOrder(), new org.apache.datasketches.ArrayOfStringsSerDe());
        return sketch.toString();
    }
}

}
