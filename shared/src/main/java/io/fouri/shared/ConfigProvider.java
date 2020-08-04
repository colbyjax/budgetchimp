package io.fouri.shared;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/***
 * Class manages all configuration retrieval
 */
public class ConfigProvider {
    private Properties properties;

    public ConfigProvider() {
        //TODO: Add environment variable pull from System
        properties = this.initialize("dev");
    }

    public String get(String property) {
        return this.properties.getProperty(property);
    }

    /**
     * Load the correct Config file for the environment and return Properties
     */
    private Properties initialize(String environment) {
        String rootPath = Thread.currentThread().getContextClassLoader().getResource("").getPath();
        String appConfigPath = rootPath + environment + ".config";
        Properties appProps = new Properties();

        try {
            appProps.load(new FileInputStream(appConfigPath));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return appProps;
    }

}
