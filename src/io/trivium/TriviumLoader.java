package io.trivium;

import io.trivium.glue.TriviumObject;
import io.trivium.anystore.AnyClient;
import io.trivium.anystore.query.Query;
import io.trivium.anystore.query.Value;
import io.trivium.anystore.statics.ContentTypes;
import io.trivium.anystore.statics.TypeIds;
import io.trivium.extension._e53042cbab0b4479958349320e397141.FileType;
import io.trivium.extension._e53042cbab0b4479958349320e397141.FileTypeFactory;
import javolution.util.FastList;
import javolution.util.FastMap;
import org.jetbrains.annotations.Nullable;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

/**
 * Our custom implementation of the ClassLoader.
 * For any of classes from "javablogging" package
 * it will use its {@link TriviumLoader#getClass()}
 * method to load it from the specific .class file. For any
 * other class it will use the super.loadClass() method
 * from ClassLoader, which will eventually pass the
 * request to the parent.
 *
 */
public class TriviumLoader extends ClassLoader {

    private FastMap<String,Class<?>> classes = new FastMap<String,Class<?>>();
    /**
     * Parent ClassLoader passed to this constructor
     * will be used if this ClassLoader can not resolve a
     * particular class.
     *
     * @param parent Parent ClassLoader
     *              (may be from getClass().getClassLoader())
     */
    public TriviumLoader(ClassLoader parent) {
        super(parent);
    }

    /**
     * Loads a given class from .class file just like
     * the default ClassLoader. This method could be
     * changed to load the class over network from some
     * other server or from the database.
     *
     * @param name Full class name
     */
    private Class<?> getClass(String name)
            throws ClassNotFoundException {
        // We are getting a name that looks like
        // javablogging.package.ClassToLoad
        // and we have to convert it into the .class file name
        // like javablogging/package/ClassToLoad.class
        try {
//            Central.logger.debug("TriviumLoader getClass '{}'", name);
        }catch(Exception ex){}
        String file = name.replace('.', File.separatorChar)
                + ".class";
        byte[] b = null;
        try {
            // This loads the byte code data from the file
            b = loadClassData(file);
            if((b==null || b.length==0 ) && Central.isRunning){
                //load from anystore
//                Central.logger.debug("loading class from anystore '{}'",name);
                Query query = new Query();
                query.criteria.add(new Value("canonicalName", name));
                query.criteria.add(new Value("typeId", TypeIds.FILE.toString()));
                query.criteria.add(new Value("contentType", ContentTypes.getMimeType("class")));
                FastList<TriviumObject> objects = AnyClient.INSTANCE.loadObjects(query);
                FileTypeFactory factory = new FileTypeFactory();
                for(TriviumObject po : objects){
                    FileType memFile = factory.getInstance(po);
//                    Central.logger.info("found class {}",memFile.metadata.findValue("canonicalName"));
                    b = Base64.getDecoder().decode(memFile.data);
                }
            }
            // defineClass is inherited from the ClassLoader class
            // and converts the byte array into a Class
            Class<?> c = defineClass(name, b, 0, b.length);
            resolveClass(c);
            return c;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Nullable
    @Override
    public URL getResource(String name) {
        try{
//            Central.logger.debug("TriviumLoader getResource '{}'",name);
        }catch(Exception ex){}
        return super.getResource(name);
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        Enumeration<URL> orgResult = super.getResources(name);
        Vector<URL> result = new Vector<URL>();
        while(orgResult.hasMoreElements()){
            result.add(orgResult.nextElement());
        }
        if(name.startsWith("META-INF/services/")){
            //enrich with anystore
//            Central.logger.debug("loading class from anystore '{}'",name);
            Query query = new Query();
            query.criteria.add(new Value("name", name));
            query.criteria.add(new Value("typeId", TypeIds.FILE.toString()));
            FastList<TriviumObject> objects = AnyClient.INSTANCE.loadObjects(query);
            FileTypeFactory factory = new FileTypeFactory();
            for(TriviumObject po : objects){
                String uri = "anystore://"+po.getId().toString();
                URL url = new URL(uri);
//                Central.logger.debug("adding url '{}'",uri);
                result.add(url);
            }
        }
        try{
//            Central.logger.debug("TriviumLoader getResources '{}'",name);
        }catch(Exception ex){}
        return result.elements();
    }

    @Override
    public InputStream getResourceAsStream(String name) {
        try{
//            Central.logger.debug("TriviumLoader getResourceAsStream '{}'",name);
        }catch(Exception ex){}
        return super.getResourceAsStream(name);
    }

    public static Enumeration<URL> getSystemResources(String name)
            throws IOException
    {
        try{
//            Central.logger.debug("TriviumLoader getSystemResources '{}'",name);
        }catch(Exception ex){}
        return ClassLoader.getSystemResources(name);
    }

    public Class<?> fromBytes(String name, byte[] input){
        try{
            try{
//            Central.logger.debug("TriviumLoader fromBytes '{}'",name);
            }catch(Exception ex){}
            Class<?> c = defineClass(name,input,0,input.length);
            return c;
        }catch(Exception ex){
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * Every request for a class passes through this method.
     * If the requested class is in "javablogging" package,
     * it will load it using the
     * {@link TriviumLoader#getClass()} method.
     * If not, it will use the super.loadClass() method
     * which in turn will pass the request to the parent.
     *
     * @param name
     *            Full class name
     */
    @Override
    public Class<?> loadClass(String name)
            throws ClassNotFoundException {
        try{
//        Central.logger.debug("TriviumLoader loadClass '{}'",name);
            //look up anystore
            if(Central.isRunning) {
                if(classes.containsKey(name)){
                    return classes.get(name);
                }
                Query query = new Query();
                query.criteria.add(new Value("canonicalName", name));
                query.criteria.add(new Value("typeId", TypeIds.FILE.toString()));
                query.criteria.add(new Value("contentType","application/java-vm"));
                FastList<TriviumObject> objects = AnyClient.INSTANCE.loadObjects(query);
                FileTypeFactory factory = new FileTypeFactory();
//                Central.logger.info("found {} entries",objects.size());
                for(TriviumObject po : objects){
                    FileType file = factory.getInstance(po);
//                    Central.logger.info("found {} {}",file.name,file.contentType);
                    if(file.contentType.equals(ContentTypes.getMimeType("class"))
                            && file.name.replace('/','.').equals(name + ".class")) {
                        byte[] bytes = Base64.getDecoder().decode(file.data);
//                        Central.logger.debug("TriviumLoader returning class definition '{}'", name);
                        Class<?> c= defineClass(name, bytes, 0, bytes.length);
                        classes.put(name,c);
                        return c;
                    }
                }
            }
        }catch(Exception ex){}
       // System.out.println("loading class '" + name + "'");
        if (name.startsWith("javablogging.")) {
            return getClass(name);
        }
        return super.loadClass(name);
    }

    /**
     * Loads a given file (presumably .class) into a byte array.
     * The file should be accessible as a resource, for example
     * it could be located on the classpath.
     *
     * @param name File name to load
     * @return Byte array read from the file
     * @throws IOException Is thrown when there
     *               was some problem reading the file
     */
    private byte[] loadClassData(String name) throws IOException {
        // Opening the file
        InputStream stream = getClass().getClassLoader()
                .getResourceAsStream(name);
        int size = stream.available();
        byte buff[] = new byte[size];
        DataInputStream in = new DataInputStream(stream);
        // Reading the binary data
        in.readFully(buff);
        in.close();
        return buff;
    }
}