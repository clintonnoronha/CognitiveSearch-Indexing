package utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FileType {
	
	private List<String> originalHexDB = null;
	private List<String> originalExtDB = null;
	
	public List<String> getOriginalHexDB() {
		return originalHexDB;
	}

	public void setOriginalHexDB(List<String> originalHexDB) {
		this.originalHexDB = originalHexDB;
	}

	public List<String> getOriginalExtDB() {
		return originalExtDB;
	}

	public void setOriginalExtDB(List<String> originalExtDB) {
		this.originalExtDB = originalExtDB;
	}


	public String signature(String path) {
		String extension;
        StringBuilder hex = null;
        String result = "";
        extension = path.substring(path.lastIndexOf(".") + 1);
        File file = new File(path);
        try {
            hex = convertToHex(file);
            if (!extension.equalsIgnoreCase("txt")) {
            	
                result = match(originalHexDB, originalExtDB, hex, extension, file.getName());
            } else {
            	result = "txt";
            } 
        } catch (IOException e) {
            e.printStackTrace();
        }
        
		return result;
    }

	
	public static StringBuilder convertToHex(File file) throws IOException {
		
		long start = System.currentTimeMillis();
        
		try(InputStream is = new FileInputStream(file)) {
			int bytesCounter = 0;
	        int value = 0;
	        StringBuilder sbHex = new StringBuilder();
	        StringBuilder sbResult = new StringBuilder();
	        while ((value = is.read()) != -1) {
	            sbHex.append(String.format("%02X ", value));
	            if (bytesCounter == 15) {
	                sbResult.append(sbHex).append("\n");
	                sbHex.setLength(0);
	                bytesCounter = 0;
	            } else {
	                bytesCounter++;
	            }
	        }
	        if (bytesCounter != 0) {
	            for (; bytesCounter < 16; bytesCounter++) {
	                sbHex.append("   ");
	            }
	            sbResult.append(sbHex).append("\n");
	        }
	        
	        long end = System.currentTimeMillis();
	        
	        System.out.println(end - start);
	        
	        StringBuilder deneme = sbResult;
	        return deneme;
		}
    }
	
	public void getSignature() {
		List<String> hexDB = new ArrayList<>();
		List<String> extDB = new ArrayList<>();
		
		Connection conn;
		
        try {
        	Class.forName("oracle.jdbc.driver.OracleDriver");
        	conn = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:xe", "system", "system");
            String query = "select hex, ext from signaturedb";
            try (conn; PreparedStatement preparedstatement = conn.prepareStatement(query);
            		ResultSet rs = preparedstatement.executeQuery()) {
            	while (rs.next()) {
                    hexDB.add(rs.getString("hex"));
                    extDB.add(rs.getString("ext"));
                }
            	
            	setOriginalHexDB(hexDB);
            	setOriginalExtDB(extDB);
            	
            } catch (SQLException e) {
            	e.printStackTrace();
            }
        } catch (SQLException | ClassNotFoundException e) {
        	e.printStackTrace();
        }
	}
	
	public String match(List<String> hexDB, List<String> extDB, StringBuilder hex, String ext, String file) {
		
		int counter = 0;
		String result = "";
        for (int i = 0; i < hexDB.size(); i++) {
        	String control = hex.substring(0, hexDB.get(i).toString().length());
        	if (control.equalsIgnoreCase(hexDB.get(i).toString())) {
        		if (!extDB.get(i).toString().equalsIgnoreCase(ext)) {
                    System.out.println("\u001b[41mDoesn't Match!!");
                    System.out.println("\u001b[41mReal extension :" + extDB.get(i));
                    result = "TAMPERED";
                } else {
                	System.out.println("\u001b[41mReal extension :" + extDB.get(i));
                    System.out.println("\u001b[42mEverything is OK!! There is no manipulation!!");
                    System.out.println("----------------------------------------------");
                    result = extDB.get(i);
                }
            } else {
                counter++;
            }
        	
            if (counter == hexDB.size()) {
                System.out.println("\u001b[41mThe signature was not found on DB!!");
                System.out.println("--------------------------------------------------");
                result = "NULL";
            }
        }
        return result;
    }

}