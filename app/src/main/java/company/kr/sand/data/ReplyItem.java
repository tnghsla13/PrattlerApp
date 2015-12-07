package company.kr.sand.data;

/**
 * Created by User on 2015-12-02.
 */
public class ReplyItem {

    private String name, profilePic, body;
    public ReplyItem() {
    }

    public ReplyItem(String name, String profilePic, String body) {
        super();
        this.name = name;
        this.profilePic = profilePic;
        this.body = body;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }
}
