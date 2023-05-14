package liuyao.utils.intranet.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 配置文件加载类
 * 优先级：jar包相对路径 > jar包相对路径/config > jar包相对路径/conf > jar包内部
 */
public class PropertiesLoader {

    private static final String DEFAULT_PROP_NAME = "application.properties";
    protected static final Map<String, Properties> PROPS = new ConcurrentHashMap<>();
    private static final String STARTER_PATH = new File("").getAbsolutePath() + File.separator;
    private static final String[] RELATIVE_PATH = {"", "config" + File.separator, "conf" + File.separator};

    public static String getLastFolderFromPath(String path){
        File file = new File(path);
        if (file.isDirectory()) { return file.getAbsolutePath(); }
        while (null != file.getParentFile()){
            file = file.getParentFile();
            if (file.isDirectory()) { return file.getAbsolutePath(); }
        }
        String path1 = path.replaceFirst(file.getPath(), "");
        return getLastFolderFromPath(path1);
    }

    public static Properties getProperties(String propName){
        if (null == PROPS.get(propName)){
            synchronized (PROPS){
                if (null == PROPS.get(propName)){
                    while (propName.startsWith("/")) { propName = propName.substring(1); }
                    while (propName.startsWith("\\")) { propName = propName.substring(1); }
                    File file = new File(STARTER_PATH + propName);
                    for (int i = 0; i < RELATIVE_PATH.length; i++) {
                        if (file.exists()) break;
                        file = new File(STARTER_PATH + RELATIVE_PATH[i] + propName);
                    }
                    InputStream is = null;
                    try {
                        if (file.exists()){
                            // 从jar包相对位置加载
                            System.out.println("load properties：" + file.getAbsolutePath());
                            is = new FileInputStream(file);
                        } else {
                            // 从class 文件处加载文件 jar包内的也可以
                            is = PropertiesLoader.class.getClassLoader().getResourceAsStream(propName);
                            if (is == null) {
                                is = PropertiesLoader.class.getClassLoader().getParent().getResourceAsStream(propName);
                            }
                        }
                        Properties p = new Properties();
                        p.load(is);
                        PROPS.put(propName, p);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            if (null != is) is.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        return PROPS.get(propName);
    }

    public static String getString(String key) {
        return getProperties(DEFAULT_PROP_NAME).getProperty(key);
    }

    public static int getInteger(String key) {
        return Integer.parseInt(getProperties(DEFAULT_PROP_NAME).getProperty(key));
    }

}
