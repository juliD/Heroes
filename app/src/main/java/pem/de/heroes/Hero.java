package pem.de.heroes;

class Hero {
    private String name;
    private int requiredKarma;

    public Hero(String name, int requiredKarma) {
        this.name = name;
        this.requiredKarma = requiredKarma;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getRequiredKarma() {
        return requiredKarma;
    }

    public void setRequiredKarma(int requiredKarma) {
        this.requiredKarma = requiredKarma;
    }
}
