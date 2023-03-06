package dev.foxen.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.*;

public class DataManager {
    private static final String API_URL = "https://polisen.se/api/events";

    private final Gson gson = new GsonBuilder().create();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public HttpRequest makeGetRequest(String URL) {
        return HttpRequest.newBuilder()
                .uri(URI.create(URL))
                .build();
    }

    public HttpResponse<String> getHttpResponse(String URL) {
        final HttpRequest getRequest = makeGetRequest(URL);
        HttpResponse<String> response = null;

        try {
            response = httpClient.send(getRequest, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            System.out.println("error: " + e);
        }
        return response;
    }

    public CaseDTO[] getData(Integer amount, Boolean filterCrime) throws InterruptedException {
        String todaysDate = LocalDate.now().toString();
        CaseDTO[] current = getCasesFromDate(todaysDate, filterCrime);

        if (current.length >= amount) {
            return Arrays.stream(current).limit(amount).toArray(CaseDTO[]::new);
        }

        int currentAmount = current.length;
        CaseDTO[] yesterdays = getCasesFromDate(getPreviousDate(current), filterCrime);

        while (currentAmount < amount) {
            System.out.println("Cases collected: " + currentAmount);
            current = Arrays.copyOf(current, current.length + yesterdays.length);
            System.arraycopy(yesterdays, 0, current, currentAmount, yesterdays.length);
            currentAmount += yesterdays.length;
            yesterdays = getCasesFromDate(getPreviousDate(yesterdays), filterCrime);

            // Sleeping for 1 second between every request
            Thread.sleep(1000);
        }

        return Arrays.stream(current).limit(amount).toArray(CaseDTO[]::new);
    }

    private CaseDTO[] getCasesFromDate(String date) {
        return getCasesFromDate(date, false);
    }

    private CaseDTO[] getCasesFromDate(String date, Boolean filterCrime) {
        HttpResponse<String> response = getHttpResponse(API_URL + "?DateTime=" + date);
        if (response == null) return new CaseDTO[0];

        return filterCrime ? filterCrime(caseFromJson(response.body())) : caseFromJson(response.body());
    }

    // Because of the API limitation of not having a from/to date argument (or any pagination)
    // I figured the easiest way to get around this was to check the previous day's cases until we have enough
    private String getPreviousDate(CaseDTO[] data) {
		CaseDTO last = data[data.length - 1];
        return LocalDate.parse(last.getDate()).minusDays(1).toString();
    }

    // Returns a map with Location name and number of cases
    public Map<String, Integer> getPerCity(CaseDTO[] cases) {
        Map<String, Integer> map = new HashMap<>();
        for (CaseDTO x : cases) {
            if (map.containsKey(x.getLocation().getLocationName())) {
                map.put(x.getLocation().getLocationName(), map.get(x.getLocation().getLocationName()) + 1);
            } else {
                map.put(x.getLocation().getLocationName(), 1);
            }
        }
        return map;
    }

    // Filters out the cases that are not inherently crimes
    private CaseDTO[] filterCrime(CaseDTO[] cases) {
        List<CaseDTO> filtered = new ArrayList<>();
        for (CaseDTO x : cases) {
            if (!x.notCrime()) {
                filtered.add(x);
            }
        }
        return filtered.toArray(new CaseDTO[0]);
    }

    // Gets a CaseDTO array from a JSON string
    private CaseDTO[] caseFromJson(String json) {
        return gson.fromJson(json, CaseDTO[].class);
    }
}
