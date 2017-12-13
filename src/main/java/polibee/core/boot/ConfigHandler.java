package polibee.core.boot;

import org.springframework.stereotype.Service;
import polibee.core.PolibeeException;

import java.io.*;
import java.util.Properties;

/**
 * Lớp phụ trách quản lý các cấu hình của app, tất cả các cấu hình của app nên được đọc/ghi từ đây
 */
@Service
public class ConfigHandler {
    private Properties _prop;
    private String _configFile;

    public ConfigHandler() {
    }

    /**
     * Init the config handler, we should have our application config in this file
     * @param configFile config handler file
     */
    public void init(String configFile) {
        _prop = new Properties();
        _configFile = configFile;
        InputStream input = null;
        try {
            input = new FileInputStream(configFile);
            _prop.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Init default config file at config/config.properties
     */
    public void init() {
        init("config/config.properties");
    }

    /**
     * Lấy ra giá trị cấu hình
     * @param configName tên cấu hình
     * @return giá trị cấu hình
     */
    public String getConfig(String configName) throws InvalidConfigException {
        if (_prop == null) {
            throw new InvalidConfigException("ConfigHandler has not been loaded");
        }
        return _prop.getProperty(configName);
    }

    /**
     * Lấy giá trị trong cấu hình về, nếu không có trả về default
     * @param configName tên cấu hình
     * @param defaultValue giá trị default
     * @return giá trị của cấu hình
     * @throws InvalidConfigException Cấu hình không hợp lệ
     */
    public String getConfig(String configName, String defaultValue) throws InvalidConfigException {
        if (_prop == null) {
            throw new InvalidConfigException("ConfigHandler has not been loaded");
        }
        return _prop.getProperty(configName, defaultValue);
    }

    /**
     * Thay đổi giá trị cấu hình
     * @param configName tên cấu hình
     * @param value giá trị cần thay đổi thành
     */
    public void setConfig(String configName, String value) throws InvalidConfigException {
        if (_prop == null) {
            throw new InvalidConfigException("ConfigHandler has not been loaded");
        }
        _prop.put(configName, value);
    }

    /**
     * Lưu cấu hình vào file
     */
    public void saveConfig() throws IOException {
        OutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(_configFile);
            _prop.store(outputStream, "store new config");
        } catch (IOException ex) {
            ex.printStackTrace();
            throw ex;
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    throw e;
                }
            }
        }
    }
}

