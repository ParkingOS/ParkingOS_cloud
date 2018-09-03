package parkingos.com.bolink.utlis;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;


public class Defind {
	
	private static Logger logger = Logger.getLogger(Defind.class);
	private static Map<String, String> config_map = new HashMap<String, String>();
	static {
		load();
		logger.info("config init complete:"+config_map);
	}
	
	private static void load() {
		InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("config.properties");
		Properties p = new Properties();
		try {
			p.load(is);
			for (Map.Entry<Object,Object> e : p.entrySet()) {
				config_map.put((String) e.getKey(), (String) e.getValue());
			}
		} catch (IOException e) {
			logger.fatal("load property file failed", e);
		}
	}
	
	public static String getProperty(String key) {
		if (StringUtils.isBlank(key)) {
			return null;
		}
		return config_map.get(key);
	}
	
	public static Map<String,String> reloadConfig() {
		config_map.clear();
		load();
		logger.info("config reload complete:" + config_map);
		return config_map;
	}
	
	public static Map<String, String> getMap (){
		return config_map;
	}
	
}
