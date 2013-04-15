/**
 * Copyright (c) 2012, NuoDB, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of NuoDB, Inc. nor the names of its contributors may
 *       be used to endorse or promote products derived from this software
 *       without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL NUODB, INC. BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.nuodb.migrator.job;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static java.lang.String.format;

/**
 * @author Sergey Bushik
 */
public class SimpleJobExecutor implements JobExecutor {

    private transient final Logger logger = LoggerFactory.getLogger(getClass());
    private final Job job;
    private final SimpleJobStatus jobStatus;
    private List<JobExecutionListener> listeners = Lists.newArrayList();

    public SimpleJobExecutor(Job job) {
        this.job = job;
        this.jobStatus = new SimpleJobStatus();
    }

    @Override
    public JobStatus getJobStatus() {
        return jobStatus;
    }

    @Override
    public void addJobExecutionListener(JobExecutionListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeJobExecutionListener(JobExecutionListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void pause() {
        synchronized (jobStatus) {
            if (!jobStatus.isPaused()) {
                jobStatus.pause();
            }
        }
    }

    @Override
    public void resume() {
        synchronized (jobStatus) {
            if (jobStatus.isPaused()) {
                jobStatus.resume();
            }
        }
    }

    @Override
    public boolean execute(Map<Object, Object> context) {
        synchronized (jobStatus) {
            if (!jobStatus.isRunning() && !jobStatus.isStopped()) {
                jobStatus.setRunning(true);
                jobStatus.setExecutionStartDate(new Date());
            } else {
                if (logger.isDebugEnabled()) {
                    logger.info(format("Job %1$s is already running or it has been stop", job.getName()));
                }
                return false;
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug(format("Starting execution of job %1$s", job.getName()));
        }
        JobExecution execution = createJobExecution(context);
        try {
            fireJobExecutionEvent(new JobExecutionEvent(execution));
            job.execute(execution);
            synchronized (jobStatus) {
                jobStatus.setExecutionEndDate(new Date());
                jobStatus.setRunning(false);
            }
            fireJobExecutionEvent(new JobExecutionEvent(execution));
        } catch (Throwable failure) {
            if (logger.isDebugEnabled()) {
                logger.debug(format("Job %1$s execution failed", job.getName()), failure);
            }
            synchronized (jobStatus) {
                jobStatus.setExecutionEndDate(new Date());
                jobStatus.setRunning(false);
                jobStatus.setFailure(failure);
            }
            fireJobExecutionEvent(new JobExecutionEvent(execution));
        }
        return true;
    }

    protected JobExecution createJobExecution(Map<Object, Object> context) {
        return new SimpleJobExecution(job, jobStatus, context);
    }

    protected void fireJobExecutionEvent(JobExecutionEvent event) {
        for (JobExecutionListener listener : listeners) {
            listener.onJobExecuted(event);
        }
    }

    @Override
    public void stop() {
        synchronized (jobStatus) {
            if (!jobStatus.isStopped()) {
                jobStatus.stop();
            }
        }
    }

    @Override
    public Job getJob() {
        return job;
    }
}
