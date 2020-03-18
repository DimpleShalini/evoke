package main.java.org.bonitasoft.log.event;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;



public class BEvent {

    /**
     * DEBUG : for debug reason
     * INFO : in a sequence of operation, the INFO level is use to return information on the different step. For example, the method calculate an information,
     * it can be a INFO event
     * SUCCESS : in the sequence of operation, report each success with a SUCCESS level
     * APPLICATIONERROR : this is an error, but due to the external system. Example, the method receive an URL, but this URL is malformed : this is a
     * APPLICATIONERROR, the function can't work with this input
     * ERROR : an internal error. You catch a NullPointerException ? THe function should have an issue, and a ERROR should be reported.
     * CRITICAL : an internal error, but which are critical, and the system should stop.
     */
    public enum Level {
        DEBUG, INFO, SUCCESS, APPLICATIONERROR, ERROR, CRITICAL
    };

    // all fields of Event
    private long mNumber;
    private Level mLevel;
    private String mPackageName;
    private String mTitle;
    /**
     * in case of error, the cause of the error
     */
    private String mCause;
    /** in case of error, the consequence : is the started can't start ? some operation will not be possible ? */
    private String mConsequence;

    /**
     * in case of error, the action to do to fix it
     */
    private String mAction;

    private String mKey;

    public String mExceptionDetails;
    private final BEvent mReferenceEvent; // event reference
    private final String mParameters; // optional parameters

    /* ******************************************************************************** */
    /*                                                                                  */
    /* Constructor of Event */
    /*                                                                                  */
    /*                                                                                  */
    /* ******************************************************************************** */

    //----------------------------------------------------------------------------
    /**
     * Constructor. for error
     *
     * @deprecated use the constructor with the consequence explanation
     */
    @Deprecated
    public BEvent(final String packageName, final long number, final Level level, final String title, final String cause, final String action)
    {
        mNumber = number;
        mLevel = level;
        mPackageName = packageName.trim();
        mTitle = title;
        mCause = cause;
        mAction = action;
        mKey = packageName + "." + number;
        mReferenceEvent = null;
        mParameters = "";

    }

    public BEvent(final String packageName, final long number, final Level level, final String title, final String cause, final String consequence,
            final String action)
    {
        mNumber = number;
        mLevel = level;
        mPackageName = packageName.trim();
        mTitle = title;
        mCause = cause;
        mConsequence = consequence;
        mAction = action;
        mKey = packageName + "." + number;
        mReferenceEvent = null;
        mParameters = "";
    }
    /**
     * constructor for normal event (info, success, debug)
     * @param packageName
     * @param number
     * @param level
     * @param title
     */
    public BEvent(final String packageName, final long number, final Level level, final String title, final String cause)
    {
        mNumber = number;
        mLevel = level;
        mPackageName = packageName.trim();
        mTitle = title;
        mCause = cause;
        mAction = null;
        mKey = packageName + "." + number;
        mReferenceEvent = null;
        mParameters = "";
    }

    /**
     * this is the common constructor in usage of event.
     *
     * @param referenceEvent
     * @param parameters
     */
    public BEvent(final BEvent referenceEvent, final String parameters)
    {
        mReferenceEvent = referenceEvent;
        mParameters = parameters;
    }

    public BEvent(final BEvent referenceEvent, final Exception e, final String parameters)
    {
        mReferenceEvent = referenceEvent;
        mParameters = parameters;
        // this is an error : keep the strack trace !
        final StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        mExceptionDetails = sw.toString();
    }

    /* ******************************************************************************** */
    /*                                                                                  */
    /* Tools */
    /*                                                                                  */
    /*                                                                                  */
    /* ******************************************************************************** */

    /**
     * is this event is consider to be an error ?
     *
     * @return
     */
    public boolean isError()
    {
        final Level level = getLevel();
        if (level == Level.APPLICATIONERROR || level == Level.CRITICAL || level == Level.ERROR) {
            return true;
        }
        return false;
    }


    /**
     * isSame
     * Compare the new event with this one. They are identical when the number / package / parameters are identical.
     */
    public boolean isIdentical(final BEvent compareEvent)
    {
        if ( compareEvent.getNumber() == getNumber()
                && compareEvent.getPackageName().equals( getPackageName())
                && compareEvent.getParameters().equals( getParameters())) {
            return true;
        }
        return false;

    }
    /**
     * log this event, in a reference way
     */
    public void log() {
        final Logger logger = Logger.getLogger(BEvent.class.getName());
        final Level level = getLevel();

        String message = "Event[" + getPackageName() + "~" + getNumber() + "] *" + level.toString() + "* " + getTitle() + " [" + getParameters() + "] -Cause:"
                + getCause();
        if (getConsequence() != null) {
            message += " -Consequence:" + getConsequence();
        }
        if (getAction() != null) {
            message += " -Action:" + getAction();
        }
        if (mExceptionDetails != null) {
            message+=" "+mExceptionDetails;
        }

        if (level == Level.DEBUG) {
            logger.info(message);
        } else if (level == Level.INFO || level == Level.SUCCESS) {
            logger.info(message);
        } else {
            logger.severe(message);
        }
    }

