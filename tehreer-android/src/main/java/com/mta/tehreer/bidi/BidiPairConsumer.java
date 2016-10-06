/*
 * Copyright (C) 2016 Muhammad Tayyab Akram
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

package com.mta.tehreer.bidi;

/**
 * Represents an operation that accepts a single <code>BidiPair</code> object and returns no result.
 */
public abstract class BidiPairConsumer {

    boolean isStopped;

    /**
     * Performs this operation on the given <code>BidiPair</code> object.
     *
     * @param bidiPair The input <code>BidiPair</code> argument.
     */
    public abstract void accept(BidiPair bidiPair);

    /**
     * Instructs the iterator to stop retrieving bidi pairs after this iteration.
     */
    public final void stop() {
        isStopped = true;
    }
}
