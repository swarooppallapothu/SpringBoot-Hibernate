package app.util;

import org.hibernate.criterion.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;

import javax.servlet.http.HttpServletRequest;

public final class Util {

    private Util() {
    }

    @SuppressWarnings("unchecked")
    public static <T> Class<T> getFirstGenericParameter(Class<?> clazz) {
        Type t = clazz;
        while (t instanceof Class<?>) {
            t = ((Class<?>) t).getGenericSuperclass();
        }
        if (t instanceof ParameterizedType) {
            for (Type param : ((ParameterizedType) t).getActualTypeArguments()) {
                if (param instanceof Class<?>) {
                    Class<T> c = null;
                    if (param instanceof Class<?>) {
                        c = (Class<T>) param;
                    }
                    if (c != null) {
                        return c;
                    }
                } else if (param instanceof TypeVariable) {
                    for (Type paramBound : ((TypeVariable<?>) param).getBounds()) {
                        if (paramBound instanceof Class<?>) {
                            Class<T> c = null;
                            if (paramBound instanceof Class<?>) {
                                c = (Class<T>) paramBound;
                            }
                            if (c != null) {
                                return c;
                            }
                        }
                    }
                }
            }
        }
        throw new IllegalStateException("Cannot determine type parameter for " + clazz.getName());
    }

    public static int getObjectId(Object object) {
        if (object == null) {
            throw new IllegalStateException("cannot read id of null object");
        }
        Method method;
        try {
            method = object.getClass().getMethod("getId", (Class<?>[]) null);
        } catch (NoSuchMethodException | SecurityException e) {
            throw new IllegalStateException("object has no getId method", e);
        }
        Object idObject;
        try {
            idObject = method.invoke(object, (Object[]) null);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new RuntimeException("cannot invoke getId on object", e);
        }
        if (!(idObject instanceof Integer)) {
            throw new RuntimeException("object getId() returned non-Integer.");
        }
        return (Integer) idObject;
    }

    public static int getObjectVersion(Object object) {
        if (object == null) {
            throw new IllegalStateException("cannot read id of null object");
        }
        Method method;
        try {
            method = object.getClass().getMethod("getVersion", (Class<?>[]) null);
        } catch (NoSuchMethodException | SecurityException e) {
            throw new IllegalStateException("object has no getVersion method", e);
        }
        Object versionObject;
        try {
            versionObject = method.invoke(object, (Object[]) null);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new RuntimeException("cannot invoke getVersion on object", e);
        }
        if (!(versionObject instanceof Integer)) {
            throw new RuntimeException("object getVersion() returned non-Integer.");
        }
        return (Integer) versionObject;
    }

    /**
     * Return the currently logged-in user, or null if no user is logged in in
     * the current context.
     */
/*    public static User getCurrentUser() {
        // Provide the currently logged-in user.
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }
        Object principalObject = authentication.getPrincipal();
        if (principalObject == null || (!(principalObject instanceof CustomUserDetails))) {
            return null;
        } else {
            CustomUserDetails userDetails = (CustomUserDetails) principalObject;
            return userDetails.getUser();
        }
    }*/

    /**
     * Return the remote host of the currently logged-in user, or null if it
     * cannot be determined.
     */
/*    public static String getCurrentHost() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }
        WebAuthenticationDetails details = (WebAuthenticationDetails) authentication.getDetails();
        if (details == null) {
            return null;
        }
        return details.getRemoteAddress();
    }*/

