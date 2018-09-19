/**
 * ﻿Copyright 2015-2018 Valery Silaev (http://vsilaev.com)
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:

 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.

 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.

 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.tascalate.async.scheduler;

import java.util.Set;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.function.Function;

import net.tascalate.async.Scheduler;

public class SimpleScheduler extends AbstractExecutorScheduler<Executor> {
    public static final Scheduler SAME_THREAD_SCHEDULER = new SimpleScheduler(Runnable::run) {
        @Override
        public String toString() {
            return "<same-thread-contextless-scheduler>";
        }
    };
    
    public SimpleScheduler(Executor executor) {
        this(executor, null, null);
    }
    
    public SimpleScheduler(Executor executor, Set<Characteristics> characteristics) {
        this(executor, characteristics, null);
    }

    public SimpleScheduler(Executor executor, Function<? super Runnable, ? extends Runnable> contextualizer) {
        this(executor, null, contextualizer);
    }        
    
    public SimpleScheduler(Executor executor, Set<Characteristics> characteristics, Function<? super Runnable, ? extends Runnable> contextualizer) {
        super(executor, ensureNonInterruptibleCharacteristic(characteristics), contextualizer);
    }
    
    @Override
    public CompletionStage<?> schedule(Runnable command) {
        SchedulePromise<?> result = new SchedulePromise<>();
        Runnable wrapper = new Runnable() {
            @Override
            public void run() {
                try {
                    command.run();
                    result.success(null);
                } catch (final Throwable ex) {
                    result.failure(ex);
                }
            }
        };
        executor.execute(wrapper);
        return result;
    }
    
    private static Set<Characteristics> ensureNonInterruptibleCharacteristic(Set<Characteristics> characteristics) {
        if (null == characteristics || !characteristics.contains(Characteristics.INTERRUPTIBLE)) {
            return characteristics;
        }
        throw new IllegalArgumentException("Characteristics must contains " + Characteristics.INTERRUPTIBLE);
    }
}
