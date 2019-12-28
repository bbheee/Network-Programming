package server.model;

import common.FileClient;
import common.UserDTO;

import java.io.Serializable;

public class User implements Serializable, UserDTO {
    private String username;
    private String password;
    private FileClient resvmsg;
    private int id;

    public User(String username, String password, FileClient resvmsg) {
        this.username = username;
        this.password = password;
        this.resvmsg = resvmsg;
        this.id = 0;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public void setUid(int id) {
        this.id = id;
    }

    @Override
    public int getUid() {
        return this.id;
    }

    @Override
    public FileClient getResvmsg() {
        return resvmsg;
    }

}
