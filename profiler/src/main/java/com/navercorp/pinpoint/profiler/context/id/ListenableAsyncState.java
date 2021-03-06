/*
 * Copyright 2017 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.profiler.context.id;

import com.navercorp.pinpoint.bootstrap.context.AsyncState;
import com.navercorp.pinpoint.common.annotations.InterfaceAudience;

/**
 * @author jaehong.kim
 */
@InterfaceAudience.LimitedPrivate("vert.x")
public class ListenableAsyncState implements AsyncState {

    private final AsyncStateListener asyncStateListener;

    private boolean setup = false;
    private boolean await = false;
    private boolean finish = false;

    public ListenableAsyncState(AsyncStateListener asyncStateListener) {
        if (asyncStateListener == null) {
            throw new NullPointerException("asyncStateListener must not be null");
        }
        this.asyncStateListener = asyncStateListener;
    }

    @Override
    public void finish() {
        boolean finished = false;
        synchronized (this) {
            if (this.await && !this.finish) {
                finished = true;
            }
            this.finish = true;
        }
        if (finished) {
            this.asyncStateListener.finish();
        }
    }

    @Override
    public void setup() {
        synchronized (this) {
            this.setup = true;
        }
    }

    @Override
    public boolean await() {
        synchronized (this) {
            if (!this.setup || this.finish) {
                return false;
            }
            this.await = true;
            return true;
        }
    }

    @InterfaceAudience.LimitedPrivate("LocalTraceContext")
    public interface AsyncStateListener {
        void finish();
    }

    @Override
    public String toString() {
        return "ListenableAsyncState{" +
                "asyncStateListener=" + asyncStateListener +
                ", setup=" + setup +
                ", await=" + await +
                ", finish=" + finish +
                '}';
    }
}