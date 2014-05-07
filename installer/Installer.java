import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.beans.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URI;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.LineBorder;

/**
 * Derived from https://github.com/MinecraftForge/Installer/
 * Copyright 2013 MinecraftForge developers, Mark Browning, StellaArtois
 * 
 * Licensed under GNU LGPL v2.1 or later.
 * 
 * @author mabrowning
 *
 */
public class Installer extends JPanel  implements PropertyChangeListener {
	private static final long serialVersionUID = -562178983462626162L;
	private static final String MC_VERSION = "1.6.4";
	private static final String OF_VERSION = "1.6.4_HD_U_D1";
    private static final String OF_VERSION_EXT = "jar";
    private static final String OF_LIB_PATH = "libraries/optifine/OptiFine/";

	private InstallTask task;

    static private File targetDir;
    private JTextField selectedDirText;
    private JLabel infoLabel;
    private JDialog dialog;
    private JPanel fileEntryPanel;
	private Frame emptyFrame;
	private String jar_id;
	private String version;
	private String mod = "";
	private JCheckBox useForge;
	private JComboBox forgeVersion;
	private JCheckBox useHydra;
	static private final String forgeNotFound = "Forge not found..." ;

	class InstallTask extends SwingWorker<Void, Void>{
		private boolean DownloadOptiFine()
		{
			try {
			    File fod = new File(targetDir,OF_LIB_PATH+OF_VERSION);
			    fod.mkdirs();
			    File fo = new File(fod,"OptiFine-"+OF_VERSION+".jar");
			    if( fo.exists() && fo.length() > 375500 ) return true;

                String surl = "http://optifine.net/download.php?f=OptiFine_"+OF_VERSION+"."+OF_VERSION_EXT;
				URL url = new URL(surl);
			    ReadableByteChannel rbc = Channels.newChannel(url.openStream());
			    FileOutputStream fos = new FileOutputStream(fo);
			    boolean success =  fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE) > 0 ;
			    fos.close();
			    return success;
			} catch (Exception e) {
				finalMessage += " Error: "+e.getLocalizedMessage();
			}
			return false;
		}

		private boolean SetupMinecraftAsLibrary() {
			File lib_dir = new File(targetDir,"libraries/net/minecraft/Minecraft/"+MC_VERSION );
			lib_dir.mkdirs();
			File lib_file = new File(lib_dir,"Minecraft-"+MC_VERSION+".jar");
			if( lib_file.exists() && lib_file.length() > 4500000 )return true; //TODO: should md5sum it here, I suppose
			try {
				ZipInputStream input_jar = new ZipInputStream(new FileInputStream(new File(targetDir,"versions/"+MC_VERSION+"/"+MC_VERSION+".jar")));
				ZipOutputStream lib_jar= new ZipOutputStream(new FileOutputStream(lib_file));
				
				ZipEntry ze = null;
				byte data[] = new byte[1024];
				while ((ze = input_jar.getNextEntry()) != null) {
					if(!ze.isDirectory() && !ze.getName().contains("META-INF"))
					{
						lib_jar.putNextEntry(new ZipEntry(ze.getName()));
						int d;
						while( (d = input_jar.read(data)) != -1 )
						{
							lib_jar.write(data, 0, d);
							
						}
						lib_jar.closeEntry();
						input_jar.closeEntry();
					}
				}
				input_jar.close();
				lib_jar.close();
				return true;
			} catch (Exception e) {
				finalMessage += " Error: "+e.getLocalizedMessage();
			}
			return false;
		}

