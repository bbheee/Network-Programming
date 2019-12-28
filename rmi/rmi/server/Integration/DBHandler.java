package server.Integration;

import common.FileDTO;
import server.model.User;

import java.sql.*;


public class DBHandler {
    String driverClassName = "com.mysql.cj.jdbc.Driver";
    String url = "jdbc:mysql://localhost:8889/filecatalog";
    String username = "root";
    String password = "root";
    Connection connection = null;

    private PreparedStatement checkUserStmt;
    private PreparedStatement createUserStmt;
    private PreparedStatement loginUserStmt;
    private PreparedStatement createFileStmt;
    private PreparedStatement deleteFileStmt;
    private PreparedStatement showAllFileStmt;
    private PreparedStatement checkFileStmt;
    private PreparedStatement getFileOwnerStmt;
    private PreparedStatement fileIsWritableStmt;
    private PreparedStatement getUserIdStmt;

    public DBHandler() {
        try {
            connect();
            prepareStatements(connection);

        } catch (Exception ex) {
            ex.printStackTrace();

        }
    }

    public void connect() throws Exception {
        Class.forName(driverClassName);
        connection = DriverManager.getConnection(url, username, password);
        System.out.println("Connect to database successfully");

    }

    public boolean unduplicatedUsername(String username) {
        boolean unduplicatedUsername = false;
        try {
            checkUserStmt.setString(1, username);
            ResultSet resultSet = checkUserStmt.executeQuery();
            if (resultSet.next()) {
                unduplicatedUsername = false;
            } else {
                unduplicatedUsername = true;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return unduplicatedUsername;

    }

    public String createUser(User user) {
        String message = "You already have an account!";
        try {
            if (unduplicatedUsername(user.getUsername())) {
                createUserStmt.setString(1, user.getUsername());
                createUserStmt.setString(2, user.getPassword());
                createUserStmt.executeUpdate();
                message = "You username is: " + user.getUsername() + ", please login! ";
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return message;
    }

    public int loginUser(User user) {
        if (user == null) {
            System.out.println("null");
        }
        int uid = 0;
        try {
            loginUserStmt.setString(1, user.getUsername());
            loginUserStmt.setString(2, user.getPassword());
            ResultSet resultSet = loginUserStmt.executeQuery();
            if (resultSet.next()) {
                user.setUid(resultSet.getInt("id"));
                uid = resultSet.getInt("id");
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return uid;
    }

    public String showFiles() {

        try {
            ResultSet resultSet = showAllFileStmt.executeQuery();
            StringBuilder sb = new StringBuilder();
            while (resultSet.next()) {
                sb.append(resultSet.getString("filename") + " ");
                sb.append(resultSet.getString("filesize") + " ");
                sb.append(resultSet.getString("fileowner") + " ");
                sb.append(resultSet.getString("filewrite") + "\n");
            }
            return sb.toString();

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private boolean unduplicatedFile(String filename) {
        boolean unduplicatedFilename = false;
        try {
            checkFileStmt.setString(1, filename);
            ResultSet resultSet = checkFileStmt.executeQuery();
            if (resultSet.next()) {
                return false;
            } else {
                return true;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return unduplicatedFilename;
    }

    public String uploadFile(FileDTO fileDTO, String username) {
        try {
            if (unduplicatedFile(fileDTO.getFileName())) {
                createFileStmt.setString(1, fileDTO.getFileName());
                createFileStmt.setInt(2, fileDTO.getFileSize());
                createFileStmt.setString(3, username);
                createFileStmt.setBoolean(4, fileDTO.isWriteable());
                createFileStmt.executeUpdate();
                return "File upload: " + fileDTO.getFileName();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return "Try a different file name!";

    }

    public boolean deleteFile(FileDTO fileDTO, String username) {
        if (!unduplicatedFile(fileDTO.getFileName())) {
            if (fileIsWritable(fileDTO, username)) {
                try {
                    deleteFileStmt.setString(1, fileDTO.getFileName());
                    deleteFileStmt.executeUpdate();
                    return true;
                } catch (SQLException e) {
                    e.printStackTrace();
                    return false;
                }
            }
        }
        return false;
    }

    public String getFileowner(FileDTO fileDTO) {
        try {
            getFileOwnerStmt.setString(1, fileDTO.getFileName());
            ResultSet resultSet = getFileOwnerStmt.executeQuery();
            while (resultSet.next()) {
                return resultSet.getString("fileowner");

            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return "Wrong!";
    }

    private boolean fileIsWritable(FileDTO fileDTO, String username) {
        boolean fileWritable = false;
        if (fileDTO.getFileowner().equals(username))
            return true;
        try {
            fileIsWritableStmt.setString(1, fileDTO.getFileName());
            ResultSet resultSet = fileIsWritableStmt.executeQuery();
            while (resultSet.next()) {
                if (resultSet.getInt(1) == 0) {
                    return false;
                } else {
                    return true;
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return fileWritable;
    }

    public int getUserID(String username) {
        try {
            getUserIdStmt.setString(1, username);
            ResultSet resultSet = getUserIdStmt.executeQuery();
            while (resultSet.next()) {
                return resultSet.getInt("id");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return -1;

    }


    private void prepareStatements(Connection connection) throws SQLException {
        checkUserStmt = connection.prepareStatement("SELECT * FROM users WHERE username = ?");

        createUserStmt = connection.prepareStatement("INSERT INTO users(username,password) VALUES (?, ?)");

        createFileStmt = connection.prepareStatement("INSERT INTO files(filename,filesize,fileowner,filewrite) VALUES ( ?, ?, ?, ?)");

        loginUserStmt = connection.prepareStatement("SELECT * FROM users WHERE username = ? AND password = ?");

        showAllFileStmt = connection.prepareStatement("SELECT * FROM files");

        checkFileStmt = connection.prepareStatement("SELECT * FROM files WHERE filename = ?");

        deleteFileStmt = connection.prepareStatement("DELETE FROM files WHERE filename = ?");

        getFileOwnerStmt = connection.prepareStatement("SELECT fileowner from files where filename = ?");

        fileIsWritableStmt = connection.prepareStatement("SELECT filewrite,fileowner FROM files WHERE filename = ?");

        getUserIdStmt = connection.prepareStatement("SELECT * FROM users WHERE username = ?");

    }
}
