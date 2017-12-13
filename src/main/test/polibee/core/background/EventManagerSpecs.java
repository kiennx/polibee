package polibee.core.background;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import polibee.core.PolibeeException;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Event Manager")
public class EventManagerSpecs {

    @Test
    @DisplayName("Có thể thêm event handler theo objectType vào để xử lý")
    public void shouldBeAbleToAddNewHandlerWithoutEventName() throws EventException {
        EventManager eventManager = new EventManager();
        eventManager.addEventHandler("Test", new EventHandler("test", hashMap -> {
            //Do nothing
        }));
    }

    @Test
    @DisplayName("Có thể thêm event handler theo objectType và event Name vào để xử lý")
    public void shouldBeAbleToAddNewHandler() throws EventException {
        EventManager eventManager = new EventManager();
        eventManager.addEventHandler("Test", "onTest", new EventHandler("test", hashMap -> {
            //Do nothing
        }));
    }

    @Test
    @DisplayName("Sẽ ném exception nếu thêm handler lỗi")
    public void shouldThrowExceptionWhenAddInvalidHandler() {
        Executable closureContainingCodeToTest = () -> {
            EventManager eventManager = new EventManager();
            eventManager.addEventHandler("Test", "onTest", null);
        };
        assertThrows(PolibeeException.class, closureContainingCodeToTest);
    }

    @Test
    @DisplayName("Có thể fire một event")
    public void shouldBeAbleToFireAnEvent() {
        EventManager eventManager = new EventManager();
        eventManager.fire("Test", "12", "onTest", new HashMap<>());
    }

    @Test
    @DisplayName("Khi fire một event đã có handler thì sẽ gọi đến handler")
    public void shouldBeAbleToFireAnEventAndExecuteHandler() throws EventException {
        final String[] string = {""};
        EventManager eventManager = new EventManager();
        eventManager.addEventHandler("Test", new EventHandler("test", hashMap -> string[0] = "fired"));

        eventManager.fire("Test", "12", "onTest", null);

        assertEquals("fired", string[0]);
    }

    @Test
    @DisplayName("Khi fire một event đã có handler thì sẽ gọi đến handler đồng thời có đủ các params đi kèm")
    public void shouldBeAbleToFireAnEventAndExecuteHandlerWithParams() throws EventException {
        HashMap<String, Object> params = new HashMap<>();
        params.put("have", "this");

        EventManager eventManager = new EventManager();
        eventManager.addEventHandler("Test", new EventHandler("test",
                hashMap -> assertEquals("this", hashMap.get("have"))));

        eventManager.fire("Test", "12", "onTest", params);
    }

    @Test
    @DisplayName("Khi fire các event đã có handler cho objectType thì sẽ luôn gọi đến handler")
    public void shouldCallHanlderWhenFireObjectTypeEvent() throws EventException {
        final String[] string = {""};
        EventManager eventManager = new EventManager();
        eventManager.addEventHandler("Test", new EventHandler("test", hashMap -> string[0] = "fired"));

        eventManager.fire("Test", "12", "onTest", null);
        assertEquals("fired", string[0]);

        string[0] = "";

        eventManager.fire("Test", "15", "onNotTest", null);
        assertEquals("fired", string[0]);
    }

    @Test
    @DisplayName("Khi fire các event đã có handler cho eventName thì chỉ gọi đến handler khi đúng eventName")
    public void shouldCallHanlderOnlyWhenFireExactEventName() throws EventException {
        final String[] string = {""};
        EventManager eventManager = new EventManager();
        eventManager.addEventHandler("Test", "onTest",
                new EventHandler("test", hashMap -> string[0] = "fired"));

        eventManager.fire("Test", "12", "onTest", null);
        assertEquals("fired", string[0]);

        string[0] = "";

        eventManager.fire("Test", "15", "onNotTest", null);
        assertNotEquals("fired", string[0]);
    }

    //TODO: chưa implement cái này, tạm thời event là tuần tự cũng được
    @DisplayName("Khi fire một event đã có nhiều handler thì handler sẽ được thực thi song song")
    public void shouldBeAbleToFireAnEventAndExecuteMultipleHandler() {
        throw new NotImplementedException();
    }

    @DisplayName("Khi fire một event nhưng handler gặp lỗi thì phải ghi lại việc thực thi lỗi handler")
    public void shouldLogHandlerErrorIfAny() throws EventException {
        //final String[] string = {""};
        EventManager eventManager = new EventManager();
        eventManager.addEventHandler("Test", new EventHandler("test", hashMap -> {
            throw new NotImplementedException();
        }));

//        Consumer<HashMap> testFunction = new Consumer<HashMap>() {
//            @Override
//            public void accept(HashMap hashMap) {
//
//            }
//        };

        eventManager.fire("Test", "12", "onTest", null);

        //Kiểm tra việc ghi log lỗi handler????
        //assertEquals("fired", string[0]);
    }

    @Test
    @DisplayName("Sẽ fire event với thông tin truyền vào qua dữ liệu json string")
    public void shouldFireEventWithJsonString() throws EventException {
        EventManager eventManager = new EventManager();
        final String[] sureFired = new String[1];
        sureFired[0] = "";
        eventManager.addEventHandler("object type", new EventHandler("test", hashMap -> {
            assertEquals("event name", hashMap.get(EventManager.PARAM_EVENT_NAME));
            assertEquals("object type", hashMap.get(EventManager.PARAM_OBJECT_TYPE));
            assertEquals("object id", hashMap.get(EventManager.PARAM_OBJECT_ID));
            assertEquals("test params", hashMap.get("test"));
            sureFired[0] = "fired";
        }));

        eventManager.fire("{\n" +
                "    \"eventName\": \"event name\",\n" +
                "    \"objectType\": \"object type\",\n" +
                "    \"objectId\": \"object id\",\n" +
                "    \"params\": {\n" +
                "      \"test\": \"test params\"\n" +
                "    }\n" +
                "  }");
        assertEquals("fired", sureFired[0]);
    }

//    @DisplayName("Khi fire một event nhưng handler gặp lỗi thì phải ghi lại việc thực thi lỗi handler")
//    public void shouldLogHandlerErrorIfAny() {
//        throw new NotImplementedException();
//    }
}
