import os, os.path, sys
import zipfile, urllib2
import shutil, tempfile, json
import errno
from hashlib import md5  # pylint: disable-msg=E0611
from optparse import OptionParser
import subprocess

from applychanges import applychanges


base_dir = os.path.dirname(os.path.abspath(__file__))

mc_version = "1.6.2"

try:
    WindowsError
except NameError:
    WindowsError = OSError

def mkdir_p(path):
    try:
        os.makedirs(path)
    except OSError as exc: # Python >2.5
        if exc.errno == errno.EEXIST and os.path.isdir(path):
            pass
        else: raise

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
    
    return True 

def download_deps( mcp_dir ):

    download_file( "http://mcp.ocean-labs.de/files/archive/mcp804.zip", "mcp804.zip" )

    try:
        os.mkdir( mcp_dir )
        mcp_zip = zipfile.ZipFile( "mcp804.zip" )
        mcp_zip.extractall( mcp_dir )
        import stat
        astyle = os.path.join(mcp_dir,"runtime","bin","astyle-osx")
        st = os.stat( astyle )
        os.chmod(astyle, st.st_mode | stat.S_IEXEC)
    except:
        pass

    jars = os.path.join(mcp_dir,"jars")
    repo = "https://s3.amazonaws.com/Minecraft.Download/"

    versions =  os.path.join(jars,"versions",mc_version) 
    mkdir_p( versions )

    if sys.platform == 'darwin':
        native = "osx"
    elif sys.platform == "linux":
        native = "linux"
    elif sys.platform == "linux2":
        native = "linux"
    else:
        native = "windows"

    
    json_file = os.path.join(versions,mc_version+".json")
    json_url = repo + "versions/"+mc_version+"/"+mc_version+".json"

    download_file( json_url, json_file )
    json_obj = []
    with open(json_file,"rb") as f:
        json_obj = json.load( f )
    try:
        newlibs = []
        for lib in json_obj['libraries']:
            skip = False
            if "rules" in  lib:
                for rule in lib["rules"]:
                    if "action" in rule and rule["action"] == "allow" and "os" in rule:
                        skip = True

            if skip:
                continue
            group,artifact,version = lib["name"].split(":")
            url = "libraries/" +group.replace(".","/")+ "/"+artifact+"/"+version +"/"+artifact+"-"+version+".jar"
            file = os.path.join(jars,url.replace("/",os.sep))
            mkdir_p(os.path.dirname(file))
            download_file( repo + url, file )
            if "natives" in lib:
                url = "libraries/" +group.replace(".","/")+ "/"+artifact+"/"+version +"/"+artifact+"-"+version+"-"+lib["natives"][native]+".jar"
                file = os.path.join(jars,url.replace("/",os.sep))
                download_file( repo + url, file )

                folder = os.path.join(jars,"versions",mc_version,mc_version+"-natives")
                mkdir_p(folder)
                zip = zipfile.ZipFile(file)
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
            newlibs.append( lib )
        json_obj['libraries'] = newlibs
        with open(json_file,"wb+") as f:
            json.dump( json_obj,f, indent=1 )
    except:
        pass

    jar_file = os.path.join(versions,mc_version+".jar")
    jar_url = repo + "versions/"+mc_version+"/"+mc_version+".jar"
    download_file( jar_url, jar_file )

    download_file( "http://optifine.net/download.php?f=OptiFine_1.6.2_HD_U_B3.zip", os.path.join(jars,"OptiFine-1.6.2_HD_U_B3.zip"),)

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
    zipmerge( os.path.join( mcp_dir,"jars","versions",mc_version,mc_version+".jar"),
              os.path.join( mcp_dir,"jars","OptiFine-1.6.2_HD_U_B3.zip") )

    print("Decompiling...")
    sys.path.append(mcp_dir)
    os.chdir(mcp_dir)
    from runtime.decompile import decompile
    #         Conf  JAD    CSV    -r     -d     -a     -n     -p     -o     -l     -g     -c    -s     --rg   -w    json  --nocopy
    decompile(None, False, False, False, False, False, False, False, False, False, False, True, False, False, None, None, True  )

    os.chdir( base_dir )

    #src_dir = os.path.join(mcp_dir, "src","minecraft")
    #org_src_dir = os.path.join(mcp_dir, "src",".minecraft_orig")
    #if os.path.exists( org_src_dir ):
    #    shutil.rmtree( org_src_dir, True )
    #shutil.copytree( src_dir, org_src_dir )

    #applychanges( mcp_dir )
    
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
        main(os.path.abspath('mcp804'))    
