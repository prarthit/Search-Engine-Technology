package cecs429.documents;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Path;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Represents a document that is saved as a simple text file in the local file
 * system.
 */
public class JsonFileDocument implements FileDocument {
	private int mDocumentId;
	private Path mFilePath;
	private String mTitle = null;

	/**
	 * Constructs a JsonFileDocument with the given document ID representing the
	 * file at the given
	 * absolute file path.
	 */
	public JsonFileDocument(int id, Path absoluteFilePath) {
		mDocumentId = id;
		mFilePath = absoluteFilePath;
	}

	@Override
	public Path getFilePath() {
		return mFilePath;
	}

	@Override
	public int getId() {
		return mDocumentId;
	}

	@Override
	public long getByteSize() {
		return mFilePath.toFile().length();
	}

	@Override
	public Reader getContent() {
		JSONParser parser = new JSONParser();

		Object parsedObj;
		try {
			parsedObj = parser.parse(new FileReader(mFilePath.toString()));
			JSONObject parsedJsonObject = (JSONObject) parsedObj;
			String body = (String) parsedJsonObject.get("body");
			return new StringReader(body);
		} catch (IOException | ParseException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String getTitle() {
		if (mTitle == null) {
			JSONParser parser = new JSONParser();
			try {
				Object parsedObj = parser.parse(new FileReader(mFilePath.toString()));
				JSONObject parsedJsonObject = (JSONObject) parsedObj;
				mTitle = (String) parsedJsonObject.get("title");

			} catch (IOException | ParseException e) {
				throw new RuntimeException(e);
			}
		}

		return mTitle;
	}

	public static FileDocument loadJsonFileDocument(Path absolutePath, int documentId) {
		return new JsonFileDocument(documentId, absolutePath);
	}
}
