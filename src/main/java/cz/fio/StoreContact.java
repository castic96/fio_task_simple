package cz.fio;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.Charset;
import java.util.List;

public class StoreContact extends HttpServlet {

	@Serial
	private static final long serialVersionUID = 1L;
	private static final String FILE_NAME = "contacts.csv";

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		if (areContactParamsValid(req)) {
			String[] newContact = getNewContact(req);
			resp.getOutputStream().print(saveContact(newContact));
		}
		else {
			resp.getOutputStream().print("Contact parameters are invalid!");
		}
	}

	private boolean areContactParamsValid(HttpServletRequest req) {
		return (req.getParameter("firstName") != null)
				&& (req.getParameter("lastName") != null)
				&& (req.getParameter("email") != null)
				&& (req.getParameterMap().size() == 3);
	}

	private String[] getNewContact(HttpServletRequest req) {
		String[] newContact = new String[3];

		newContact[0] = req.getParameter("firstName");
		newContact[1] = req.getParameter("lastName");
		newContact[2] = req.getParameter("email");

		return newContact;
	}

	private String saveContact(String[] newContact) {
		File csvFile = createCsvFile();

		if (csvFile == null) {
			return "Unable to save new record... Error during creating CSV file.";
		}

		List<String[]> existingContacts = getExistingContacts(csvFile);

		if (existingContacts == null) {
			return "Unable to load CSV file.";
		}

		if (existsContact(existingContacts, newContact)) {
			return "Entered contact already exists in CSV file... Skipping...";
		}

		if (!writeNewContactIntoCsv(csvFile, newContact)) {
			return "Unable to save new record into CSV File.";
		}

		return "New contact has been written to CSV file successfully!";
	}

	private boolean existsContact(List<String[]> existingContacts, String[] newContact) {
		String newContactStr = String.join(",", newContact);
		String currentContactStr;

		for (String[] currentContact : existingContacts) {
			currentContactStr = String.join(",", currentContact);

			if (currentContactStr.equals(newContactStr)) {
				return true;
			}

		}

		return false;
	}

	private File createCsvFile() {
		String tempDir = System.getProperty("java.io.tmpdir");

		File csvFile = new File(tempDir + FILE_NAME);

		if (!csvFile.exists()) {
			try {
				csvFile.createNewFile();
			}
			catch (IOException e) {
				return null;
			}
		}

		return csvFile;
	}

	private boolean writeNewContactIntoCsv(File csvFile, String[] newContact) {
		FileWriter fileWriter;
		CSVWriter csvWriter;

		try {
			fileWriter = new FileWriter(csvFile, Charset.forName("Cp1250"), true);
			csvWriter = new CSVWriter(fileWriter);

			csvWriter.writeNext(newContact);

			csvWriter.close();
			fileWriter.close();

			return true;
		}
		catch (IOException e) {
			return false;
		}
	}

	private List<String[]> getExistingContacts(File csvFile) {
		CSVReader csvReader;
		List<String[]> existingData;

		try {
			csvReader = new CSVReader(new FileReader(csvFile, Charset.forName("Cp1250")));
			existingData = csvReader.readAll();
			csvReader.close();

			return existingData;
		}
		catch (IOException | CsvException e) {
			return null;
		}
	}

}
