package polibee.core.boot;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Unit-Test lớp ConfigHandler")
public class ConfigHandlerSpecs {
    @Test()
    @DisplayName("ném Exception khi lấy cấu hình khi chưa khởi tạo")
    public void shouldThrowExceptionWhenGetConfigButNotInitialized() {
        Executable closureContainingCodeToTest = ()-> {
            ConfigHandler configHandler = new ConfigHandler();
            configHandler.getConfig("someConfigName");
        };
        assertThrows(InvalidConfigException.class, closureContainingCodeToTest);
    }

    @Test
    @DisplayName("ném Exception khi ghi cấu hình khi chưa khởi tạo")
    public void shouldThrowExceptionWhenSetConfigButNotInitialized() {
        Executable closureContainingCodeToTest = ()-> {
            ConfigHandler configHandler = new ConfigHandler();
            configHandler.setConfig("someConfigName", "someValue");
        };
        assertThrows(InvalidConfigException.class, closureContainingCodeToTest);
    }

    @Test
    @DisplayName("trả về giá trị cấu hình sau khi khởi tạo và set")
    public void shouldReturnConfigValueAfterInitAndSet() throws InvalidConfigException {
        ConfigHandler configHandler = new ConfigHandler();
        configHandler.init();

        configHandler.setConfig("test", "value");
        String value = configHandler.getConfig("test");
        assertEquals("value", value);
    }

    @Test
    @DisplayName("trả về giá trị mặc định nếu chưa có giá trị")
    public void shouldReturnDefaultValueIfDoNotHasValue() throws InvalidConfigException {
        ConfigHandler configHandler = new ConfigHandler();
        configHandler.init();

        String value = configHandler.getConfig("test", "default");
        assertEquals("default", value);
    }

    @Test
    @DisplayName("trả về giá trị đối với cấu hình 2 cấp (có dấu .)")
    public void shouldReturnValueWith2LevelConfig() throws InvalidConfigException {
        ConfigHandler configHandler = new ConfigHandler();
        configHandler.init();

        String value = configHandler.getConfig("test.config", "default");
        assertEquals("default", value);
    }

    @Test
    @DisplayName("ghi được vào file properties")
    public void shouldBeAbleToWriteToPropertiesFile() throws InvalidConfigException, IOException {
        ConfigHandler configHandler = new ConfigHandler();
        configHandler.init("write.properties");

        configHandler.setConfig("application.schedulerPool", "50");
        configHandler.saveConfig();

        configHandler.init("write.properties");
        String value = configHandler.getConfig("application.schedulerPool");
        assertEquals("50", value);
    }

    @Test
    @DisplayName("đọc được từ file properties")
    public void shouldBeAbleToReadFromPropertiesFile() throws InvalidConfigException {
        ConfigHandler configHandler = new ConfigHandler();
        configHandler.init("test.properties");

        String value = configHandler.getConfig("application.schedulerPool", "50");
        assertEquals("10", value);
    }
}
