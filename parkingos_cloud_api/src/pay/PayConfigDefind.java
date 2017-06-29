package pay;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.ibatis.common.resources.Resources;

public class PayConfigDefind {
	
	private static Map<String, String> config_map = new HashMap<String, String>();
	
	static {
		init();
	}
	
	public static void init(){
		String fileName ="aliconfig.properties";//÷ß∏∂±¶≈‰÷√
		Properties properties = new Properties();
		try {
			File file1 = Resources.getResourceAsFile(fileName);
			properties.load(new FileInputStream(file1));
			for (Map.Entry<Object,Object> e : properties.entrySet()) {
				config_map.put((String) e.getKey(), (String) e.getValue());
			}
			fileName ="weixinconfig.properties";//Œ¢–≈≈‰÷√
			File file2 = Resources.getResourceAsFile(fileName);
			properties.clear();
			properties.load(new FileInputStream(file2));
			for (Map.Entry<Object,Object> e : properties.entrySet()) {
				config_map.put((String) e.getKey(), (String) e.getValue());
			}
			System.err.println("Œ¢–≈£¨÷ß∏∂±¶≈‰÷√£∫"+config_map);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static String getValue(String key){
		if(config_map.containsKey(key))
			return config_map.get(key);
		else 
			return "";
	}
}
