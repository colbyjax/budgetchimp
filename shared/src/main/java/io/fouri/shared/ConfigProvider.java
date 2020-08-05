package io.fouri.shared;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/***
 * Class manages all configuration retrieval
 */
public class ConfigProvider {
    //public static final String rootPath = "resources/";
    private Properties properties;
    private static final Logger logger = LogManager.getLogger(ConfigProvider.class);

    public ConfigProvider() {
        //TODO: Add environment variable pull from System
        properties = this.initialize("dev");
    }

    /**
     * Method to load properties into memory from the configuration file for the given environment
     * @param environment the environment (e.g. dev, uat, prod)
     * @return properties object with key value pairs
     */
    private Properties initialize(String environment) {
        String appConfigPath = environment + ".properties";

        Properties properties = new Properties();

        try {
            logger.debug("Attempting to pull configuration from: " + appConfigPath);
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream(appConfigPath);
            if(inputStream != null) {
                properties.load(inputStream);
                logger.debug("Property File loaded successfully: " + appConfigPath);
            } else {
                logger.error("Property File: " + appConfigPath + " not found in Classpath!");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties;
    }

    /**
     * Accessor method to retrieve any given property from the configuration file
     * @param property the property requested from config file
     * @return the value of the property requested
     */
    public String get(String property) {
        return this.properties.getProperty(property);
    }

}
