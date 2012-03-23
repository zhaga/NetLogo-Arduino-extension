mkdir -p classes # create the classes subfolder if it does not exist

#I think that the >& null dumps output into the nether.

javac -cp .:../../NetLogo.jar:RXTXcomm.jar -d classes src/*.java > /dev/null

jar cvfm arddue.jar manifest.txt -C classes . > /dev/null
