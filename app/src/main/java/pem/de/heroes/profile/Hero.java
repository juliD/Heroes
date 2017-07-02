package pem.de.heroes.profile;

public class Hero {
    private String name;
    private int requiredKarmaTillNextLevel;

    public Hero(String name, int requiredKarmaTillNextLevel) {
        this.name = name;
        this.requiredKarmaTillNextLevel = requiredKarmaTillNextLevel;
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
}
