package hk.edu.cuhk.ie.iems5722.a2_1155080901.Data;

/**
 * Created by leoymr on 29/1/17.
 */

public class Send_INFO {
    /**
     * Store text information
     */

    private String left_text;
    private String name;
    private String right_text;
    private String textTime;
    private String dateTime;

    public Send_INFO(String left_text, String right_text, String name, String textTime,String dateTime) {
        this.left_text = left_text;
        this.right_text = right_text;
        this.textTime = textTime;
        this.name = name;
        this.dateTime = dateTime;
    }

    public String getName() {
        return name;
    }

    public String getDateTime() {
        return dateTime;
    }

    public String getLeft_text() {
        return left_text;
    }

    public String getRight_text() {
        return right_text;
    }

    public String getTextTime() {
        return textTime;
    }

}
