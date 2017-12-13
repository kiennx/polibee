package polibee.core.background;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Component quản lý event, một event name sẽ có format như sau:
 * ObjectType.EventName
 * Một handler có thể được gán với ObjectType và eventName cụ thể hoặc chỉ ObjectType
 */
@Component
public class EventManager {
    public static final String PARAM_OBJECT_TYPE = "ObjectType";
    public static final String PARAM_OBJECT_ID = "ObjectId";
    public static final String PARAM_EVENT_NAME = "EventName";

    public static final String JSON_OBJECT_TYPE = "objectType";
    public static final String JSON_OBJECT_ID = "objectId";
    public static final String JSON_EVENT_NAME = "eventName";
    public static final String JSON_PARAMS = "params";

    private HashMap<String, List<EventHandler>> _registeredObjectTypeHandler;
    private HashMap<String, List<EventHandler>> _registeredEventHandler;

    private static Logger _logger = LogManager.getLogger(EventManager.class);

    /**
     * Khởi tạo bộ đăng ký các handler
     */
    public EventManager() {
        _registeredEventHandler = new HashMap<>();
        _registeredObjectTypeHandler = new HashMap<>();
    }

    /**
     * Thêm handler xử lý event vào cả objectType
     *
     * @param objectType tên loại đối tượng
     * @param handler    handler xử lý event
     */
    public void addEventHandler(String objectType, EventHandler handler) throws EventException {
        if (handler == null) {
            throw new EventException("Event handler must not be null");
        }

        List<EventHandler> handlers =
                this._registeredObjectTypeHandler.computeIfAbsent(objectType, k -> new ArrayList<>());

        handlers.add(handler);
    }

    /**
     * Thêm handler xử lý event vào theo objectType và eventName
     *
     * @param objectType tên loại đối tượng
     * @param handler    handler xử lý event
     */
    public void addEventHandler(String objectType, String eventName, EventHandler handler) throws EventException {
        if (handler == null) {
            throw new EventException("Event handler must not be null");
        }
        String key = getCompositeKey(objectType, eventName);

        List<EventHandler> handlers = this._registeredEventHandler.computeIfAbsent(key, k -> new ArrayList<>());

        handlers.add(handler);
    }

    /**
     * Fire an event;
     *
     * @param objectType name of the object type
     * @param objectId   object id
     * @param eventName  name of the event
     */
    public void fire(String objectType, String objectId, String eventName, HashMap<String, Object> params) {
        long firedAt = new Date().getTime();
        if (_logger.isDebugEnabled()) {
            String[] data = new String[]{
                    "Event fired", objectType, objectId, eventName, params.toString(), Long.toString (firedAt)
            };
            _logger.debug(String.join(",", data));
        }
        if (params == null) {
            params = new HashMap<>();
        }

        params.put(PARAM_OBJECT_TYPE, objectType);
        params.put(PARAM_OBJECT_ID, objectId);
        params.put(PARAM_EVENT_NAME, eventName);

        List<EventHandler> registered = getEventHandlers(objectType, eventName);
        executeHandlers(registered, params);

        if (_logger.isDebugEnabled()) {
            long now = new Date().getTime();
            String[] data = new String[]{
                    "Event fired successfully", objectType, objectId, eventName, params.toString(), Long.toString(now),
                    Long.toString(now - firedAt)
            };
            _logger.debug(String.join(",", data));
        }
    }

    /**
     * Bắn một event theo chuỗi json string
     *
     * @param jsonString chuỗi json string chứa json object event
     */
    public void fire(String jsonString) {
        JsonParser jsonParser = new JsonParser();
        JsonObject eventObject = (JsonObject) jsonParser.parse(jsonString);
        String objectType = "";
        String objectId = "";
        String eventName = "";
        HashMap<String, Object> params = new HashMap<>();
        if (eventObject.has(JSON_OBJECT_TYPE)) {
            objectType = eventObject.get(JSON_OBJECT_TYPE).getAsString();
        }
        if (eventObject.has(JSON_OBJECT_ID)) {
            objectId = eventObject.get(JSON_OBJECT_ID).getAsString();
        }
        if (eventObject.has(JSON_EVENT_NAME)) {
            eventName = eventObject.get(JSON_EVENT_NAME).getAsString();
        }
        if (eventObject.has(JSON_PARAMS)) {
            JsonElement jsonElement = eventObject.get(JSON_PARAMS);
            if (jsonElement.isJsonObject()) {
                jsonToHashMap(jsonElement.getAsJsonObject(), params);
            }
        }

        fire(objectType, objectId, eventName, params);
    }

    /**
     * Đưa từ JsonObject thành HashMap
     *
     * @param jsonObject đối tượng JsonObject cần chuyển
     * @param hashMap    HashMap sẽ được truyền thông tin từ JsonObject vào
     */
    private void jsonToHashMap(JsonObject jsonObject, HashMap<String, Object> hashMap) {
        for (String key : jsonObject.keySet()) {
            JsonElement element = jsonObject.get(key);
            hashMap.put(key, getFromElement(element));
        }
    }

    /**
     * Lấy giá trị ra từ một JsonElement (về String)
     *
     * @param element JsonElement cần lấy giá trị
     * @return một hashmap nếu là JsonObject, một String nếu là kiểu primitive, null nếu là null, List nếu là array
     */
    private Object getFromElement(JsonElement element) {
        if (element.isJsonNull()) {
            return null;
        } else if (element.isJsonArray()) {
            JsonArray array = element.getAsJsonArray();
            List<Object> list = new ArrayList<>();
            for (JsonElement e : array) {
                list.add(getFromElement(e));
            }
            return list;
        } else if (element.isJsonObject()) {
            HashMap<String, Object> child = new HashMap<>();
            jsonToHashMap(element.getAsJsonObject(), child);
            return child;
        } else if (element.isJsonPrimitive()) {
            return element.getAsString();
        }

        return element;
    }

    /**
     * Lấy về các event handler theo object type và event name
     *
     * @param objectType loại object
     * @param eventName  tên event
     * @return list các event handler
     */
    private List<EventHandler> getEventHandlers(String objectType, String eventName) {
        List<EventHandler> registered = new ArrayList<>();

        if (_registeredObjectTypeHandler.containsKey(objectType)) {
            List<EventHandler> handlers = _registeredObjectTypeHandler.get(objectType);
            registered.addAll(handlers);
        }

        if (_registeredEventHandler.containsKey(getCompositeKey(objectType, eventName))) {
            List<EventHandler> handlers = _registeredEventHandler.get(getCompositeKey(objectType, eventName));
            registered.addAll(handlers);
        }
        return registered;
    }

    /**
     * Lấy key phức hợp của object type và object id để lưu trong hash
     *
     * @param objectType Loại object
     * @param eventName  Tên event
     * @return key phức hợp
     */
    private String getCompositeKey(String objectType, String eventName) {
        return objectType + "." + eventName;
    }

    /**
     * Thực thi một loạt event handler, có thể sau này sẽ cho thực hiện song song qua một threadpool
     *
     * @param registered list các event handler
     * @param params     tham số của event
     */
    private void executeHandlers(List<EventHandler> registered, HashMap params) {
        for (EventHandler handler : registered) {
            handler.execute(params);
        }
    }
}
