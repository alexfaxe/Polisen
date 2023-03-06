package dev.foxen.data;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CaseDTO {
    private int id;
    private String datetime;
    private String name;
    private String summary;
    private String url;
    private String type;
    private Location location;

    public boolean isSummary() {
        return type.contains("Sammanfattning");
    }

    public boolean isOther() {
        return type.contains("Övrigt");
    }

    public boolean isTrafficControl() {
        return type.contains("Trafikkontroll");
    }

    public boolean notCrime() {
        return isSummary() || isOther() || isTrafficControl();
    }

    public String getDate() {
        return datetime.substring(0, 10);
    }

    public String getLocationName() {
        return location.getLocationName();
    }
}

class Location {
    private final String name;
    private final String gps;

    public Location(String name, String gps) {
        this.name = name;
        this.gps = gps;
    }

    public String getLocationName() {
        return name;
    }

    @Override
    public String toString() {
        return "Location{" +
                "name='" + name + '\'' +
                ", gps='" + gps + '\'' +
                '}';
    }
}
