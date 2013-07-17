import os, os.path, sys
import urllib, zipfile, urllib2
import shutil, tempfile
from hashlib import md5  # pylint: disable-msg=E0611
from optparse import OptionParser
import subprocess

from applychanges import applychanges

try:
    WindowsError
except NameError:
    WindowsError = OSError

base_dir = os.path.dirname(os.path.abspath(__file__))

#Helpers taken from forge mod loader, https://github.com/MinecraftForge/FML/blob/master/install/fml.py
def get_md5(file):
    if not os.path.isfile(file):
        return ""
    with open(file, 'rb') as fh:
        return md5(fh.read()).hexdigest()

def download_file(url, target, md5=None):
    name = os.path.basename(target)
    
    if not os.path.isfile(target):
        try:
            with open(target,"wb") as tf:
                res = urllib2.urlopen(urllib2.Request( url, headers = {"User-Agent":"Mozilla/5.0"}))
                tf.write( res.read() )
            if not md5 == None:
                if not get_md5(target) == md5:
                    print 'Download of %s failed md5 check, deleting' % name
                    os.remove(target)
                    return False
            print 'Downloaded %s' % name
        except Exception as e:
            print e
            print 'Download of %s failed, download it manually from \'%s\' to \'%s\'' % (target, url, target)
            return False
    else:
        print 'File Exists: %s' % os.path.basename(target)
    return True
    
def download_native(url, folder, name):
    if not os.path.exists(folder):
        os.makedirs(folder)
    
    target = os.path.join(folder, name)
    if not download_file(url, target):
        return False
    
    zip = zipfile.ZipFile(target)
    for name in zip.namelist():
        if not name.startswith('META-INF') and not name.endswith('/'):
            out_file = os.path.join(folder, name)
            if not os.path.isfile(out_file):
                print '    Extracting %s' % name
                out = open(out_file, 'wb')
                out.write(zip.read(name))
                out.flush()
                out.close()
    zip.close()
    return True 

def download_deps( mcp_dir ):

    download_file( "http://mcp.ocean-labs.de/files/archives/mcp751.zip", "mcp751.zip" )

    try:
        os.mkdir( mcp_dir )
        mcp_zip = zipfile.ZipFile( "mcp751.zip" )
        mcp_zip.extractall( mcp_dir )
        import stat
        astyle = os.path.join(mcp_dir,"runtime","bin","astyle-osx")
        st = os.stat( astyle )
        os.chmod(astyle, st.st_mode | stat.S_IEXEC)
    except:
        pass

    if sys.platform == 'darwin':
        shutil.copy("minecraft_ff_osx.patch.crlf",os.path.join(mcp_dir,"conf","patches","minecraft_ff.patch"))
    jars = os.path.join(mcp_dir,"jars")
    bin = os.path.join(jars,"bin")

    native = "natives-window.jar"
    if sys.platform == 'darwin':
        native = "natives-osx.jar"
    elif sys.platform == "linux":
        native = "natives-linux.jar"

    MinecraftDownload = "https://s3.amazonaws.com/Minecraft.Download/libraries/org/lwjgl/lwjgl/lwjgl-platform/2.9.0/lwjgl-platform-2.9.0-"
    download_native( MinecraftDownload + native, os.path.join(bin,"natives"), "lwjgl-"+native )
    MinecraftDownload = "https://s3.amazonaws.com/Minecraft.Download/libraries/net/java/jinput/jinput-platform/2.0.5/jinput-platform-2.0.5-"
    download_native( MinecraftDownload+ native, os.path.join(bin,"natives"), "jinput-"+native )

    MinecraftDownload = "https://s3.amazonaws.com/Minecraft.Download/libraries/"
    download_file( MinecraftDownload + "net/java/jinput/jinput/2.0.5/jinput-2.0.5.jar", os.path.join(bin,"jinput.jar" ))
    download_file( MinecraftDownload + "org/lwjgl/lwjgl/lwjgl/2.9.0/lwjgl-2.9.0.jar", os.path.join(bin,"lwjgl.jar" ))
    download_file( MinecraftDownload + "org/lwjgl/lwjgl/lwjgl_util/2.9.0/lwjgl_util-2.9.0.jar", os.path.join(bin,"lwjgl_util.jar" ))

    MinecraftDownload = "http://s3.amazonaws.com/Minecraft.Download/versions/"
    download_file( MinecraftDownload+ "1.5.2/1.5.2.jar", os.path.join(bin,"minecraft.jar"), "6897c3287fb971c9f362eb3ab20f5ddd" )
    #download_file( MinecraftDownload+ "1.5.2/minecraft_server.1.5.2.jar", os.path.join(jars,"minecraft_server.jar"),"c4e1bf89e834bd3670c7bf8f13874bc6" ) 

    download_file( "http://optifine.net/download.php?f=OptiFine_1.5.2_HD_U_D3.zip", os.path.join(bin,"optifine.zip"), "de96e9633842957bf2c25cc59151e3e1" )

