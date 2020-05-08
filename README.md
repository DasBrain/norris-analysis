# norris-analysis
Deobfuscation of codesnores.jar

I came accross [this](https://gist.github.com/TheJambo/bf9a9c2002f4b325f544b5a68ad241c3) gist - and wanted to know how it works.

So the journey to deobfuscate that jar started.

I'm quite happy with the results. The only last nitpick is: static final constants are inlined by the java compiler.  
Therefore the original meaning of constants in the code is lost.

I could probably try to match those constants against org.objectweb.Opcodes. Maybe an other time.  

# Conclusion
This was a lot of fun for me.  
The obfuscation is quite primitive, but good enough to make the code hard to follow in a decompiler.  
I automated *most* of the deobfuscation process - so the code could be reused with other jars that are obfuscated in the same way.
