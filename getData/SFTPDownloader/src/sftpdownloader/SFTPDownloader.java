package sftpdownloader;

import datatosql.Utilities;
import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.Selectors;
import org.apache.commons.vfs2.impl.StandardFileSystemManager;
import org.apache.commons.vfs2.provider.sftp.SftpFileSystemConfigBuilder;

public class SFTPDownloader
{

    static Properties props;

    public static void main(String[] args)
    {

        SFTPDownloader getMyFiles = new SFTPDownloader();
        if (args.length < 1)
        {
            System.err.println("Usage: java " + getMyFiles.getClass().getName()
                    + " Properties_filename");
            System.exit(1);
        }

        String propertiesFilename = args[0];

        getMyFiles.startFTP(propertiesFilename);

    }

    public boolean startFTP(String propertiesFilename)
    {

        props = new Properties();
        StandardFileSystemManager manager = new StandardFileSystemManager();

        try
        {

            props.load(new FileInputStream(propertiesFilename));
            String serverAddress = props.getProperty("serverAddress").trim();
            String userId = props.getProperty("userId").trim();
            String password = props.getProperty("password").trim();
            String remoteDirectory = props.getProperty("remoteDirectory").trim();
            String localDirectory = props.getProperty("localDirectory").trim();
            //Initializes the file manager
            manager.init();

            //Setup our SFTP configuration
            FileSystemOptions opts = new FileSystemOptions();
            SftpFileSystemConfigBuilder.getInstance().setStrictHostKeyChecking(opts, "no");
            SftpFileSystemConfigBuilder.getInstance().setUserDirIsRoot(opts, true);
            SftpFileSystemConfigBuilder.getInstance().setTimeout(opts, 10000);

            //Create the SFTP URI using the host name, userid, password,  remote path and file name
            String sftpUri = "sftp://" + userId + ":" + Utilities.encodeURIComponent(password) + "@" + serverAddress + "/"
                    + remoteDirectory;

            FileObject localFileObject = manager.resolveFile(sftpUri);
            FileObject[] children = localFileObject.getChildren();
            for (FileObject children1 : children)
            {

                String fileName = children1.getName().getBaseName();
                // Create local file object
                String filepath = localDirectory + fileName;
                File file = new File(filepath);
                FileObject localFile = manager.resolveFile(file.getAbsolutePath());

                // Create remote file object
                String sftpUriFile = sftpUri + fileName;
                FileObject remoteFile = manager.resolveFile(sftpUriFile, opts);

                // Copy local file to sftp server
                localFile.copyFrom(remoteFile, Selectors.SELECT_SELF);
                System.out.println(filepath + " - File download successful");
            }
            

        } catch (Exception ex)
        {
            System.out.println(ex.getMessage());
            return false;
        } finally
        {
            manager.close();
        }

        return true;
    }

}
