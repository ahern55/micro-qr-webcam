package config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.locks.Condition;

public class Configuration {

    private static Configuration INSTANCE;

    public String name;
    public List<RegionCalibration> calibration;

    public static void main(String[] args) {
            Configuration c = getConfiguration();
            System.out.println(c.name);
            System.out.println(c.calibration.get(0).x2);
    }

    public static Configuration getConfiguration() {
        if (INSTANCE == null) {
            try {
                ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
                File file = new File(classLoader.getResource("config.yml").getFile());

                ObjectMapper om = new ObjectMapper(new YAMLFactory());

                INSTANCE = om.readValue(file, Configuration.class);
            }
            catch (IOException e) {
                System.err.println("Unable to read configurations. Make sure the config.yml file exists!");
                System.err.println(e.getMessage());
                System.exit(1);
            }
        }

        return INSTANCE;
    }
}
