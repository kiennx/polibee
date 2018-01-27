package polibee.core.background;

import com.google.gson.*;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Lớp mô tả một job cần thực hiện ở background, trong đó mô tả service là beanName để inject lớp thực thi
 * method là phương thức cần thực thi và params để truyền vào các tham số thực thi
 * TODO: cho phép Job có tham số cụ thể thay vì luôn phải là HashMap như hiện tại
 */
public class Job {
    public static final String JSON_SERVICE_BEAN = "service";
    public static final String JSON_METHOD_NAME = "method";
    public static final String JSON_PARAMS = "params";

    private static final Logger LOGGER = LogManager.getLogger(Job.class);

    private ApplicationContext _context;
    private String _service;
    private String _method;
    private HashMap _params;

    /**
     * Khởi tạo một đối tượng Job, cần truyền vào chuỗi JSON thể hiện nội dung job và ApplicationContext sẽ thực thi job
     * @param json Thể hiện nội dung job bao gồm beanName, method và params
     * @param context Context sẽ thực thi Job, nhằm đảm bảo Job có thể load đúng beanName và method
     * @throws JobException Exception khi dữ liệu không chính xác
     */
    public Job(String json, ApplicationContext context) throws JobException {
        _context = context;
        JsonParser jsonParser = new JsonParser();
        JsonElement elm;
        try {
            elm = jsonParser.parse(json);
        }
        catch (JsonSyntaxException ex) {
            LOGGER.error("Job wasnt created because json data is not valid");
            throw new JobException("A job json must be an json object, json syntax error", ex);
        }

        if (!elm.isJsonObject()) {
            LOGGER.error("Job wasnt created because json data is not an object");
            throw new JobException("A job json must be an json object");
        }

        JsonObject jsonObject = elm.getAsJsonObject();
        if (!jsonObject.has(JSON_SERVICE_BEAN)
            || !jsonObject.has(JSON_METHOD_NAME)
            || !jsonObject.has(JSON_PARAMS)) {
            LOGGER.error("Job wasnt created because json data lack some data");
            throw new JobException("A job must have service name (bean), method and params");
        }

        this._service = jsonObject.get(JSON_SERVICE_BEAN).getAsString();
        this._method = jsonObject.get(JSON_METHOD_NAME).getAsString();

        JsonElement paramsElm = jsonObject.get(JSON_PARAMS);
        if (paramsElm.isJsonObject()) {
            Gson gson = new Gson();
            this._params = gson.fromJson(paramsElm, HashMap.class);
        }
        else {
            this._params = new HashMap();
        }
    }

    /**
     * Khởi tạo một job từ thông tin cụ thể
     * @param service Tên bean service
     * @param method Tên phương thức
     * @param params Tham số của job
     * @param context ApplicationContext thực thi job
     */
    public Job(String service, String method, HashMap params, ApplicationContext context) {
        this._service = service;
        this._method = method;
        this._params = params;
        this._context = context;
    }

    /**
     * Thực thi Job
     * @throws JobException Exception trong việc thực thi job này
     */
    public void execute() throws JobException {
        Object obj = getObject();
        Method method = getMethod(obj);

        try {
            method.invoke(obj, this.getParams());
        } catch (IllegalAccessException e) {
            LOGGER.error("Job could not executed because of an exception when invoke method", e);
            throw new JobException("Phương thức thực thi không hợp lệ, không access được phương thức, không thực thi được job", e);
        } catch (InvocationTargetException e) {
            LOGGER.error("Job could not executed because of an exception when invoke method", e);
            throw new JobException("Phương thức thực thi không hợp lệ, không thực thi được job", e);
        }
        catch (Exception e) {
            LOGGER.error("Job executed failed because of job logic", e);
            throw new JobException("Việc thực thi job xảy ra lỗi do bên trong phương thức thực thi job có lỗi", e);
        }
    }

    /**
     * Lấy phương thức sử dụng để thực thi job
     * @param obj Đối tượng sử dụng để thực thi job
     * @return Phương thức sẽ invoke để thực thi job
     * @throws JobException Không tìm thấy phương thức
     */
    private Method getMethod(Object obj) throws JobException {
        Method method = null;
        try {
            Object[] params = this.getParams();
            Class[] classes = new Class[params.length];

            for (int ii = 0; ii < params.length; ii++) {
                classes[ii] = params[ii].getClass();
            }
            method = obj.getClass().getMethod(this._method, classes);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        if (method == null) {
            LOGGER.error(String.format("Job could not executed because could not find method %s", this._method));
            throw new JobException("Không tìm thấy phương thức để thực thi job");
        }
        return method;
    }

    /**
     * Lấy đối tượng dùng để thực thi job từ job data
     * @return Đối tượng (service) dùng để thực thi job
     * @throws JobException Không tìm tím thấy service
     */
    private Object getObject() throws JobException {
        Object obj;
        try {
            obj = _context.getBean(this._service);
        }
        catch (NoSuchBeanDefinitionException ex) {
            LOGGER.error(String.format("Job could not executed because could not find service %s", this._service));
            throw new JobException("Không tìm thấy service (bean) để thực thi job", ex);
        }
        return obj;
    }

    private Object[] getParams() {
        if (this._params.containsKey("params")) {
            Object list = this._params.get("params");
            if (ArrayList.class.isInstance(list)) {
                ArrayList arrayList = (ArrayList) list;
                Object[] result = new Object[arrayList.size()];
                for (int ii = 0; ii < arrayList.size(); ii ++) {
                    Object value = arrayList.get(ii);
                    if (String.class.isInstance(value)) {
                        result[ii] = (String) value;
                    }
                    if (Double.class.isInstance(value)) {
                        result[ii] = (Double) value;
                    }
                    if (Boolean.class.isInstance(value)) {
                        result[ii] = (Boolean) value;
                    }
                }
                return result;
            }
        }
        return new Object[] { this._params};
    }

    /**
     * Chuyển hóa thành chuỗi Json tượng trưng cho job, tất nhiên sẽ không còn context
     * @return Chuỗi json tượng trưng cho job, không bao gồm context
     */
    @Override
    public String toString() {
        return toJsonObject().toString();
    }

    /**
     * Chuyển hóa job thành JsonObject tượng trưng cho job
     * @return Đối tượng JsonObject tượng trưng cho job, không bao gồm context
     */
    public JsonObject toJsonObject() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty(JSON_SERVICE_BEAN, this._service);
        jsonObject.addProperty(JSON_METHOD_NAME, this._method);
        Gson gson = new Gson();
        jsonObject.add(JSON_PARAMS, gson.toJsonTree(this._params));

        return jsonObject;
    }
}