		private boolean ExtractVersion() {
			if( jar_id != null )
			{
				InputStream version_json; 
				if(useForge.isSelected() && forgeVersion.getSelectedItem() != forgeNotFound ) {
					String filename;
					if( useHydra.isSelected() ) {
						filename = "version-forge.json";
						mod="-forge";
					} else {
						filename = "version-forge-nohydra.json";
						mod="-forge-nohydra";
					}

					version_json = new FilterInputStream( Installer.class.getResourceAsStream(filename) ) {
						public int read(byte[] buff) throws IOException {
							int ret = in.read(buff);
							if( ret > 0 ) {
								String s = new String( buff,0, ret, "UTF-8");
								s = s.replace("$FORGE_VERSION", (String)forgeVersion.getSelectedItem());
								ret = s.length();
								System.arraycopy(s.getBytes("UTF-8"), 0, buff, 0, ret);
							}
							return ret;
						}
						
					};
				} else {
					String filename;
					if( useHydra.isSelected() ) {
						filename = "version.json";
					} else {
						filename = "version-nohydra.json";
						mod="-nohydra";
					}
					version_json = Installer.class.getResourceAsStream(filename);
				}
				jar_id += mod;
				InputStream version_jar =Installer.class.getResourceAsStream("version.jar");
				if( version_jar != null && version_json != null )
				try {
					File ver_dir = new File(new File(targetDir,"versions"),jar_id);
					ver_dir.mkdirs();
					File ver_json_file = new File (ver_dir, jar_id+".json");
					FileOutputStream ver_json = new FileOutputStream(ver_json_file); 
					int d;
					byte data[] = new byte[40960];
					
					// Extract json
	                while ((d = version_json.read(data)) != -1) {
	                    ver_json.write(data,0,d);
	                }
	                ver_json.close();
	                
	                // Extract new lib
					File lib_dir = new File(targetDir,"libraries/com/mtbs3d/minecrift/"+version);
					lib_dir.mkdirs();
					File ver_file = new File (lib_dir, "minecrift-"+version+".jar");
					FileOutputStream ver_jar = new FileOutputStream(ver_file); 
	                while ((d = version_jar.read(data)) != -1) {
	                    ver_jar.write(data,0,d);
	                }
	                ver_jar.close();
	                
	                //Create empty version jar file
	                //All code actually lives in libraries/
					ZipOutputStream null_jar = new ZipOutputStream(new FileOutputStream(new File (ver_dir, jar_id+".jar"))); 
	                null_jar.putNextEntry(new ZipEntry("Classes actually in libraries directory"));
	                null_jar.closeEntry();
	                null_jar.close();
					return ver_json_file.exists() && ver_file.exists();
				} catch (Exception e) {
					finalMessage += " Error: "+e.getLocalizedMessage();
				}
				
			}
			return false;
		}

		/*
		 * Main task. Executed in background thread.
		 */
		public String finalMessage;
		public String statusMessage;
		@Override
		public Void doInBackground() {
			finalMessage = "Failed: Couldn't download Optifine. ";
			statusMessage = "Downloading Optifine... Please donate to them!";
			setProgress(1);
			if(!DownloadOptiFine())
			{
				return null;
			}
			setProgress(50);
			finalMessage = "Failed: Couldn't setup Minecraft "+MC_VERSION+" as library. Have you run "+MC_VERSION+" at least once yet?";
			if(!SetupMinecraftAsLibrary())
			{
				return null;
			}
			setProgress(75);
			finalMessage = "Failed: Couldn't extract Minecrift. Try redownloading this installer.";
			if(!ExtractVersion())
			{
				return null;
			}
			finalMessage = "Installed Successfully! Restart Minecraft and Edit Profile->Use Version minecrift-"+version+mod;
			setProgress(100);
			return null;
		}

