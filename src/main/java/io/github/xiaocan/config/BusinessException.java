package io.github.xiaocan.config;

public class BusinessException extends RuntimeException {

    private final String message;

    private final Integer code;

    public BusinessException(String message) {
        this(500, message);
    }
    public BusinessException(Integer code, String message) {
        super(message);
        this.message = message;
        this.code = code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
