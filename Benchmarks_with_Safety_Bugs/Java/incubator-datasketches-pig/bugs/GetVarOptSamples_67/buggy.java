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

package org.apache.datasketches.pig.sampling;

import static org.apache.datasketches.pig.sampling.VarOptCommonImpl.RECORD_ALIAS;
import static org.apache.datasketches.pig.sampling.VarOptCommonImpl.WEIGHT_ALIAS;
import static org.apache.datasketches.pig.sampling.VarOptCommonImpl.createDataBagFromSketch;

import java.io.IOException;

import org.apache.datasketches.memory.Memory;
import org.apache.datasketches.sampling.VarOptItemsSketch;
import org.apache.pig.EvalFunc;
import org.apache.pig.data.DataBag;
import org.apache.pig.data.DataByteArray;
import org.apache.pig.data.DataType;
import org.apache.pig.data.Tuple;
import org.apache.pig.impl.logicalLayer.FrontendException;
import org.apache.pig.impl.logicalLayer.schema.Schema;

/**
 * This UDF extracts samples from the binary image of a VarOpt&lt;Tuple&gt; sketch. Because the
 * input is a binary object, this UDF is unable to automatically determine the data schema at query
 * planning time, beyond knowing that the result will be a <tt>DataBag</tt> of
 * (varOptWeight, (record)) tuples.
 *
 * @author Jon Malkin
 */
public class GetVarOptSamples extends EvalFunc<DataBag> {
  private static final ArrayOfTuplesSerDe SERDE = new ArrayOfTuplesSerDe();

  @Override
  public DataBag exec(final Tuple inputTuple) throws IOException {
    if (inputTuple == null || inputTuple.size() < 1 || inputTuple.isNull(0)) {
      return null;
    }

    final DataByteArray dba = (DataByteArray) inputTuple.get(0);
    final Memory mem = Memory.wrap(dba.get());
    final VarOptItemsSketch<Tuple> sketch = VarOptItemsSketch.heapify(mem, SERDE);

    return createDataBagFromSketch(sketch);
  }

  @Override
public org.apache.pig.impl.logicalLayer.schema.Schema outputSchema(final org.apache.pig.impl.logicalLayer.schema.Schema input) {
    try {
        if (((input == null) || (input.size() == 0)) || (input.getField(0).type != org.apache.pig.data.DataType.BYTEARRAY)) {
            throw new java.lang.IllegalArgumentException("Input to GetVarOptSamples must be a DataByteArray: " + /* NPEX_NULL_EXP */
            input.toString());
        }
        final org.apache.pig.impl.logicalLayer.schema.Schema weightedSampleSchema = new org.apache.pig.impl.logicalLayer.schema.Schema();
        weightedSampleSchema.add(new org.apache.pig.impl.logicalLayer.schema.Schema.FieldSchema(org.apache.datasketches.pig.sampling.VarOptCommonImpl.WEIGHT_ALIAS, org.apache.pig.data.DataType.DOUBLE));
        weightedSampleSchema.add(new org.apache.pig.impl.logicalLayer.schema.Schema.FieldSchema(org.apache.datasketches.pig.sampling.VarOptCommonImpl.RECORD_ALIAS, org.apache.pig.data.DataType.TUPLE));
        return new org.apache.pig.impl.logicalLayer.schema.Schema(new org.apache.pig.impl.logicalLayer.schema.Schema.FieldSchema(getSchemaName(this.getClass().getName().toLowerCase(), input), weightedSampleSchema, org.apache.pig.data.DataType.BAG));
    } catch (final org.apache.pig.impl.logicalLayer.FrontendException e) {
        throw new java.lang.RuntimeException(e);
    }
}
}