		/*
		 * Executed in event dispatching thread
		 */
		@Override
		public void done() {
			setCursor(null); // turn off the wait cursor
            JOptionPane.showMessageDialog(null, finalMessage, "Complete", JOptionPane.INFORMATION_MESSAGE);
	        dialog.dispose();
	        emptyFrame.dispose();
		}

	}
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
        if ("progress" == evt.getPropertyName()) {
            int progress = (Integer) evt.getNewValue();
            System.out.println(progress);
        } 
	}

    public void run()
    {
        JOptionPane optionPane = new JOptionPane(this, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);

        emptyFrame = new Frame("Minecraft VR Installer");
        emptyFrame.setUndecorated(true);
        emptyFrame.setVisible(true);
        emptyFrame.setLocationRelativeTo(null);
        dialog = optionPane.createDialog(emptyFrame, "Minecraft VR Installer");
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setVisible(true);
        int result = (Integer) (optionPane.getValue() != null ? optionPane.getValue() : -1);
        if (result == JOptionPane.OK_OPTION)
        {
        	task = new InstallTask();
        	task.run();
        	task.addPropertyChangeListener(this);
        }
        else{
	        dialog.dispose();
	        emptyFrame.dispose();
        }
    }

	private static void createAndShowGUI() {
        String userHomeDir = System.getProperty("user.home", ".");
        String osType = System.getProperty("os.name").toLowerCase();
        String mcDir = ".minecraft";
        File minecraftDir;

        if (osType.contains("win") && System.getenv("APPDATA") != null)
        {
            minecraftDir = new File(System.getenv("APPDATA"), mcDir);
        }
        else if (osType.contains("mac"))
        {
            minecraftDir = new File(new File(new File(userHomeDir, "Library"),"Application Support"),"minecraft");
        }
        else
        {
            minecraftDir = new File(userHomeDir, mcDir);
        }
        
        Installer panel = new Installer(minecraftDir);
        panel.run();
	}

    private class FileSelectAction extends AbstractAction
    {
		private static final long serialVersionUID = 743815386102831493L;

		@Override
        public void actionPerformed(ActionEvent e)
        {
            JFileChooser dirChooser = new JFileChooser();
            dirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            dirChooser.setFileHidingEnabled(false);
            dirChooser.ensureFileIsVisible(targetDir);
            dirChooser.setSelectedFile(targetDir);
            int response = dirChooser.showOpenDialog(Installer.this);
            switch (response)
            {
            case JFileChooser.APPROVE_OPTION:
                targetDir = dirChooser.getSelectedFile();
                updateFilePath();
                break;
            default:
                break;
            }
        }
    }

    public Installer(File targetDir)
    {
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JPanel logoSplash = new JPanel();
    	logoSplash.setLayout(new BoxLayout(logoSplash, BoxLayout.Y_AXIS));
	    try {
	        BufferedImage image;
			image = ImageIO.read(Installer.class.getResourceAsStream("logo.png"));
	        ImageIcon icon = new ImageIcon(image);
	        JLabel logoLabel = new JLabel(icon);
	        logoLabel.setAlignmentX(CENTER_ALIGNMENT);
	        logoLabel.setAlignmentY(CENTER_ALIGNMENT);
	        logoLabel.setSize(image.getWidth(), image.getHeight());
	        logoSplash.add(logoLabel);
		} catch (IOException e) {
		} catch( IllegalArgumentException e) {
		}
	    

	    version = "UNKNOWN";
		try {
			InputStream ver = Installer.class.getResourceAsStream("version");
			if( ver != null )
			{
				String[] tok = new BufferedReader(new InputStreamReader(ver)).readLine().split(":");
				if( tok.length > 0)
				{
					jar_id = tok[0];
					version = tok[1];
				}
			}
		} catch (IOException e) { }
        JLabel tag = new JLabel("Welcome! This will install Minecraft VR "+ version);
        tag.setAlignmentX(CENTER_ALIGNMENT);
        tag.setAlignmentY(CENTER_ALIGNMENT);
        logoSplash.add(tag);
		logoSplash.add(Box.createRigidArea(new Dimension(5,20)));
        tag = new JLabel("Select path to minecraft. (The default here is almost always what you want.)");
        tag.setAlignmentX(CENTER_ALIGNMENT);
        tag.setAlignmentY(CENTER_ALIGNMENT);
        logoSplash.add(tag);

        logoSplash.setAlignmentX(CENTER_ALIGNMENT);
        logoSplash.setAlignmentY(TOP_ALIGNMENT);
        this.add(logoSplash);
		

        JPanel entryPanel = new JPanel();
        entryPanel.setLayout(new BoxLayout(entryPanel,BoxLayout.X_AXIS));

        Installer.targetDir = targetDir;
        selectedDirText = new JTextField();
        selectedDirText.setEditable(false);
        selectedDirText.setToolTipText("Path to minecraft");
        selectedDirText.setColumns(30);
        entryPanel.add(selectedDirText);
        JButton dirSelect = new JButton();
        dirSelect.setAction(new FileSelectAction());
        dirSelect.setText("...");
        dirSelect.setToolTipText("Select an alternative minecraft directory");
        entryPanel.add(dirSelect);

        entryPanel.setAlignmentX(LEFT_ALIGNMENT);
        entryPanel.setAlignmentY(TOP_ALIGNMENT);
        infoLabel = new JLabel();
        infoLabel.setHorizontalTextPosition(JLabel.LEFT);
        infoLabel.setVerticalTextPosition(JLabel.TOP);
        infoLabel.setAlignmentX(LEFT_ALIGNMENT);
        infoLabel.setAlignmentY(TOP_ALIGNMENT);
        infoLabel.setForeground(Color.RED);
        infoLabel.setVisible(false);

        fileEntryPanel = new JPanel();
        fileEntryPanel.setLayout(new BoxLayout(fileEntryPanel,BoxLayout.Y_AXIS));
        fileEntryPanel.add(infoLabel);
        fileEntryPanel.add(entryPanel);

        fileEntryPanel.setAlignmentX(CENTER_ALIGNMENT);
        fileEntryPanel.setAlignmentY(TOP_ALIGNMENT);
        this.add(fileEntryPanel);
        this.add(Box.createVerticalStrut(5));

		JPanel optPanel = new JPanel();
		optPanel.setLayout( new BoxLayout(optPanel, BoxLayout.Y_AXIS));
        optPanel.setAlignmentX(LEFT_ALIGNMENT);
        optPanel.setAlignmentY(TOP_ALIGNMENT);

        //Add forge options
		JPanel forgePanel = new JPanel();
		forgePanel.setLayout( new BoxLayout(forgePanel, BoxLayout.X_AXIS));
        //Create forge: no/yes buttons
		useForge = new JCheckBox("Install with Forge",false);
		forgeVersion = new JComboBox();

		//Add "yes" and "which version" to the forgePanel
		useForge.setAlignmentX(LEFT_ALIGNMENT);
		forgeVersion.setAlignmentX(LEFT_ALIGNMENT);
		forgePanel.add(useForge);
		forgePanel.add(forgeVersion);
		
		useHydra = new JCheckBox("Include Razer Hydra support",false);
		useHydra.setAlignmentX(LEFT_ALIGNMENT);

		//Add option panels option panel
		forgePanel.setAlignmentX(LEFT_ALIGNMENT);
		optPanel.add(forgePanel);
		optPanel.add(useHydra);
		this.add(optPanel);


        this.add(Box.createVerticalGlue());
		JLabel website = linkify("Minecraft VR is Open Source (LGPL)! Check back here for updates.","http://minecraft-vr.com","http://minecraft-vr.com") ;
		JLabel optifine = linkify("We make use of OptiFine for performance. Please consider donating to them!","http://optifine.net/donate.php","http://optifine.net/donate.php");

		website.setAlignmentX(CENTER_ALIGNMENT);
		optifine.setAlignmentX(CENTER_ALIGNMENT);
		this.add(Box.createRigidArea(new Dimension(5,20)));
		this.add( website );
		this.add( optifine );

        this.setAlignmentX(LEFT_ALIGNMENT);

        updateFilePath();
    }


    private void updateFilePath()
    {
    	String[] forgeVersions = null;
        try
        {
            targetDir = targetDir.getCanonicalFile();
            if( targetDir.exists() ) {
            	File ForgeDir = new File( targetDir, "libraries"+File.separator+"net"+File.separator+"minecraftforge"+File.separator+"minecraftforge");
            	if( ForgeDir.isDirectory() ) {
            		forgeVersions = ForgeDir.list();
            	}
            }
            selectedDirText.setText(targetDir.getPath());
            selectedDirText.setForeground(Color.BLACK);
            infoLabel.setVisible(false);
            fileEntryPanel.setBorder(null);
            if (dialog!=null)
            {
                dialog.invalidate();
                dialog.pack();
            }
        }
        catch (IOException e)
        {

            selectedDirText.setForeground(Color.RED);
            fileEntryPanel.setBorder(new LineBorder(Color.RED));
            infoLabel.setText("<html>"+"Error!"+"</html>");
            infoLabel.setVisible(true);
            if (dialog!=null)
            {
                dialog.invalidate();
                dialog.pack();
            }
        }
        if( forgeVersions == null || forgeVersions.length == 0 )
        	forgeVersions =  new String[] { };
        forgeVersion.setModel( new DefaultComboBoxModel(forgeVersions));
    }

    
	public static void main(String[] args) {


		try {
        	// Set System L&F
	        UIManager.setLookAndFeel(
	        UIManager.getSystemLookAndFeelClassName());
	    } catch (Exception e) { }
		
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});
	}
	public static JLabel linkify(final String text, String URL, String toolTip)
	{
		URI temp = null;
		try
		{
			temp = new URI(URL);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		final URI uri = temp;
		final JLabel link = new JLabel();
		link.setText("<HTML><FONT color=\"#000099\">"+text+"</FONT></HTML>");
		if(!toolTip.equals(""))
			link.setToolTipText(toolTip);
		link.setCursor(new Cursor(Cursor.HAND_CURSOR));
		link.addMouseListener(new MouseListener()
		{
			public void mouseExited(MouseEvent arg0)
			{
				link.setText("<HTML><FONT color=\"#000099\">"+text+"</FONT></HTML>");
			}

			public void mouseEntered(MouseEvent arg0)
			{
				link.setText("<HTML><FONT color=\"#000099\"><U>"+text+"</U></FONT></HTML>");
			}

			public void mouseClicked(MouseEvent arg0)
			{
				if (Desktop.isDesktopSupported())
				{
					try
					{
						Desktop.getDesktop().browse(uri);
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
				else
				{
					JOptionPane pane = new JOptionPane("Could not open link.");
					JDialog dialog = pane.createDialog(new JFrame(), "");
					dialog.setVisible(true);
				}
			}

			public void mousePressed(MouseEvent e)
			{
			}

			public void mouseReleased(MouseEvent e)
			{
			}
		});
		return link;
	}
}
