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
                // 如果使用%f——String.format("%f", floatNum)——来格式化double数据会出现3.5->3.500000
                // 3.5555555->3.555556这样的情况 因此这里使用%s
                res += String.format("\"%s\":%s,", key, value.toString());
            } else if (value instanceof Object[]) {
                // 如果需要key-value的value是string，在下面第二个%s前后增加\"即可
                res += String.format("\"s\":%s,", parseSlice((Object[]) value));
            } // value 尚不支持map set list等其它类型
        }
        return res;
    }

    // 增加对切片参数的处理
    private String parseSlice(Object[] slice) {
        String fin = "[";
        // 默认format
        String format = "%s,";
        // string切片的format与其它的不同
        if (slice instanceof String[]) {
            format = "\\\"%s\\\",";
        }

        for (int i = 0; i < slice.length; i++) {
            // %s 原因参考上面double/float的处理 int使用%s也正常故统一
            fin += String.format(format, slice[i]);
        }
        fin += "]";
        // 去掉切片中的最后一个,号
        fin = fin.replace(",]", "]");

        return fin;
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

        MyJson sliceChild = new MyJson();
        sliceChild.put("users", new String[] { "user1", "user2", "user3" });
        sliceChild.put("ids", new Integer[] { 1, 2, 3 });
        sliceChild.put("scores", new Double[] { 3.5555555555, 4.2, 3.3 }); // 绩点(bushi

        MyJson parent = new MyJson();
        parent.put("1key", child1);
        parent.put("2key", child2);
        parent.put("slice_child", sliceChild);
        parent.put("2Key", "hello");

        System.out.println("toString result: " + parent.toString());
    }
}
