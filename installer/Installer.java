import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.beans.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.LineBorder;

/**
 * Derived from https://github.com/MinecraftForge/Installer/
 * Copyright 2013 MinecraftForge developers and Mark Browning
 * 
 * Licensed under GNU LGPL v2.1 or later.
 * 
 * @author mabrowning
 *
 */
public class Installer extends JPanel  implements PropertyChangeListener {
	private static final long serialVersionUID = -562178983462626162L;
	private static final String OF_VERSION = "1.6.2_HD_U_B3";
	private InstallTask task;


    static private File targetDir;
    private JTextField selectedDirText;
    private JLabel infoLabel;
    private JDialog dialog;
    private JPanel fileEntryPanel;
	private Frame emptyFrame;
	private String jar_id;
	private String version;

	class InstallTask extends SwingWorker<Void, Void>{
		private boolean DownloadOptiFine()
		{
			try {
			    File fod = new File(targetDir,"libraries/net/optifine/OptiFine/"+OF_VERSION);
			    fod.mkdirs();
			    File fo = new File(fod,"OptiFine-"+OF_VERSION+".jar");
			    if( !fo.exists() )
			    {
					URL url = new URL("http://optifine.net/download.php?f=OptiFine_"+OF_VERSION+".zip");
				    ReadableByteChannel rbc = Channels.newChannel(url.openStream());
				    FileOutputStream fos = new FileOutputStream(fo);
				    fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);	
			    }
			    return true;
			} catch (Exception e) {
				statusMessage += "Error: "+e.getLocalizedMessage();
			}
			return false;
		}

		private boolean SetupMinecraftAsLibrary() {
			File lib_dir = new File(targetDir,"libraries/net/minecraft/Minecraft/1.6.2/");
			lib_dir.mkdirs();
			File lib_file = new File(lib_dir,"Minecraft-1.6.2.jar");
			if( lib_file.exists())return true;
			try {
				ZipInputStream input_jar = new ZipInputStream(new FileInputStream(new File(targetDir,"versions/1.6.2/1.6.2.jar")));
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
				statusMessage += "Error: "+e.getLocalizedMessage();
			}
			return false;
		}

		private boolean ExtractVersion() {
			if( jar_id != null )
			{
				InputStream version_json =Installer.class.getResourceAsStream("version.json");
				InputStream version_jar = Installer.class.getResourceAsStream("version.jar");
				if( version_jar != null && version_json != null )
				try {
					File ver_dir = new File(new File(targetDir,"versions"),jar_id);
					ver_dir.mkdirs();
					File ver_json_file = new File (ver_dir, jar_id+".json");
					FileOutputStream ver_json = new FileOutputStream(ver_json_file); 
					int d;
					byte data[] = new byte[1024];
					
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
					statusMessage += "Error: "+e.getLocalizedMessage();
				}
				
			}
			return false;
		}

		/*
		 * Main task. Executed in background thread.
		 */
		public String statusMessage;
		@Override
		public Void doInBackground() {
			statusMessage = "Failed: Couldn't download Optifine. ";
			setProgress(0);
			if(!DownloadOptiFine())
			{
				return null;
			}
			setProgress(50);
			statusMessage = "Failed: Couldn't setup Minecraft 1.6.2 as library. ";
			if(!SetupMinecraftAsLibrary())
			{
				return null;
			}
			setProgress(75);
			statusMessage = "Failed: Couldn't extract Minecraft. ";
			if(!ExtractVersion())
			{
				return null;
			}
			statusMessage = "Installed Successfully!";
			setProgress(100);
			return null;
		}

		/*
		 * Executed in event dispatching thread
		 */
		@Override
		public void done() {
			setCursor(null); // turn off the wait cursor
            JOptionPane.showMessageDialog(null, statusMessage, "Complete", JOptionPane.INFORMATION_MESSAGE);
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

        emptyFrame = new Frame("Mod system installer");
        emptyFrame.setUndecorated(true);
        emptyFrame.setVisible(true);
        emptyFrame.setLocationRelativeTo(null);
        dialog = optionPane.createDialog(emptyFrame, "Mod system installer");
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
			InputStream ver =Installer.class.getResourceAsStream("version");
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
        tag = new JLabel("Select Path to minecraft... (default is usually right)");
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
        fileEntryPanel.add(Box.createVerticalGlue());
        fileEntryPanel.add(entryPanel);
        fileEntryPanel.setAlignmentX(CENTER_ALIGNMENT);
        fileEntryPanel.setAlignmentY(TOP_ALIGNMENT);
        this.add(fileEntryPanel);
        updateFilePath();
    }


    private void updateFilePath()
    {
        try
        {
            targetDir = targetDir.getCanonicalFile();
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
}
