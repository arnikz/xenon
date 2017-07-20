/**
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
package nl.esciencecenter.xenon.adaptors.schedulers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.schedulers.Scheduler;
import nl.esciencecenter.xenon.schedulers.SchedulerAdaptorDescription;

public abstract class SchedulerTestParent {

	private Scheduler scheduler;
	private SchedulerAdaptorDescription description;
	private SchedulerLocationConfig locationConfig;

	@Before
	public void setup() throws XenonException {
		scheduler = setupScheduler();
		description = setupDescription();
		locationConfig = setupLocationConfig();
	}

	protected abstract SchedulerLocationConfig setupLocationConfig();

    @After
    public void cleanup() throws XenonException {
    	if (scheduler.isOpen()) { 
    		scheduler.close();
    	}
    }

    public abstract Scheduler setupScheduler() throws XenonException;

    private SchedulerAdaptorDescription setupDescription() throws XenonException {
        String name = scheduler.getAdaptorName();
        return Scheduler.getAdaptorDescription(name);
    }

    @Test
    public void test_close() throws XenonException {
    	scheduler.close();
    	assertFalse(scheduler.isOpen());
    }
    	
    @Test
    public void test_getLocation() throws XenonException {
    	assertEquals(locationConfig.getLocation(), scheduler.getLocation());
    }
    
    @Test
    public void test_unknownJobStatus() throws XenonException {
   	
    	

    }

    
    

}
