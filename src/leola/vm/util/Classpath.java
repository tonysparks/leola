package leola.vm.util;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

/**
 * Add Jars to the class path during runtime.
 *
 * @author Tony
 *
 */
public class Classpath {

    /**
     * Do not allow clients to instantiate
     */
    private Classpath() {
    }

    /*
     *  Parameters
     */
    private static final Class<?>[] parameters = new Class<?> [] {
        URL.class
    };


    /**
     * Load a directory full of jars into the class path.
     *
     * @param directory
     */
    public static void loadJars(String directory) throws IOException {
        File dir = new File(directory);
        if ( ! dir.isDirectory() ) {
            throw new IOException(directory + " is not a valid directory!");
        }


        /*
         * Get all the Jars
         */
        List<File> jars = Classpath.getJarFiles(dir, new ArrayList<File>());

        /*
         * Iterate through each jar and add it to the class path
         */
        for ( File jar : jars ) {
            /*
             * Add the jar to the class path
             */
            Classpath.addFile(jar);
        }
    }

    /**
     * Add file to CLASSPATH
     * @param s File name
     * @throws IOException  IOException
     */
    public static void addFile(String s) throws IOException {
        File f = new File(s);
        addFile(f);
    }

    /**
     * Add file to CLASSPATH
     *
     * @param f  File object
     * @throws IOException
     */
    @SuppressWarnings("deprecation")
    public static void addFile(File f) throws IOException {
        addURL(f.toURL());
    }

    /**
     * Add URL to CLASSPATH
     * @param u URL
     * @throws IOException IOException
     */
    public static void addURL(URL u) throws IOException {
        URLClassLoader sysLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        addURL(sysLoader, u);
    }

    public static void addURL(URLClassLoader sysLoader, URL u) throws IOException {
        /*
         * If the Url has not been loaded, load it.
         */
        if ( ! isLoaded(sysLoader, u) ) {

            Class<?> sysclass = URLClassLoader.class;
            try {

                /*
                 * Grab the protected method
                 */
                Method method = sysclass.getDeclaredMethod("addURL", parameters);
                method.setAccessible(true);

                /*
                 * Execute it, effectively adding the url
                 */
                method.invoke(sysLoader, new Object[]{u});

            }
            catch (Throwable t) {
                throw new IOException("Error, could not add " + u +" to system classloader - " + t.getLocalizedMessage());
            }
        }        
    }
    
    /**
     * Determine if the {@link URL} already exists.
     *
     * @param sysLoader
     * @param u
     * @return
     */
    private static boolean isLoaded(URLClassLoader sysLoader, URL u) {

        URL urls[] = sysLoader.getURLs();
        for (int i = 0; i < urls.length; i++) {

            /*
             * If its already been loaded, ignore it
             */
            if (urls[i].toString().equalsIgnoreCase(u.toString())) {
                return true;
            }
        }

        return false;
    }

    /**
     * Recursively get the jar files in a given directory.
     *
     * @param dir
     * @param output
     * @return
     */
    public static List<File> getJarFiles(File dir, List<File> output) {

        /**
         * Get Jars and Folders
         */
        File [] jars = dir.listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                return pathname.isDirectory() || pathname.getName().endsWith(".jar");
            }

        });

        /*
         * Add to the output
         */
        for ( File file : jars ) {

            /*
             * If its a directory recursively add
             * the jars of the child folder
             */
            if ( file.isDirectory() ) {
                getJarFiles(file, output);
            }
            else {

                /*
                 * Add the jar file
                 */
                output.add(file);
            }
        }

        return (output);
    }

}
