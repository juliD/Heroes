package pem.de.heroes.profile;

public class Hero {
    private String name;
    private int requiredKarmaTillNextLevel;
    private int resource;

    public Hero(String name, int requiredKarmaTillNextLevel, int resource) {
        this.name = name;
        this.requiredKarmaTillNextLevel = requiredKarmaTillNextLevel;
        this.resource = resource;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getRequiredKarmaTillNextLevel() {
        return requiredKarmaTillNextLevel;
    }

    public void setRequiredKarmaTillNextLevel(int requiredKarmaTillNextLevel) {
        this.requiredKarmaTillNextLevel = requiredKarmaTillNextLevel;
    }

    public int getResource() {
        return resource;
    }

    public void setResource(int resource) {
        this.resource = resource;
    }
}
