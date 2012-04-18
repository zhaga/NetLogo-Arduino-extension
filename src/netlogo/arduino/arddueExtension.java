package netlogo.arduino;

/** (c) 2004 Uri Wilensky. See README.txt for terms of use. **/


import org.nlogo.api.*;

import java.net.URL;
import java.io.File;
import java.util.Iterator;

public class arddueExtension extends org.nlogo.api.DefaultClassManager
{
    public static arddueController controller ;

	
    public void load( org.nlogo.api.PrimitiveManager primManager )
    {
    // Reset the "sys_paths" field of the ClassLoader to null.
    try {
        final String basedir = new File(this.getClass().getProtectionDomain().getCodeSource().getLocation().getFile()).getParent();
        final String filesep = System.getProperty("file.separator");
        String libdir = basedir + filesep + "lib" + filesep;

        String osname = System.getProperty("os.name");
        if ( osname.equals("Mac OS X") )
        {
            libdir = libdir + osname ;
        }
        else if( osname.startsWith("Windows") )
        {
            libdir = libdir + "Windows";
        }
        else
        {
            final String systype = System.getProperty("os.name") + "-" + System.getProperty("os.arch");
            libdir = libdir + osname + "-" + System.getProperty("os.arch") ;
        }
        org.nlogo.api.JavaLibraryPath.add(new java.io.File(libdir));

        }catch(Exception ex) {
            System.err.println("Cannot add our native libraries to java.library.path: " + ex.getMessage());
        }
        //list of Primitives
        //Note that the first argument is not case sensitive (the string).
        primManager.addPrimitive( "ports",   new arddueListPorts() ) ;
        primManager.addPrimitive( "open",    new arddueOpen() ) ;
        primManager.addPrimitive( "close",   new arddueClose() ) ;
        primManager.addPrimitive( "read",    new arddueRead() ) ;
        primManager.addPrimitive( "write",   new arddueWrite() ) ;
        primManager.addPrimitive( "analog",  new arddueAnalogRead() ) ;
        primManager.addPrimitive( "digital", new arddueDigitalRead() ) ;

        //still testing
        primManager.addPrimitive( "pause",   new ardduePauseThread() ) ;
        primManager.addPrimitive( "stop",    new arddueStopThread() ) ;

        //still testing
        primManager.addPrimitive( "motor",   new arddueMotor() ) ;
        primManager.addPrimitive( "config",  new arddueConfig() ) ;
        primManager.addPrimitive( "get-baud-rate", new getBaudRate() );
    }
	
    public void unload()
    {
        if ( controller != null )
        {
            controller.closePort();
            controller = null ;
        }
        // Since native libraries cannot be loaded in more than one classloader at once
        // and even though we are going dispose of this classloader we can't be sure
        // it will be GC'd before we want to reload this extension, we unload it manually
        // as described here: http://forums.sun.com/thread.jspa?forumID=52&threadID=283774
        // This is a hack, but it works. ev 6/25/09
        try
        {
            ClassLoader classLoader = this.getClass().getClassLoader() ;
            java.lang.reflect.Field field = ClassLoader.class.getDeclaredField( "nativeLibraries" ) ;
            field.setAccessible(true);
            java.util.Vector libs = (java.util.Vector) field.get(classLoader) ;
            for ( Object o : libs )
            {
                java.lang.reflect.Method finalize = o.getClass().getDeclaredMethod( "finalize" , new Class[0] ) ;
                finalize.setAccessible( true ) ;
                finalize.invoke( o , new Object[0] ) ;
            }
        }
        catch( Exception e )
        {
            System.err.println( e.getMessage() ) ;
        }
    }

    public static void ensureardduePort()
		throws ExtensionException
    {
        if ( controller == null || controller.currentPort() == null )
        {
            throw new ExtensionException( "No arddue port open." ) ;
        }
		
    }
    /*
     * initialize controller
     */
    public static void initController( String portName )
    {
        controller = new arddueController( portName ) ;
        // ping to clear out any queued up output
        System.out.println("initializing controller");
       // controller.r = new arddueController.Reader();
    }
    ////////////////////////////////////////////////////////////////////////////
    //Primitive classes
    ////////////////////////////////////////////////////////////////////////////
    public static class getBaudRate extends DefaultReporter
    {
		public Object report(Argument args[], Context context)
			throws ExtensionException , org.nlogo.api.LogoException
		{
			return String.valueOf(controller.BAUD_RATE);
		}
    }
    
    
    public static class arddueListPorts extends DefaultReporter
    {
		public Syntax getSyntax() {
			return Syntax.reporterSyntax(Syntax.ListType()); //in 4.x API syntax this is .TYPE_LIST); 
		}
		
