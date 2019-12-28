package common;

/**
 * Identifies a user.
 */

public interface UserDTO {
    public String getUsername();

    public String getPassword();

    public void setUid(int uid);

    public int getUid();

    public FileClient getResvmsg();

}
