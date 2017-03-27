package hk.edu.cuhk.ie.iems5722.a2_1155080901.Data;

/**
 * Created by leoymr on 13/2/17.
 */

public class Chatroom_INFO {

    private String chatroom_name;
    private int id;

    public int getId() {
        return id;
    }

    public String getChatroom_name() {
        return chatroom_name;
    }

    public Chatroom_INFO(int id, String chatroom_name) {
        this.chatroom_name = chatroom_name;
        this.id = id;
    }
}
