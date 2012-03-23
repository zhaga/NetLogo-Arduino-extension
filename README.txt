CONTENTS
========

This package contains the NetLogo Audrino Board extension.

This package also contains some perifierial files and software.  The RXTX package (RXTXcomm.jar) needs to be moved up to the Netlogo/extensions folder, or placed in your classpath.  

//\\If you are using windows, you will be using rxtxSerial.dll.

You will also need to download the Audrino software (version 0022 or greater).  
The software can be downloaded here: 
    http://www.arduino.cc/en/Main/Software 
    
    Remember to install the drivers supplied with the Audrino software.
    
 Once you have the Audrino software installed, you will need to flash your Audrino board with the sketch file supplied in this extension package (extensions/arddue/.

INSTRUCTIONS
============

The Audrino board extension depends upon the RXTX package for
communicating with the Audrino Boards via your computers USB port.  To
install the RXTX package, available at www.rxtx.org.  The Audrino
extension requires version 2.1 or greater, and uses the gnu.io package
(not the javax.comm package).  The RXTXcomm.jar file needs to be in
your CLASSPATH, or in the extensions subdir.  

For models that use this extension, see Code Examples/Audrino
in the models library.

TERMS OF USE
============

All contents (C) 2004-2007 Uri Wilensky.

The contents of this package may be freely copied, distributed, 
altered, or otherwise used by anyone for any legal purpose.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT
OWNERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.


