Oblate
======

Oblate is a Java app that was created in the Netbeans environment.

Latest Version
--------------

You can find the latest stable version of Oblate here:
http://apps.pfrr.alaska.edu/oblate

Development
-----------

Oblate is tested and run with Netbeans 6.8. Simply clone the repository and in Netbeans do 
File > Open Project. Navigate to the Oblate project folder and click Open. You can build
and run the Oblate projects straight from Netbeans.

To release Oblate, simply build the project and copy Oblate.jar from the build folder to your
desired location and run Oblate.jar from there (either via a GUI or the command line):

    $ java -jar Oblate.jar

Distributing
------------

You can distribute the app how ever you wish, but what I use right now is
One-Jar. Check out http://one-jar.sourceforge.net/ and read all the
documentation.

To distribute the app using One-Jar, in NetBeans right click on the `build.xml`
file in your files list and select `Run Target > dist-all`. Oblate will be
built and distributed into the local `dist` folder. Versions for all platforms,
Windows, Mac, Linux, and the original app will be provided.

