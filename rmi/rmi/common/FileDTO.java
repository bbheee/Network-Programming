package common;

import java.io.Serializable;

public class FileDTO implements Serializable {
    private String fileName;
    private int fileSize;
    private String fileowner;
    private boolean isWritable = false;

    // GETTERS

    public String getFileName() {
        return this.fileName;
    }

    public int getFileSize() {
        return fileSize;
    }

    public String getFileowner() {
        return fileowner;
    }

    public boolean isWriteable() {
        return isWritable;
    }

    // SETTERS

    public void setFileName(String fileName) {
        this.fileName = fileName;

    }

    public void setFileSize(int fileSize) {
        this.fileSize = fileSize;
    }

    public void setFileowner(String username) {
        this.fileowner = username;
    }

    public void setWritable(boolean isWritable) {
        this.isWritable = isWritable;
    }

    // WITHERS

    public FileDTO withFileName(String fileName) {
        setFileName(fileName);
        return this;
    }

    public FileDTO withFileSize(int fileSize) {
        setFileSize(fileSize);
        return this;
    }

    public FileDTO withFileOwner(String owner) {
        setFileowner(owner);
        return this;
    }

    public FileDTO withWriteable(boolean writeable) {
        setWritable(writeable);
        return this;
    }

    @Override
    public String toString() {
        return "FileEntity [ fileName=" + fileName + ", fileSize="
                + fileSize + ", fileowner=" + fileowner + ", is writable=" + isWritable + "]";
    }
}

