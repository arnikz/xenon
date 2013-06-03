/*
 * Copyright 2013 Netherlands eScience Center
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
package nl.esciencecenter.octopus.adaptors.local;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

import nl.esciencecenter.octopus.credentials.Credential;
import nl.esciencecenter.octopus.engine.OctopusEngine;
import nl.esciencecenter.octopus.engine.OctopusProperties;
import nl.esciencecenter.octopus.engine.jobs.SchedulerImplementation;
import nl.esciencecenter.octopus.engine.util.JobQueues;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.exceptions.OctopusIOException;
import nl.esciencecenter.octopus.exceptions.OctopusRuntimeException;
import nl.esciencecenter.octopus.jobs.Job;
import nl.esciencecenter.octopus.jobs.JobDescription;
import nl.esciencecenter.octopus.jobs.JobStatus;
import nl.esciencecenter.octopus.jobs.Jobs;
import nl.esciencecenter.octopus.jobs.QueueStatus;
import nl.esciencecenter.octopus.jobs.Scheduler;
import nl.esciencecenter.octopus.jobs.Streams;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalJobs implements Jobs {

    @SuppressWarnings("unused")
    private static final Logger logger = LoggerFactory.getLogger(LocalJobs.class);

    @SuppressWarnings("unused")
    private final OctopusEngine octopusEngine;

    private final LocalAdaptor localAdaptor;

    private final Scheduler localScheduler;

    // private final LocalProcessFactory factory;
    
    private final JobQueues jobQueues; 
    
   // private final String defaultWorkingDirectory;

    public LocalJobs(OctopusProperties properties, LocalAdaptor localAdaptor, OctopusEngine octopusEngine)
            throws OctopusException {

        this.octopusEngine = octopusEngine;
        this.localAdaptor = localAdaptor;
        
        URI uri = null;

        try {
            uri = new URI("local:///");
        } catch (URISyntaxException e) {
            throw new OctopusRuntimeException(LocalAdaptor.ADAPTOR_NAME, "Failed to create URI", e);
        }

        //defaultWorkingDirectory = System.getProperty("user.dir");
        
       // if (defaultWorkingDirectory == null) { 
      //      throw new OctopusException(LocalAdaptor.ADAPTOR_NAME, "Failed to retrieve current working directory!");
      //  }

        localScheduler = new SchedulerImplementation(LocalAdaptor.ADAPTOR_NAME, "LocalScheduler", uri, 
                new String[] { "single", "multi", "unlimited" }, null, properties, true, true, true);

        int processors = Runtime.getRuntime().availableProcessors();
        
        int multiQThreads = properties.getIntProperty(LocalAdaptor.MULTIQ_MAX_CONCURRENT, processors);
        int maxQSize = properties.getIntProperty(LocalAdaptor.MAX_HISTORY);
        int pollingDelay = properties.getIntProperty(LocalAdaptor.POLLING_DELAY);
        
        jobQueues = new JobQueues(localAdaptor, localScheduler, new LocalProcessWrapperFactory(), multiQThreads, maxQSize, pollingDelay);        
    }

    @Override
    public Scheduler newScheduler(URI location, Credential credential, Properties properties) throws OctopusException,
            OctopusIOException {

        localAdaptor.checkURI(location);

        String path = location.getPath();

        if (path != null && !path.equals("/")) {
            throw new OctopusException(LocalAdaptor.ADAPTOR_NAME, "Cannot create local scheduler with path!");
        }

        if (credential != null) {
            throw new OctopusException(LocalAdaptor.ADAPTOR_NAME, "Cannot create local scheduler with credentials!");
        }

        if (properties != null && properties.size() > 0) {
            throw new OctopusException(LocalAdaptor.ADAPTOR_NAME, "Cannot create local scheduler with additional properties!");
        }

        return localScheduler;
    }

    @Override
    public Scheduler getLocalScheduler() throws OctopusException, OctopusIOException {
        return localScheduler;
    }

    @Override
    public Job[] getJobs(Scheduler scheduler, String... queueNames) throws OctopusException, OctopusIOException {
        return jobQueues.getJobs(queueNames);
    }

    @Override
    public Job submitJob(Scheduler scheduler, JobDescription description) throws OctopusException {
        return jobQueues.submitJob(scheduler, description);
    }

    @Override
    public JobStatus getJobStatus(Job job) throws OctopusException {
        return jobQueues.getJobStatus(job);
    }

    @Override
    public JobStatus[] getJobStatuses(Job... jobs) {
        return jobQueues.getJobStatuses(jobs);
    }

    @Override
    public void cancelJob(Job job) throws OctopusException {
        jobQueues.cancelJob(job);
    }

    public void end() {
        jobQueues.end();        
    }

    @Override
    public QueueStatus getQueueStatus(Scheduler scheduler, String queueName) throws OctopusException {
        return jobQueues.getQueueStatus(scheduler, queueName);
    }

    @Override
    public QueueStatus[] getQueueStatuses(Scheduler scheduler, String... queueNames) throws OctopusException {
        return jobQueues.getQueueStatuses(scheduler, queueNames);
    }

    @Override
    public void close(Scheduler scheduler) throws OctopusException, OctopusIOException {
        // ignored
    }

    @Override
    public boolean isOpen(Scheduler scheduler) throws OctopusException, OctopusIOException {
        return true;
    }

    @Override
    public Streams getStreams(Job job) throws OctopusException {
        return jobQueues.getStreams(job);
    }
}