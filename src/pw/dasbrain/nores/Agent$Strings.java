package pw.dasbrain.nores;
//Decompiled using Fernflower. Too lazy to get the result myself, so I just added a main method.

import java.util.HashMap;

// $FF: synthetic class
public class Agent$Strings {
    // $FF: synthetic field
    private static final HashMap<Integer, String> map = new HashMap<>();
    
    static {
        map.put(1673725702, decode("i`u`-bk`rr-o`sg"));
        map.put(106148, decode("kha"));
        map.put(1444589, decode("-i`q"));
        map.put(1382100394, decode("-bk`rr"));
        map.put(-494889092, decode("o`sg-rdo`q`snq"));
        map.put(-2060416996, decode("nqf.itmhs.@rrdqs-bk`rr"));
        map.put(-1741207126, decode("i`u`-gnld"));
        map.put(45, decode("-"));
        map.put(46, decode("."));
        map.put(-892381648, decode("nqf-itmhs-@rrdqs"));
    }
    
    // $FF: synthetic method
    static final String get(int var0) {
        return (String) map.get(var0);
    }
    
    // $FF: synthetic method
    private static final String decode(String var0) {
        char[] var1 = var0.toCharArray();
        
        for (int var2 = 0; var2 < var1.length; ++var2) {
            char var3 = var1[var2];
            var3 -= '\uffff';
            var1[var2] = var3;
        }
        
        return String.valueOf(var1);
    }
    
    public static void main(String[] args) {
        for (var e : map.entrySet()) {
            System.out.println(e.getKey() + ", \"" + e.getValue() + "\",");
        }
    }
}
