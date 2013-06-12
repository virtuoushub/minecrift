import os, os.path, sys
import shutil, tempfile,zipfile, fnmatch
from optparse import OptionParser
import subprocess

try:
    WindowsError
except NameError:
    WindowsError = OSError

base_dir = os.path.dirname(os.path.abspath(__file__))

#Helpers taken from forge mod loader, https://github.com/MinecraftForge/FML/blob/master/install/fml.py
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
    sys.path.append(mcp_dir)
    os.chdir(mcp_dir)

    reobf = os.path.join(mcp_dir,'reobf','minecraft')
    try:
        shutil.rmtree(reobf)
    except OSError:
        pass

    print("Recompiling...")
    from runtime.mcp import recompile_side, reobfuscate_side
    from runtime.commands import Commands, CLIENT
    commands = Commands(None, verify=True)
    recompile_side( commands, CLIENT)

    print("Reobfuscating...")
    commands.creatergcfg(reobf=True, keep_lvt=True, keep_generics=True, srg_names=False)
    reobfuscate_side( commands, CLIENT )

    version = "0_27"
    out_file = os.path.join( base_dir,"releases","minecrift_"+version+"_classes.zip" )
    try:
        os.remove( out_file)
    except OSError:
        pass
    print("Creating %s"%out_file)
    with zipfile.ZipFile( out_file,'w') as zipout:
        for abs_path, _, filelist in os.walk(reobf, followlinks=True):
            arc_path = os.path.relpath( abs_path, reobf ).replace('\\','/').replace('.','')+'/'
            for cur_file in fnmatch.filter(filelist, '*.class'):
                if cur_file=='bkc.class':
                    continue
                in_file= os.path.join(abs_path,cur_file) 
                arcname =  arc_path + cur_file
                zipout.write(in_file, arcname)




    os.chdir( base_dir )
    
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
