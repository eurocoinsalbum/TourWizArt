package codingdavinci.tour.model;

import java.util.UUID;

public class People {
    public UUID uuid;
    public String name;

    public People(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
    }
}