		public Object report(Argument args[], Context context)
			throws ExtensionException , org.nlogo.api.LogoException
		{
			try  {
				LogoListBuilder lb = new LogoListBuilder();
				lb.addAll(arddueController.availablePorts());
				return lb.toLogoList();
				//in 4.x API syntax this is return new org.nlogo.api.LogoList( arddueController.availablePorts() ) ;
			}
			catch ( java.lang.NoClassDefFoundError e )
			{
				throw new ExtensionException(
					"Could not initialize SupCRX Extension.  Please ensure that you have installed RXTX correctly.  Full error message: " + e.getLocalizedMessage() ) ;
			}
		}
    }


    /*
     * Open the arduino port and initialize controller
     */
    public static class arddueOpen extends DefaultCommand    {

        public Syntax getSyntax() {
        	int[] right = { Syntax.StringType() }; //in 4.x API syntax this is.TYPE_STRING } ;
            return Syntax.commandSyntax(right);
        }

        public void perform(Argument args[], Context context)
                throws ExtensionException , org.nlogo.api.LogoException {

          try {
                initController( args[0].getString() ) ;
                System.out.println("Initializing controller");
            }
            catch ( java.lang.NoClassDefFoundError e ) {
                throw new ExtensionException("Could not initialize Arduino duemilanove Extension.  Please ensure that you have installed RXTX correctly.  Full error message: " + args[0].getString() + " : " + e.getLocalizedMessage() ) ;
            }
            catch ( RuntimeException e ){
                throw new ExtensionException( "Could not open port " + args[0].getString() + " : " + e.getLocalizedMessage() ) ;
            }
          try{
              controller.openPort();
          }
          catch(Exception e) {
              System.out.println("Screwed opening port");
              e.printStackTrace();
          }
        }
}
/*
 * Close the arduino port and controller
 */

    public static class arddueClose extends DefaultCommand {

        public Syntax getSyntax() {
            return Syntax.commandSyntax();
        }

        public void perform(Argument args[], Context context)
                throws ExtensionException , org.nlogo.api.LogoException {
            //new thread stuff
            controller.readThread.halt();
            controller.closePort();
            controller = null ;
        }
    }

    /*
     * read data from serial port
     */
    public static class arddueRead extends DefaultReporter
    {
        public Syntax getSyntax() {
        	int ret   = Syntax.StringType(); //in 4.x API syntax this isTYPE_STRING;
            return Syntax.reporterSyntax(ret);
        }

        public Object report(Argument args[], Context context)
            throws ExtensionException , org.nlogo.api.LogoException
        {
            try  {
            	//TODO: ask Zach why he chose to return this data in a list, rather than as a StringType, as suggested in Syntax. CB 4/3/12
            	// WHY, in fact, does this work :) ?
            	LogoListBuilder lb = new LogoListBuilder();
				lb.add( controller.getSerialData() );
				return lb.toLogoList();
            	//in 4.x API syntax this is return new org.nlogo.api.LogoList( controller.getSerialData() ) ;
            }
            catch ( java.lang.NoClassDefFoundError e )
            {
                throw new ExtensionException(
                    "Could not initialize arddue Extension.  Please ensure that you have installed RXTX correctly.  Full error message: " + e.getLocalizedMessage() ) ;
            }
        }
    }
    /*
     * Very basic write command to the serial port.  There is no error checking.
     * So make sure you type it in correctly. 
     */

    public static class arddueWrite extends DefaultCommand
    {

        public Syntax getSyntax() {
        	int[] right = { Syntax.StringType() }; //in 4.x API syntax this is .TYPE_STRING } ;
            return Syntax.commandSyntax(right);
        }

        public void perform(Argument args[], Context context)
                throws ExtensionException , org.nlogo.api.LogoException {

            controller.write(args[0].getString());
        }
    }

    /*
     * Pause the update thread
     */
    public static class ardduePauseThread extends DefaultCommand
    {
        public Syntax getSyntax() {
        	int[] right = { Syntax.StringType() }; //in 4.x API syntax this is .TYPE_STRING } ;TYPE_STRING } ;
            return Syntax.commandSyntax(right);
        }

