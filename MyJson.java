package ECC;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;

// 自实现json toString时按key值排序 json嵌套循环处理
public class MyJson {
    public TreeMap<String, Object> map;

    public MyJson() {
        this.map = new TreeMap<String, Object>(
                new Comparator<String>() {
                    public int compare(String obj1, String obj2) {
                        // 升序排序
                        return obj1.compareTo(obj2);
                    }
                });
    }

    public void put(String key, Object value) {
        map.put(key, value);
    }

    public Object get(String key) {
        return map.get(key);
    }

    public String toString() {
        Set<String> keySet = this.map.keySet();
        Iterator<String> iter = keySet.iterator();

        String res = iterate(iter, "{", this.map) + "}";
        // 去掉倒数前一个","号 且对于{}结构嵌套还有后续字段的情况补上","
        res = res.replace(",}", "}");
        res = res.replace("}\"", "},\"");
        return res;
    }

    // 迭代key拼接字符串，对于json结构嵌套则循环调用
    private String iterate(Iterator<String> iter, String res, TreeMap<String, Object> map) {
        while (iter.hasNext()) {
            String key = iter.next();
            Object value = map.get(key);

            // 类型断言
            if (value instanceof MyJson) {
                // 强转
                MyJson j = (MyJson) value;
                Set<String> keySet = j.map.keySet();
                Iterator<String> it = keySet.iterator();
                res += String.format("\"%s\":", key) + iterate(it, "{", j.map) + "}";
            } else if (value instanceof Integer) {
                res += String.format("\"%s\":%d,", key, value);
            } else if (value instanceof String) {
                res += String.format("\"%s\":\"%s\",", key, value);
            } else if (value instanceof Double || value instanceof Float) { // 实际上小数都会被视为double
                res += String.format("\"%s\":%s,", key, value.toString());
            } // value 尚不支持map set list等其它类型
        }
        return res;
    }

    public static void main(String[] args) {
        MyJson child1 = new MyJson();
        child1.put("key", "value");
        child1.put("app_id", 10086);
        child1.put("phone", "phone");

        MyJson child2 = new MyJson();
        child2.put("user_name", "user_name");
        child2.put("uter_name", "auser_name");
        // 当小数位过多超过double时会自动被截断，除非用decimal 但是像下面那么复杂的用法还不如直接string...
        child2.put("sms_code", 3.141592653512345);
        // 保留这么多个0
        // DecimalFormat df = new DecimalFormat("#0.00000000000000000000000");
        // System.out.println(df.format(3.14159265351111111111));
        child2.put("language", 2);
        child2.put("type", "type");

        MyJson parent = new MyJson();
        parent.put("1key", child1);
        parent.put("2key", child2);
        parent.put("2Key", "hello");

        System.out.println("toString result: " + parent.toString());
    }
}
