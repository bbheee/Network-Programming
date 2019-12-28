package common;

import java.io.*;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class FileWorker {

    private static URI GLOBAL_PATH = URI.create("");
    private URI PATH;
    private String fileName;
    private byte[] data;
    private int fileSize = 0;

    public static void setGlobalPath(String path) {
        GLOBAL_PATH = URI.create(path);
    }

    public FileWorker() {
        setFileName("");
    }

    public FileWorker(String fileName) {
        setFileName(fileName);
    }

    public String getFileName() {
        return fileName;
    }

    public int getFileSize() {
        return fileSize;
    }

    public boolean readFile() {
        File file = new File(PATH.toString());
        data = new byte[(int) file.length()];
        fileSize = data.length;
        try {
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
            bis.read(data, 0, data.length);
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean writeFile() {
        if (fileExists()) {
            return false;
        }
        File file = new File(PATH.toString());
        try {
            FileOutputStream fos;
            if (file.createNewFile()) {
                fos = new FileOutputStream(file);
                fos.write(data);
            } else {
                return false;
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean fileExists() {
        File file = new File(PATH.toString());
        return file.exists();
    }

    public void deleteFile() {
        File file = new File(PATH.toString());
        if (file.isFile()) {
            file.delete();
        }
    }

    public boolean sendFile(OutputStream out) {
        if (data == null || data.length == 0) {
            return false;
        }

        // int nameLength | String name | int dataLength | Bytes data
        byte[] nameBytes = fileName.getBytes();
        byte[] nameLength = ByteBuffer.allocate(Integer.BYTES).putInt(nameBytes.length).array();
        byte[] dataLength = ByteBuffer.allocate(Integer.BYTES).putInt(data.length).array();
        try {
            out.write(nameLength);
            out.write(nameBytes);
            out.write(dataLength);
            out.write(data);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean receiveFile(InputStream in) {
        try {
            // Read in filename length
            byte[] indata = in.readNBytes(Integer.BYTES);

            // Read in filename
            int size = ByteBuffer.wrap(indata).getInt();
            byte[] data = new byte[size];
            in.read(data);

            //parse filename
            setFileName(new String(data, StandardCharsets.UTF_8));


            // Read in data length
            indata = in.readNBytes(Integer.BYTES);
            fileSize = ByteBuffer.wrap(indata).getInt();

            // Read in data
            this.data = in.readNBytes(fileSize);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private void setFileName(String fileName) {
        this.fileName = fileName;
        PATH = GLOBAL_PATH.resolve(fileName);
    }
}