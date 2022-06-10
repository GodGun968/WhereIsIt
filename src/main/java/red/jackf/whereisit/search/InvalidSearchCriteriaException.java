package red.jackf.whereisit.search;


import java.io.IOException;

public class InvalidSearchCriteriaException extends IOException {
    public InvalidSearchCriteriaException(String message) {
        super(message);
    }

    public InvalidSearchCriteriaException(Throwable t) {
        super(t);
    }
}
