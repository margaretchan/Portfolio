// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import com.google.sps.TimeRange;
import com.google.sps.Event;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;

public final class FindMeetingQuery {

    /** 
     * Given all known events and a new event request, find all possible time ranges to schedule 
     * the requested event
     */ 
    public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
        // Instantiate events as ArrayList in order to index
        List<Event> eventsList = new ArrayList<>(events);

        Collection<TimeRange> possibleTimes = new ArrayList<>();
        long duration = request.getDuration();

        if (duration > TimeRange.WHOLE_DAY.duration()) {
            return possibleTimes;
        }

        // Remove events which are irrelevant to the request 
        eventsList.removeIf(e -> (
            Collections.disjoint(e.getAttendees(), request.getAttendees()) 
            || e.getWhen().duration() <= 0));

        // Sort events by start time 
        Collections.sort(eventsList, new Comparator<Event>() {
            public int compare (Event e1, Event e2) {
                return TimeRange.ORDER_BY_START.compare(e1.getWhen(), e2.getWhen());
            }
        });

        if (eventsList.isEmpty()) {
            sufficientTime(TimeRange.START_OF_DAY, TimeRange.END_OF_DAY, duration, 
                            possibleTimes, true);
        } else {
            // Check for time before first event of day
            sufficientTime(TimeRange.START_OF_DAY, eventsList.get(0).getWhen().start(), duration, 
                            possibleTimes, false);
        }

        for (int i = 0; i < eventsList.size(); i++) {
            Event currEvent = eventsList.get(i);
            TimeRange currEventTime = currEvent.getWhen();
            int currEventEnd = currEventTime.end();
            
            if (i != eventsList.size() - 1) {
                TimeRange nextEventTime = eventsList.get(i + 1).getWhen();
                int nextEventStart = nextEventTime.start();

                if (currEventTime.overlaps(nextEventTime)) {
                    // Check for nested events
                    if (currEventTime.contains(nextEventTime)) {
                        eventsList.set(i + 1, currEvent);
                    }
                } else {
                    sufficientTime(currEventEnd, nextEventStart, duration, possibleTimes, false);
                }
            } else {
                // Check for time after last event of day
                sufficientTime(currEventEnd, TimeRange.END_OF_DAY, duration, possibleTimes, true);
            }
        }

        return possibleTimes;
    }

    /** 
     * Checks for sufficient meeting time between startTime and endTime.
     * If there is enough time, add a new TimeRange to the possibleTimes list.
     */
    private void sufficientTime (int startTime, int endTime, long duration, 
                                Collection<TimeRange> possibleTimes, boolean inclusive) {
        int freeTime = endTime - startTime;
        if (freeTime >= duration) {
            possibleTimes.add(TimeRange.fromStartEnd(startTime, endTime, inclusive));
        }
    }

}
