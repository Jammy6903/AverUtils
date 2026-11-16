package onl.jami.averUtils.exceptions;

public class PlayerNotOnlineException extends RuntimeException {
    public PlayerNotOnlineException(String message) {
        super(message);
    }
}