    /**
     * Determine if the specified request contains an application/json body.
     */
    public static boolean isJsonRequest(HttpServletRequest request) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set(HttpHeaders.CONTENT_TYPE, request.getContentType());
        if (MediaType.APPLICATION_JSON.equals(httpHeaders.getContentType())) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Determine if a JSON response should be used for this request. If the HTTP
     * Accept header of the request contains "application/json" and does not
     * contain "text/html", then this method returns true. Otherwise, this
     * method returns false.
     *
     * Note that accept-params such as the relative quality factor ("q") are not
     * regarded.
     */
    public static boolean useJsonResponse(HttpServletRequest request) {
        String acceptHeaderValue = request.getHeader("Accept");
        if (acceptHeaderValue == null) {
            return false;
        }
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.ACCEPT, acceptHeaderValue);
        List<MediaType> acceptedMediaTypes = httpHeaders.getAccept();
        boolean acceptsHtml = acceptedMediaTypes.contains(MediaType.TEXT_HTML);
        boolean acceptsJson = acceptedMediaTypes.contains(MediaType.APPLICATION_JSON);
        if (acceptsJson && (!acceptsHtml)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Pretty-print a dump of this object, recursively parsing any lists, sets,
     * maps, and properties.
     *
     * @param object
     * @return
     */
    public static String dump(Object object) {
        return dump(object, null, 0);
    }

    private static final String indent(int indent) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < indent; i++) {
            sb.append("    ");
        }
        return sb.toString();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static String dump(Object object, Set<Integer> visited, int indent) {
        if (visited == null) {
            visited = new HashSet<Integer>();
        } else if (object != null) {
            int identity = System.identityHashCode(object);
            if (visited.contains(identity)) {
                return "XXX";
            }
            visited.add(identity);
        }

        StringBuilder sb = new StringBuilder();
        if (object == null) {
            sb.append("null");
        } else if (object instanceof String) {
            sb.append("\"" + (String) object + "\"");
        } else if (object instanceof Number) {
            sb.append((Number) object);
        } else if (object instanceof List) {
            List<Object> list = (List<Object>) object;
            int size = list.size();
            if (list.isEmpty()) {
                return "[]";
            }
            sb.append("[\n");
            int count = 0;
            for (Object o : (List) object) {
                count++;
                sb.append(indent(indent + 1));
                sb.append(dump(o, visited, indent + 1));
                if (count < size) {
                    sb.append(',');
                }
                sb.append('\n');
            }
            sb.append("]");
        } else if (object instanceof Set) {
            Set<Object> set = (Set<Object>) object;
            int size = set.size();
            if (set.isEmpty()) {
                return "<>";
            }
            sb.append("<\n");
            int count = 0;
            for (Object o : set) {
                count++;
                sb.append(indent(indent + 1));
                sb.append(dump(o, visited, indent + 1));
                if (count < size) {
                    sb.append(',');
                }
                sb.append("\n");
            }
            sb.append(indent(indent));
            sb.append('>');
        } else if (object instanceof Map) {
            Map<Object, Object> map = (Map<Object, Object>) object;
            int size = map.size();
            if (map.isEmpty()) {
                return "{}";
            }
            sb.append("{\n");
            int count = 0;
            for (Map.Entry<Object, Object> entry : map.entrySet()) {
                count++;
                sb.append(indent(indent + 1));
                sb.append(dump(entry.getKey(), visited, indent + 1));
                sb.append(": ");
                sb.append(dump(entry.getValue(), visited, indent + 1));
                if (count < size) {
                    sb.append(',');
                }
                sb.append('\n');
            }
            sb.append(indent(indent));
            sb.append("}");
        } else {
            // Is this an entity object (does it have a getId(...) method for any argument type?)
            boolean hasGetId = false;
            Method[] methods = object.getClass().getMethods();
            for (Method method : methods) {
                if (method.getName().equals("getId"));
                hasGetId = true;
            }

            // If this is an entity object, convert it to a map and then render the map.
            if (hasGetId) {
                Map<String, Object> map = new HashMap<String, Object>();
                PropertyDescriptor[] pds;
                try {
                    pds = Introspector.getBeanInfo(object.getClass(), Object.class)
                            .getPropertyDescriptors();
                } catch (IntrospectionException e) {
                    e.printStackTrace();
                    return "ERR";
                }
                for (PropertyDescriptor pd : pds) {
                    Method readMethod = pd.getReadMethod();
                    if (readMethod != null) {
                        Object value = null;
                        boolean success = false;
                        try {
                            value = readMethod.invoke(object);
                            success = true;
                        } catch (Throwable t) {
                            // best-effort only
                        }
                        if (success) {
                            map.put(pd.getName(), value);
                        }
                    }
                }
                sb.append(dump(map, visited, indent));
            } else {
                // If this is not an entity object, simply render with toString().
                return object.toString();
            }
        }
        return sb.toString();
    }

    private static final int DATE_TIME_DELIMITATION_INDEX = 10;

