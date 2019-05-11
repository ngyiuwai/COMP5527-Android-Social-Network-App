package comp5527.android.social_network_v03;

public class FindFriends
{
    public String profileimage, fullname, status;

    public FindFriends()
    {

    }

    public FindFriends(String profileimage, String fullname, String status)
    {
        this.profileimage = profileimage;
        this.fullname = fullname;
        this.status = status;
    }

    public String getProfileimage() {
        return profileimage;
    }

    public String getFullname() {
        return fullname;
    }

    public String getStatus() {
        return status;
    }

    public void setProfileimage(String profileimage) {
        this.profileimage = profileimage;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
