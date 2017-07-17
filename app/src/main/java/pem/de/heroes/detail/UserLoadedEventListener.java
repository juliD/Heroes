package pem.de.heroes.detail;

import pem.de.heroes.model.User;

public interface UserLoadedEventListener {
    void onUserLoaded(User user);
}