    /* ******************************************************************************** */
    /*                                                                                  */
    /* Generators */
    /*                                                                                  */
    /*                                                                                  */
    /* ******************************************************************************** */
    /**
     * return the Json
     *
     * @return
     */
    public Map<String, Object> getJson(final boolean withHtml)
    {
        final Map<String, Object> json = new HashMap<String, Object>();
        json.put("number", getNumber());
        json.put("level", getLevel().toString());
        json.put("packageName", stringToJson(getPackageName()));
        json.put("title", stringToJson(getTitle()));
        json.put("cause", stringToJson(getCause()));
        json.put("action", stringToJson(getAction()));
        json.put("consequence", stringToJson(getConsequence()));
        json.put("key", getKey());
        json.put("parameters", stringToJson(getParameters()));
        if (withHtml) {
            json.put("html", stringToJson(getHtml()));
        }
        return json;

    }
    public String getHtml()
    {
        final StringBuffer htmlEvent = new StringBuffer();
        htmlEvent.append("<div style=\"border:1px solid black;padding-right: 20px;\"><a href='#' class=\"");
        if (getLevel() == Level.CRITICAL || getLevel() == Level.ERROR) {
            htmlEvent.append("label label-danger");
        } else if (getLevel() == Level.APPLICATIONERROR) {
            htmlEvent.append("label label-warning");
        } else if (getLevel() == Level.SUCCESS) {
            htmlEvent.append("label label-success");
        } else {
            htmlEvent.append("label label-info");
        }
        htmlEvent.append("\" title=\"" + getKey() + "\"");
        htmlEvent.append("\">" + getTitle());
        htmlEvent.append("</a>");

        if (getParameters() != null) {
            htmlEvent.append("<br><span style=\"margin-left:30px;\">" + getParameters() + "</span>");
        if (getCause() != null) {
                htmlEvent.append("<br><span style=\"margin-left:30px;font-style: italic;font-size: 75%;\">Cause: " + getCause() + "</span>");
        }
        }
        if (getConsequence() != null) {
            htmlEvent.append("<br><span style=\"margin-left:30px;font-style: italic;font-weight: bold; font-size: 75%;\">Consequence: " + getConsequence()
                    + "</span>");
        }
        if (getAction() != null) {
            htmlEvent.append("<br><span style=\"margin-left:30px;font-style: italic;font-weight: bold; font-size: 75%;\">Action: " + getAction() + "</span>");
        }
        htmlEvent.append("</div>");

        return htmlEvent.toString();

    }

    /**
     * this method is mainly for debugging
     */
    @Override
    public String toString()
    {
        // don't display the cause and the action, it's mainly for debugging
        return getPackageName() + ":" + getNumber() + " (" + getLevel().toString() + ") " + getTitle() + " " + getParameters();
    }
    /* ******************************************************************************** */
    /*                                                                                  */
    /* getter */
    /*                                                                                  */
    /*                                                                                  */
    /* ******************************************************************************** */

    public long getNumber() {
        return mReferenceEvent == null ? mNumber : mReferenceEvent.getNumber();
    }

    public Level getLevel() {
        return mReferenceEvent == null ? mLevel : mReferenceEvent.getLevel();
    }

    public String getPackageName() {
        return mReferenceEvent == null ? mPackageName : mReferenceEvent.getPackageName();
    }

    public String getTitle() {
        return mReferenceEvent == null ? mTitle : mReferenceEvent.getTitle();
    }

    public String getCause() {
        return mReferenceEvent == null ? mCause : mReferenceEvent.getCause();
    }

    public String getConsequence() {
        return mReferenceEvent == null ? mConsequence : mReferenceEvent.getConsequence();
    }

    public String getAction() {
        return mReferenceEvent == null ? mAction : mReferenceEvent.getAction();
    }

    public String getKey() {
        return mReferenceEvent == null ? mKey : mReferenceEvent.getKey();
    }

    public BEvent getReferenceEvent() {
        return mReferenceEvent;
    }

    public String getParameters() {
        return mParameters != null ? mParameters : mReferenceEvent != null ? mReferenceEvent.getParameters() : null;
    }

    private String stringToJson(final String source)
    {
        if (source == null) {
            return "";
        }
        return source.replaceAll("\"", "\\\"");
    }
}
