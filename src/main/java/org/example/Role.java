package org.example;

public enum Role {
    ADMIN ("ADMIN"),
    GUEST ("GAST"),
    USER ("USER");



    private final String title;

    Role(String role) {
        this.title = role;
    }

    public static Role findByTitle(String name) {
        for (Role role : values()) {
            if (role.name().equalsIgnoreCase(name)) {
                return role;
            }
        }
        return null;
    }


    public String getTitle() {
        return this.title;
    }
    @Override
    public String toString() {
        return "Role{" +
                "role='" + title + '\'' +
                '}';
    }

}
