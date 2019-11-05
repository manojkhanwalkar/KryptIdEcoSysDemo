package logger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IdLogger {

    private static final org.slf4j.Logger logger
            = LoggerFactory.getLogger(IdLogger.class);


    public  static Logger getLogger()
    {
        return logger;
    }



}
