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

import com.google.sps.Event;
import com.google.sps.TimeRange;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public final class FindMeetingQuery {

    /** 
     * Given all known events and a new event request, find all possible time ranges to schedule 
     * the requested event where all attendees (including optional) can attend. If no such time
     * range exists, return one where all mandatory attendees can attend.
     */ 
    public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
        long duration = request.getDuration();
        Collection<TimeRange> requiredAttendeeTimes = new ArrayList<>();
        Collection<TimeRange> optionalAttendeeTimes = new ArrayList<>();
        Collection<String> requiredAttendees = request.getAttendees();
        Collection<String> optionalAttendees = request.getOptionalAttendees();

        if (duration > TimeRange.WHOLE_DAY.duration()) {
            return new ArrayList<>();
        }

        if (!requiredAttendees.isEmpty()) {
            requiredAttendeeTimes = findFreeTimes(events, requiredAttendees, duration);
        } else if (!optionalAttendees.isEmpty()) {
            return findFreeTimes(events, optionalAttendees, duration);
        } else {
            return Arrays.asList(TimeRange.WHOLE_DAY);
        }
        
        optionalAttendeeTimes = checkOptionalAttendees(events, requiredAttendeeTimes, request);

        return (optionalAttendeeTimes.isEmpty() ? requiredAttendeeTimes : optionalAttendeeTimes);
    }

    /** 
     * Given all known events and details about a new event request, find all possible time ranges
     * to schedule the requested event.
     */ 
    public Collection<TimeRange> findFreeTimes(Collection<Event> events, 
                                               Collection<String> attendees, long duration) {
        // Instantiate events as ArrayList in order to index
        List<Event> eventsList = new ArrayList<>(events);

        Collection<TimeRange> possibleTimes = new ArrayList<>();

        filterAndSortEvents(eventsList, attendees);

        if (eventsList.isEmpty()) {
            return Arrays.asList(TimeRange.WHOLE_DAY);
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

    /** 
     * Find the timeOptions which all optional attendees can attend.
     */ 
    private Collection<TimeRange> checkOptionalAttendees (Collection<Event> events, 
                                Collection<TimeRange> timeOptions, MeetingRequest request) {
        // Instantiate events as ArrayList in order to use comparator in sorting
        List<Event> eventsList = new ArrayList<>(events);

        // Make a copy to preserve original
        Collection<TimeRange> possibleTimes = new ArrayList<>(timeOptions);

        filterAndSortEvents(eventsList, request.getOptionalAttendees());

        for (Event event : eventsList) {
            Iterator<TimeRange> timeRangeIterator = possibleTimes.iterator();
            while (timeRangeIterator.hasNext()) {

                TimeRange time = timeRangeIterator.next();
                if (event.getWhen().overlaps(time)) {
                    timeRangeIterator.remove();
                }
            }
        } 
        return possibleTimes;
    }

    /** 
     * Filter eventsList and keep events which contain any of the requested attendees.
     * Sort eventsList by start time with earliest appearing first. 
     */ 
    private void filterAndSortEvents (List<Event> eventsList, Collection<String> attendees) {
        // Remove events which are irrelevant to the request 
        eventsList.removeIf(e -> (
            e.getWhen().duration() <= 0 || Collections.disjoint(e.getAttendees(), attendees)));

        // Sort events by start time 
        Collections.sort(eventsList, new Comparator<Event>() {
            public int compare (Event e1, Event e2) {
                return TimeRange.ORDER_BY_START.compare(e1.getWhen(), e2.getWhen());
            }
        });
    }

}