       public void perform(Argument args[], Context context)
                throws ExtensionException , org.nlogo.api.LogoException {
                    
            controller.readThread.pause();
       }
    }
    /*
     * Halt thread
     */
    public static class arddueStopThread extends DefaultCommand
    {
        public Syntax getSyntax() {
        	int[] right = { Syntax.StringType() }; //in 4.x API syntax this is .TYPE_STRING } ;TYPE_STRING } ;
            return Syntax.commandSyntax(right);
        }

       public void perform(Argument args[], Context context)
                throws ExtensionException , org.nlogo.api.LogoException {
         
           controller.readThread.halt();

        }
    }
/*
 * passes analog data along
 */
    public static class arddueAnalogRead extends DefaultReporter
    {
        public Syntax getSyntax() {
        	int ret   = Syntax.ListType() ; //in 4.x API syntax this is TYPE_LIST;
            return Syntax.reporterSyntax(ret);
        }

        public Object report(Argument args[], Context context)
            throws ExtensionException , org.nlogo.api.LogoException
        {
        	LogoListBuilder lb = new LogoListBuilder();
        	int[] AnaData = controller.getAnalogData();
        	try  {
        		for (int i = 0; i < 6; i++) {
        			lb.add(Double.valueOf(AnaData[i]));
        		}
        		return lb.toLogoList() ;
        	}
//			  IN 4.x API syntax, this is the following....
//            LogoList AnaValues = new LogoList();
//            int[] AnaData = controller.getAnalogData();
//            try  {
//                for (int i = 0; i < 6; i++) {
//                    AnaValues.add(Double.valueOf(AnaData[i]));
//                }
//
//                return new org.nlogo.api.LogoList( AnaValues ) ;
//            }
            catch ( java.lang.NoClassDefFoundError e )
            {
                throw new ExtensionException("error message: " + e.getLocalizedMessage() ) ;
            }
        }
    }
/*
 * passes digital data along
 */
    public static class arddueDigitalRead extends DefaultReporter
    {
        public Syntax getSyntax() {
        	int ret   = Syntax.ListType(); //in 4.x API syntax, this is TYPE_LIST;
            return Syntax.reporterSyntax(ret);
        }

        public Object report(Argument args[], Context context)
            throws ExtensionException , org.nlogo.api.LogoException
        {
        	LogoListBuilder dvals = new LogoListBuilder();
            int[] DigData = controller.getDigitalData();
            try  {
                for (int i = 0; i < 6; i++) {
                    dvals.add(Double.valueOf(DigData[i]));
                }

                return dvals.toLogoList() ;
            }
//			in 4.x API syntax this is:            
//            LogoList DigValues = new LogoList();
//            int[] DigData = controller.getDigitalData();
//            try  {
//                for (int i = 0; i < 6; i++) {
//                    DigValues.add(Double.valueOf(DigData[i]));
//                }
//
//                return new org.nlogo.api.LogoList(DigValues) ;
//            }
            catch ( java.lang.NoClassDefFoundError e )
            {
                throw new ExtensionException("error message: " + e.getLocalizedMessage() ) ;
            }
        }
    }

 /*
  * Writes motor commands to serial port ----TESTING PHASE
  */

 public static class arddueMotor extends DefaultCommand
    {

        public Syntax getSyntax() {
        	int[] right = {Syntax.ListType() }; //in 4.x API syntax, this is TYPE_LIST }  ;
            return Syntax.commandSyntax(right);
        }

       public void perform(Argument args[], Context context)
                throws ExtensionException , org.nlogo.api.LogoException {
           LogoList iter;
           iter = args[0].getList();
           Double val =null;
           for (int i = 0; i< 6; i ++){
               //System.out.println("Testing = "+ iter.get(i));
               val = (Double) iter.get(i);
               controller.MotorArray[i] = val.intValue();
           }
           controller.sendMotor();
        }
    }

public static class arddueConfig extends DefaultCommand
    {
        public Syntax getSyntax() {
        	int[] right = { Syntax.StringType() }; //in 4.x API syntax, this is TYPE_STRING } ;
            return Syntax.commandSyntax(right);
        }

       public void perform(Argument args[], Context context)
                throws ExtensionException , org.nlogo.api.LogoException {
            controller.sendConfig(args[0].getString());
        }
    }

    ////////////////////////////////////////////////////////////////////////////


    public arddueController getController()	{ return controller ; }
	    
}
