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
 * Represents an operation that accepts a single <code>BidiRun</code> object and returns no result.
 */
public abstract class BidiRunConsumer {

    boolean isStopped;

    /**
     * Performs this operation on the given <code>BidiRun</code> object.
     *
     * @param bidiRun The input <code>BidiRun</code> argument.
     */
    public abstract void accept(BidiRun bidiRun);

    /**
     * Instructs the iterator to stop retrieving bidi runs after this iteration.
     */
    public final void stop() {
        isStopped = true;
    }
}
