package com.kilowhat.services;

import com.kilowhat.models.ApplianceCategory;

import java.util.List;
import java.util.Map;

public class BackupCalculatorService {

    // Rough average watts per single appliance in each category.
    // These are estimates, not measured values - good enough for an MVP
    // "how much load does this household probably have" figure.
    private static final Map<String, Integer> AVG_WATTS_PER_CATEGORY = Map.of(
        "KITCHEN", 120,
        "ENTERTAINMENT", 80,
        "HEATING_COOLING", 1200,
        "LIGHTING", 10,
        "OTHER", 50
    );

    private static final double USABLE_BATTERY_WH = 2000.0;

    public double estimateLoadWatts(List<ApplianceCategory> categories, int householdSize) {
        double totalWatts = 0;
        for (ApplianceCategory c : categories) {
            int avgWatts = AVG_WATTS_PER_CATEGORY.getOrDefault(c.category, 50);
            totalWatts += avgWatts * c.count;
        }
        // more people in the household -> more concurrent usage; a simple
        // multiplier rather than pretending to model this precisely
        double householdMultiplier = 1 + (Math.max(householdSize - 1, 0) * 0.05);
        return totalWatts * householdMultiplier;
    }

    public int estimateRuntimeMinutes(double currentLoadWatts) {
        if (currentLoadWatts <= 0) {
            return Integer.MAX_VALUE;
        }
        double hours = USABLE_BATTERY_WH / currentLoadWatts;
        return (int) Math.round(hours * 60);
    }
}