    /**
     * Parse an ISO-8601 string to a LocalDateTime assumed to be in UTC. This
     * method aims for maximum flexibility by trying to parse the string as a
     * date-time, just a date, or a date-time with a time zone.
     *
     * @throws DateTimeParseException if the string could not be parsed.
     */
    public static LocalDateTime dateTimeFromISO8601(String string) {
        if (string == null) {
            throw new DateTimeParseException("ISO-8601 string is null.", "", 0);
        }

        // If the date-time is specified with a space separating the date and time,
        // convert the space to 'T' so that it is proper ISO-8601.  This tolerance is
        // needed to support PostgreSQL-style times.
        if (string.length() > DATE_TIME_DELIMITATION_INDEX
                && string.charAt(DATE_TIME_DELIMITATION_INDEX) == ' ') {
            char[] characters = string.toCharArray();
            characters[DATE_TIME_DELIMITATION_INDEX] = 'T';
            string = new String(characters);
        }

        // If the date-time is specified with a two-digit time zone offset instead of
        // an offset in the form [+-]HH:MM, then convert to the latter.  This tolerance
        // is needed to support PostgreSQL-style times.
        int length = string.length();
        if (length > 3) {
            char sign = string.charAt(length - 3);
            if (sign == '+' || sign == '-') {
                char d1 = string.charAt(length - 2);
                char d2 = string.charAt(length - 1);
                if (d1 >= '0' && d1 <= '9' && d2 >= '0' && d2 <= '9') {
                    string = string + ":00";
                }
            }
        }

        LocalDateTime dateTime;
        try {
            dateTime = LocalDateTime.parse(string);
        } catch (DateTimeParseException e1) {
            try {
                LocalDate date = LocalDate.parse(string);
                dateTime = LocalDateTime.of(date, LocalTime.MIDNIGHT);
            } catch (DateTimeParseException e2) {
                try {
                    ZonedDateTime zonedDateTime = ZonedDateTime.parse(string);
                    dateTime = LocalDateTime.ofInstant(
                            zonedDateTime.toInstant(), ZoneOffset.UTC);
                } catch (DateTimeParseException e3) {
                    throw new DateTimeParseException(
                            "DateTime not provided in any supported ISO-8601 format.", string, 0);
                }
            }
        }
        return dateTime;
    }

    /**
     * Convert a point-in-time (expressed in milliseconds since the epoch) to a
     * LocalDateTime.
     */
    public static LocalDateTime dateTimeFromMilliseconds(long milliseconds) {
        return LocalDateTime.ofInstant(
                Instant.ofEpochMilli(milliseconds), ZoneOffset.UTC);
    }

    /**
     * Convert a point-in-time (expressed in milliseconds since the epoch) to a
     * LocalDateTime.
     */
    public static LocalDateTime dateTimeFromMilliseconds(Number milliseconds) {
        return LocalDateTime.ofInstant(
                Instant.ofEpochMilli(milliseconds.longValue()), ZoneOffset.UTC);
    }

    /**
     * Try to parse a DateTime from a variety of object types.
     */
    public static LocalDateTime dateTimeFromObject(Object object) {
        // validate correct type
        LocalDateTime dateTime;
        if (object instanceof String) {
            // parse the string
            String string = (String) object;
            try {
                dateTime = Util.dateTimeFromISO8601(string);
            } catch (DateTimeParseException e) {
                throw new DateTimeParseException(
                        "DateTime not provided in any supported ISO-8601 format.", string, 0);
            }
        } else if (object instanceof LocalDateTime) {
            // use the data as-is.
            dateTime = (LocalDateTime) object;
        } else if (object instanceof Number) {
            dateTime = Util.dateTimeFromMilliseconds((Number) object);
        } else {
            throw new DateTimeParseException(
                    "expected String, LocalDateTime, or Number object; "
                    + "received " + object.getClass().getSimpleName(), "", 0);
        }
        return dateTime;
    }

    /**
     * @params HttpServletRequest request
     * @returns Order object or null
     *
     */
    public static Order getSortingList(HttpServletRequest request) {
        Order order = null;

        if (request != null) {
            String fieldName = request.getParameter("customSortParam");
            String dir = request.getParameter("dir");
            if (fieldName != null && !fieldName.isEmpty()
                    && dir != null && !dir.isEmpty()) {
                if (dir.toLowerCase().equals("desc")) {
                    order = Order.desc(fieldName);
                } else {
                    order = Order.asc(fieldName);
                }
            }
        }

        return order;
    }

    public static String getClientIpAddress(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-FORWARDED-FOR");
        if (ipAddress != null) {
            ipAddress = ipAddress.split(",")[0];
        } else {
            ipAddress = request.getRemoteAddr();
        }

        return ipAddress;
    }

}
