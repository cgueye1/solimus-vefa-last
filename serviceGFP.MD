package com.wakana.samater.services.impl;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;
import com.wakana.samater.dto.StopTimeResponse;
import com.wakana.samater.model.gtfs.Calendar;
import com.wakana.samater.model.gtfs.Route;
import com.wakana.samater.model.gtfs.StopTime;
import com.wakana.samater.model.gtfs.Trip;
import com.wakana.samater.repository.CalendarRepository;
import com.wakana.samater.repository.RouteRepository;
import com.wakana.samater.repository.StopTimeRepository;
import com.wakana.samater.repository.TripRepository;
import com.wakana.samater.services.GtfsService;
import lombok.RequiredArgsConstructor;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.Comparator;
@Service
@RequiredArgsConstructor
public class GtfsServiceImpl implements GtfsService {

    private final CalendarRepository calendarRepository;
    private final TripRepository tripRepository;
    private final StopTimeRepository stopTimeRepository;
    private final RouteRepository routeRepository;

    @Override
    public List<StopTime> findClosestStopTimes(String date, String time, String directionId, String stopId) {
        // Step 1: Parse date and time
        LocalDate parsedDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyyMMdd"));
        LocalTime parsedTime = LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm:ss"));
        String dayOfWeek = parsedDate.getDayOfWeek().name().toLowerCase();

        // Step 2: Retrieve calendars where the date is between startDate and endDate
        List<Calendar> validCalendars = calendarRepository.findAll().stream()
            .filter(calendar -> isDateWithinRange(parsedDate, calendar.getStartDate(), calendar.getEndDate()))
            .collect(Collectors.toList());

        // Step 3: Filter calendars based on the day of the week
        List<String> serviceIds = validCalendars.stream()
                .filter(calendar -> calendarDayMatches(calendar, dayOfWeek))
                .map(Calendar::getServiceId)
                .collect(Collectors.toList());

        // Step 4: Find trips associated with these calendars and the specified directionId
        List<Trip> trips = tripRepository.findAllByServiceIdInAndDirectionId(serviceIds, directionId);
        List<String> tripIds = trips.stream().map(Trip::getTripId).collect(Collectors.toList());

        // Step 5: Retrieve stop times for these trips and filter by stopId containing stopId, sort by departure time
        List<StopTime> stopTimes = stopTimeRepository.findAllByTripIdIn(tripIds);
        List<StopTime> stopTimes4 = stopTimes.stream()
            .filter(st -> st.getStopId().contains(stopId) && LocalTime.parse(st.getDepartureTime()).isAfter(parsedTime))
            .sorted(Comparator.comparing(StopTime::getDepartureTime))
            .limit(4)
            .collect(Collectors.toList());

        List<StopTimeResponse> stopTimesResponses = new ArrayList<>();
        
        // Collect all required trips and routes in advance to minimize database hits
        Map<String, Trip> tripsMap = tripRepository.findAllByTripIdIn(tripIds).stream()
            .collect(Collectors.toMap(Trip::getTripId, trip -> trip));

        Map<String, Route> routesMap = routeRepository.findAllByRouteIdIn(tripsMap.values().stream()
            .map(Trip::getRouteId)
            .collect(Collectors.toList()))
            .stream()
            .collect(Collectors.toMap(Route::getRouteId, route -> route));

       for (StopTime stopTime : stopTimes4) {
            StopTimeResponse stopTimeResponse = new StopTimeResponse();
            stopTimeResponse.setStopTime(stopTime);
            
            Trip trip = tripsMap.get(stopTime.getTripId());
            if (trip == null) {
                // Handle missing trip case, e.g., log an error or continue
                continue;
            }
            stopTimeResponse.setTrip(trip);
            
            Route route = routesMap.get(trip.getRouteId());
            if (route == null) {
                // Handle missing route case, e.g., log an error or continue
                continue;
            }
            stopTimeResponse.setRoute(route);

            stopTimesResponses.add(stopTimeResponse);
        }

        return stopTimes4;
    }

    private boolean isDateWithinRange(LocalDate date, String startDateStr, String endDateStr) {
        LocalDate startDate = LocalDate.parse(startDateStr, DateTimeFormatter.ofPattern("yyyyMMdd"));
        LocalDate endDate = LocalDate.parse(endDateStr, DateTimeFormatter.ofPattern("yyyyMMdd"));
        return (date.isEqual(startDate) || date.isAfter(startDate)) && (date.isEqual(endDate) || date.isBefore(endDate));
    }

    private boolean calendarDayMatches(Calendar calendar, String dayOfWeek) {
        switch (dayOfWeek) {
            case "monday": return "1".equals(calendar.getMonday());
            case "tuesday": return "1".equals(calendar.getTuesday());
            case "wednesday": return "1".equals(calendar.getWednesday());
            case "thursday": return "1".equals(calendar.getThursday());
            case "friday": return "1".equals(calendar.getFriday());
            case "saturday": return "1".equals(calendar.getSaturday());
            case "sunday": return "1".equals(calendar.getSunday());
            default: return false;
        }
    }
}
