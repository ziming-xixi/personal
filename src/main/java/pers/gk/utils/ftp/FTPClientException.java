package pers.gk.utils.ftp;

public class FTPClientException extends Exception {
    private static final long serialVersionUID = 1337965161852998097L;

    public FTPClientException() {}

    public FTPClientException(String message) { super(message); }



    public FTPClientException(String message, Throwable cause) { super(message, cause); }



    public FTPClientException(Throwable cause) { super(cause); }
}
