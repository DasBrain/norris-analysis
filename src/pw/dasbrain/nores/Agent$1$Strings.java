package pw.dasbrain.nores;
// Decompiled using Fernflower. Too lazy to get the result myself, so I just added a main method.

import java.util.HashMap;

// $FF: synthetic class
public class Agent$1$Strings {
      // $FF: synthetic field
      private static final HashMap<Integer,String> map = new HashMap<>();

      static {
            map.put(3104478, decode("e`hk"));
            map.put(85, decode("U"));
            map.put(-1437791514, decode("`rrdqs"));
            map.put(-1318747143, decode("dwodbs"));
      }

      // $FF: synthetic method
      static final String get(int var0) {
            return (String)map.get(var0);
      }

      // $FF: synthetic method
      private static final String decode(String var0) {
            char[] var1 = var0.toCharArray();

            for(int var2 = 0; var2 < var1.length; ++var2) {
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