def zipmerge( target_file, source_file ):
    out_file, out_filename = tempfile.mkstemp()
    out = zipfile.ZipFile(out_filename,'a')
    target = zipfile.ZipFile( target_file, 'r')
    source = zipfile.ZipFile( source_file, 'r' )

    #source supersedes target
    source_files = set( source.namelist() )
    target_files = set( target.namelist() ) - source_files

    for file in source_files:
        out.writestr( file, source.open( file ).read() )

    for file in target_files:
        out.writestr( file, target.open( file ).read() )

    source.close()
    target.close()
    out.close()
    os.remove( target_file )
    shutil.copy( out_filename, target_file )
   
    
def symlink(source, link_name):
    import os
    os_symlink = getattr(os, "symlink", None)
    if callable(os_symlink):
        try:
            os_symlink(source, link_name)
        except Exception:
			pass
    else:
        import ctypes
        csl = ctypes.windll.kernel32.CreateSymbolicLinkW
        csl.argtypes = (ctypes.c_wchar_p, ctypes.c_wchar_p, ctypes.c_uint32)
        csl.restype = ctypes.c_ubyte
        flags = 1 if os.path.isdir(source) else 0
        if csl(link_name, source, flags) == 0:
            raise ctypes.WinError()

def main(mcp_dir):
    print 'Using mcp dir: %s' % mcp_dir
    print 'Using base dir: %s' % base_dir
    print("Downloading dependencies...")
    download_deps( mcp_dir )

    print("Applying Optifine...")
    zipmerge( os.path.join( mcp_dir,"jars","bin","minecraft.jar"),
              os.path.join( mcp_dir,"jars","bin","optifine.zip") )

    print("Decompiling...")
    sys.path.append(mcp_dir)
    os.chdir(mcp_dir)
    from runtime.decompile import decompile
    #         Conf  JAD    CSV    -r     -d     -a     -n     -p     -o     -l     -g     -c     -s
    decompile(None, False, False, False, False, False, False, False, False, False, False, True, False )

    os.chdir( base_dir )

    src_dir = os.path.join(mcp_dir, "src","minecraft")
    org_src_dir = os.path.join(mcp_dir, "src",".minecraft_orig")
    if os.path.exists( org_src_dir ):
        shutil.rmtree( org_src_dir, True )
    shutil.copytree( src_dir, org_src_dir )

    applychanges( mcp_dir )
    
	#Need git in system PATH!
    try:
        process = subprocess.Popen(["git","submodule","init"], cwd=base_dir, bufsize=-1)
        process.communicate()

        process = subprocess.Popen(["git","submodule","update"], cwd=base_dir, bufsize=-1)
        process.communicate()
    except:
        print("You'll need to get the JRift and Sixense-Java git submodules manually! Then, copy the jar files to mcp/lib")
        pass

    try:
        os.mkdir( os.path.join( mcp_dir, "lib" ) )
    except WindowsError:
        pass	

    try:
        symlink( os.path.join( base_dir, "JRift","JRift.jar"), os.path.join( mcp_dir, "lib" ,"JRift.jar") )
        symlink( os.path.join( base_dir, "Sixense-Java","SixenseJava.jar"), os.path.join( mcp_dir, "lib" ,"SixenseJava.jar") )
    except WindowsError:
        pass
    
if __name__ == '__main__':
    parser = OptionParser()
    parser.add_option('-m', '--mcp-dir', action='store', dest='mcp_dir', help='Path to MCP to use', default=None)
    options, _ = parser.parse_args()

    if not options.mcp_dir is None:
        main(os.path.abspath(options.mcp_dir))
    elif os.path.isfile(os.path.join('..', 'runtime', 'commands.py')):
        main(os.path.abspath('..'))
    else:
        main(os.path.abspath('mcp'))	
