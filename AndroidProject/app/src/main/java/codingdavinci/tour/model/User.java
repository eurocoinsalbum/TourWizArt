package codingdavinci.tour.model;

import java.util.List;
import java.util.UUID;

public class User {
    public enum Role { ADMIN, INSTITUTE, USER }

    private UUID userUuid;
    private String name;
    private Role role;
    private List<Tour> createdTours;
    private List<PlanedTour> planedTours;

    public User(UUID userUuid) {
        this.userUuid = userUuid;
        name = "-";
        role = Role.USER;
    }

    public UUID getUserUuid() {
        return userUuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Tour> getCreatedTours() {
        return createdTours;
    }

    public void setCreatedTours(List<Tour> createdTours) {
        this.createdTours = createdTours;
    }

    public List<PlanedTour> getPlanedTours() {
        return planedTours;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public void setPlanedTours(List<PlanedTour> planedTours) {
        this.planedTours = planedTours;
    }
}
