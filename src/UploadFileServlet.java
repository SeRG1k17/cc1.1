import java.io.*;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;


@WebServlet("/UploadDownloadFileServlet")
@MultipartConfig(fileSizeThreshold=1024*1024*20, // 20MB
		maxFileSize=1024*1024*100,      // 100MB
		maxRequestSize=1024*1024*500)   // 500MB
public class UploadFileServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private static String saveDir = "/Users/SeRG1k/IdeaProjects/Cc1.1/files/";
	private static String saveFileName=null;
	private static String fullPathName=null;
	private static boolean firstTime=true;
    private static RandomAccessFile raf=null;
	private static List<Packet> list=new ArrayList<>();
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String fileName = request.getParameter("fileName");
		if(fileName == null || fileName.equals("")){
			throw new ServletException("File Name can't be null or empty");
		}
		File file = new File(request.getServletContext().getAttribute("FILES_DIR")+File.separator+fileName);
		if(!file.exists()){
			throw new ServletException("File doesn't exists on server.");
		}
		System.out.println("File location on server::"+file.getAbsolutePath());
		ServletContext ctx = getServletContext();
		InputStream fis = new FileInputStream(file);
		String mimeType = ctx.getMimeType(file.getAbsolutePath());
		response.setContentType(mimeType != null? mimeType:"application/octet-stream");
		response.setContentLength((int) file.length());
		response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

		ServletOutputStream os       = response.getOutputStream();
		byte[] bufferData = new byte[1024];
		int read=0;
		while((read = fis.read(bufferData))!= -1){
			os.write(bufferData, 0, read);
		}
		os.flush();
		os.close();
		fis.close();
		System.out.println("File downloaded at client successfully");
	}
	/** **********************************************************
	 *  doPost()
	 ************************************************************ */
	public void doPost (HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException
	{
		ServletOutputStream out = null;
		DataInputStream     in = null;
        OutputStream os=null;

		try
		{  // Try block in case the response object fails
			// Set content type of reponse and get handle to output stream
			res.setContentType ("text/plain");
			out = res.getOutputStream();
		}
		catch (IOException e)
		{  // Print error message to standard out
			System.out.println ("Error getting output stream");
			System.out.println ("Error description: " + e);
			return;
		}

		// Read and unpack the request stream
		try
		{  // Get and check the content type of client request
			String contentType = req.getContentType();

			if (contentType != null && contentType.indexOf ("multipart/form-data") != -1)
			{
				Packet packet=new Packet();
				// Open input stream from client to capture upload file
				in = new DataInputStream (req.getInputStream());

				// Get length of content data
				int formDatalength = req.getContentLength();

				// Allocate a byte array to store content data
				byte dataByte[] = new byte [formDatalength];

				// Read uploaded file into byte array
				int bytesRead = 0;
				int totalBytesRead = 0;
				while (totalBytesRead < formDatalength)
				{  // Read byte-by-byte
					dataByte [totalBytesRead] = in.readByte();
					totalBytesRead ++;
				}

				String uploadFile = new String (dataByte);
				packet.setSize(uploadFile.length());

				dataByte = null;

				int lastIndex   = contentType.lastIndexOf ("=");
				String boundary = contentType.substring (lastIndex+1, contentType.length());

				if(firstTime) {
					saveFileName = uploadFile.substring(uploadFile.indexOf("filename=\"") + 10);
					saveFileName = saveFileName.substring(0, saveFileName.indexOf("\n"));
					saveFileName = saveFileName.substring(saveFileName.lastIndexOf("\\") + 1, saveFileName.indexOf("\""));
				}
				int pos; // Position in upload file-packet
				pos = uploadFile.indexOf ("filename=\"");
				pos = uploadFile.indexOf ("\n", pos) + 1;
				pos = uploadFile.indexOf ("\n", pos) + 1;
				pos = uploadFile.indexOf ("\n", pos) + 1;

				int boundaryLocation = uploadFile.indexOf (boundary, pos) - 4;

				String tempString=uploadFile.substring(pos, boundaryLocation);

				//Find position after "number="
				pos = uploadFile.indexOf("number\"");
				pos = uploadFile.indexOf ("\n", pos) + 1;
				pos = uploadFile.indexOf ("\n", pos) + 1;
				boundaryLocation = uploadFile.indexOf (boundary, pos) - 4;
				packet.setNumber(Integer.parseInt(uploadFile.substring(pos,boundaryLocation)));

				//Find position after "checksum="
				pos = uploadFile.indexOf("checksum\"");
				pos = uploadFile.indexOf ("\n", pos) + 1;
				pos = uploadFile.indexOf ("\n", pos) + 1;
				boundaryLocation = uploadFile.indexOf (boundary, pos) - 4;
				packet.setChecksum(uploadFile.substring(pos,boundaryLocation));

				if (firstTime) {
					fullPathName = new String(saveDir + saveFileName);
                    raf=new RandomAccessFile(new File(fullPathName),"rw");
					System.out.println(raf.getFilePointer());
                    raf.write(tempString.getBytes());
					list.add(packet);
					firstTime=false;
				}
                else {
                    raf=new RandomAccessFile(new File(fullPathName),"rw");
                        int startPos = 0;
                        Packet p;
                        for (int i = 0; i < list.size(); i++) {
                            p = list.get(i);
                            startPos += tempString.length();
                            if (packet.getNumber() < p.getNumber() && packet.getNumber() > 0) {
                                startPos = startPos - p.getData().length();
								raf.seek(startPos);
								System.out.println(raf.getFilePointer());
								raf.write(tempString.getBytes());
                                list.add(i, packet);
								break;
                            }
                            else if ( (i + 1) == list.size()&& (packet.getNumber() != p.getNumber())  )  {
                                    raf.seek(startPos);
								System.out.println(raf.getFilePointer());
                                    raf.write(tempString.getBytes());
                                    list.add(packet);
                                    break;
                            }
                        }
					tempString=null;
                    }
				out.println("Packet was received");
                }
			else
			{  // Request is not multipart/form-data, so we cannot save it
				out.println ("Request not multipart/form-data.");
			}
		}
		catch (Exception e)
		{
			try
			{
				System.out.println ("Error in doPost: " + e);
				out.println ("An unexpected error has occured.");
				out.println ("Error description: " + e);
			}
			catch (Exception f) {}
		}
		finally
		{
			try
			{
				raf.close();
			}
			catch (Exception f) {}
			try
			{
				in.close();
			}
			catch (Exception f) {}
			try
			{
				out.close();
			}
			catch (Exception f) {}
		}
	}
}