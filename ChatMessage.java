import java.io.*;

//Creates a ChatMesage Object that has a message string and a type variable
public class ChatMessage implements Serializable {  //Implements serializable so that it can be sent via IO stream

    protected static final long serialVersionUID = -6067470153967221972L;

    static final int WHOISIN = 0, MESSAGE = 1, LOGOUT = 2;
    private int type;
    private String message;

    ChatMessage(int type, String message) {
        this.type = type;
        this.message = message;
    }

    int getType() {
        return type;
    }
    String getMessage() {
        return message;
    }
}