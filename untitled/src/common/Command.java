package common;

public enum Command {
    HEARTBEAT,
    SCREEN_REQUEST,
    SCREEN_RESPONSE,
    MOUSE_MOVE,
    MOUSE_CLICK,
    KEY_PRESS,
    FILE_REQUEST,
    FILE_RESPONSE,
    FILE_UPLOAD_START,
    FILE_UPLOAD_DATA,
    FILE_UPLOAD_END,
    ERROR,
    OK,
    WEBCAM_RESPONSE,
    WEBCAM_REQUEST,
    GET_WEBCAM_LIST, // Add this command
    WEBCAM_LIST,       // Add this command
    SET_WEBCAM        // Add this command
}