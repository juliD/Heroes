package pem.de.heroes;

/**
 * Created by Julia on 10.06.2017.
 */

public class ListItem {
    private String title;
    private String description;
    private String address;

    public String getDescription() {
        return description;
    }

    public String getAddress() {
        return address;
    }

    public ListItem(String title, String description, String address) {
        this.title = title;
        this.description = description;
        this.address = address;
    }

    public String getTitle() {

        return title;
    }
}
