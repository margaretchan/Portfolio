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
import java.util.Optional;
import java.util.Set;

public final class FindMeetingQuery {

    /** 
     * Given all known events and a new event request, find all possible time ranges to schedule 
     * the requested event where all attendees (including optional) can attend. If no such time
     * range exists, return one where all mandatory attendees can attend.
     *
     * Detailed cases:
     * 0. If request has duration greater than a day, return zero slots.
     * 1. if no required and no optional attendees, return all day.
     * 2. If all are optional (no required), treat all optional as if they were required.
     * 3. If has required and no optional, return slots that try to match all required.
     * 4. If has both required and optional, try to match all attendees. 
     *    If none exist, return slots to match required only.
     */ 
    public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
        long duration = request.getDuration();
        Collection<TimeRange> requiredAttendeeTimes = new ArrayList<>();
        Collection<TimeRange> optionalAttendeeTimes = new ArrayList<>();
        Collection<String> requiredAttendees = request.getAttendees();
        Collection<String> optionalAttendees = request.getOptionalAttendees();

        // Case 0
        if (duration > TimeRange.WHOLE_DAY.duration()) {
            return new ArrayList<>();
        }

        if (!requiredAttendees.isEmpty()) {
            // Case 3 + 4
            requiredAttendeeTimes = findFreeTimes(events, requiredAttendees, duration);
        } else if (!optionalAttendees.isEmpty()) {
            // Case 2
            return findFreeTimes(events, optionalAttendees, duration);
        } else {
            // Case 1
            return Arrays.asList(TimeRange.WHOLE_DAY);
        }
        
        // In Case 3, this collection is gaurenteed to be empty
        optionalAttendeeTimes = filterForOptionalAttendees(events, requiredAttendeeTimes, request);

        return optionalAttendeeTimes.isEmpty() ? requiredAttendeeTimes : optionalAttendeeTimes;
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
        Optional<TimeRange> timeOptional;

        List<Event> sortedEvents = filterAndSortEvents(eventsList, attendees);

        if (sortedEvents.isEmpty()) {
            return Arrays.asList(TimeRange.WHOLE_DAY);
        }

        // Check for time before first event of day
        timeOptional = hasSufficientTime(TimeRange.START_OF_DAY, sortedEvents.get(0).getWhen().start(), 
                                         duration, /*inclusive=*/false);
        timeOptional.ifPresent(timeRange -> possibleTimes.add(timeRange));

        for (int i = 0; i < sortedEvents.size(); i++) {
            Event currEvent = sortedEvents.get(i);
            TimeRange currEventTime = currEvent.getWhen();
            int currEventEnd = currEventTime.end();
            
            if (i != sortedEvents.size() - 1) {
                TimeRange nextEventTime = sortedEvents.get(i + 1).getWhen();
                int nextEventStart = nextEventTime.start();

                if (currEventTime.overlaps(nextEventTime)) {
                    // Check for nested events
                    if (currEventTime.contains(nextEventTime)) {
                        sortedEvents.set(i + 1, currEvent);
                    }
                } else {
                    timeOptional = hasSufficientTime(currEventEnd, nextEventStart, duration, 
                                                     /*inclusive=*/false);
                    timeOptional.ifPresent(timeRange -> possibleTimes.add(timeRange));
                }
            } else {
                // Check for time after last event of day
                timeOptional = hasSufficientTime(currEventEnd, TimeRange.END_OF_DAY, duration, 
                                                 /*inclusive=*/true);
                timeOptional.ifPresent(timeRange -> possibleTimes.add(timeRange));
            }
        }
        return possibleTimes;
    }

    /** 
     * Checks for sufficient meeting time between startTime and endTime. If there is, 
     * return Optional of new TimeRange.
     */
    private Optional<TimeRange> hasSufficientTime (int startTime, int endTime, long duration, 
                                                   boolean inclusive) {
        int freeTime = endTime - startTime;
        if (freeTime >= duration) {
            return Optional.of(TimeRange.fromStartEnd(startTime, endTime, inclusive));
        }
        return Optional.empty();
    }

    /** 
     * Find the TimeRanges in timeOptions which all optional attendees can attend.
     */ 
    private Collection<TimeRange> filterForOptionalAttendees (Collection<Event> events, 
                                Collection<TimeRange> timeOptions, MeetingRequest request) {
        // Instantiate events as ArrayList in order to use comparator in sorting
        List<Event> eventsList = new ArrayList<>(events);

        // Make a copy to preserve original
        Collection<TimeRange> possibleTimes = new ArrayList<>(timeOptions);

        List<Event> sortedEvents = filterAndSortEvents(eventsList, request.getOptionalAttendees());

        for (Event event : sortedEvents) {
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
    private List<Event> filterAndSortEvents (List<Event> eventsList, Collection<String> attendees) {
        List<Event> filteredSortedEvents = new ArrayList<>(eventsList);

        // Remove events which are irrelevant to the request 
        filteredSortedEvents.removeIf(e -> (
            e.getWhen().duration() <= 0 || Collections.disjoint(e.getAttendees(), attendees)));

        // Sort events by start time 
        Collections.sort(filteredSortedEvents, new Comparator<Event>() {
            public int compare (Event e1, Event e2) {
                return TimeRange.ORDER_BY_START.compare(e1.getWhen(), e2.getWhen());
            }
        });

        return filteredSortedEvents;
    }

}

