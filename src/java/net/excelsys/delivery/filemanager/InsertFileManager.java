package net.excelsys.delivery.filemanager;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

public class InsertFileManager {

	private static final String _RESULT_PATH = "/home/ftroncoso/workspace/producto2/ScriptProyectos/";

	private static final String _READ_PATH = "/home/ftroncoso/Descargas/excelsys/filemanager/independiente/";

	private StringBuilder _fileLines;

	public InsertFileManager() {

	}

	public static void main(String[] args) {
		InsertFileManager ifm = new InsertFileManager();
		ifm.process();
	}

	public void process() {
		try {
			File[] f = finder(_READ_PATH);
			for (int i = 0; i < f.length; i++) {
				List<String> lines = readLines(f[i]);
				String header = lines.get(0);
				String[] headerArray = header.split(";");
				String prefix = headerArray[0];
				String desc = headerArray[0];
				String resultFile = headerArray[0];
				lines.remove(0);
				processLines(lines, prefix, desc, resultFile);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public File[] finder(String dirName) {
		File dir = new File(dirName);

		return dir.listFiles(new FilenameFilter() {

			public boolean accept(File dir, String filename) {
				return filename.endsWith(".csv");
			}
		});

	}

	private List<String> readLines(File file) throws IOException {
		List<String> lines = FileUtils.readLines(file, "UTF-8");
		return lines;
	}

	private void addLine(String text) {
		addLine(text + "\n");
	}

	private void processLines(List<String> lines, String prefix, String desc,
			String resultFile) {
		_fileLines = new StringBuilder();
		addLine("DECLARE");
		addLine("");
		declareMasks();
		addLine("");
		int count = 0;
		for (count = 0; count < lines.size(); count++) {
			addLine("	idValidatorInstance" + count + "	NUMBER(19) := 0;");
		}

		addLine("");
		addLine("	idValidationSetup	NUMBER(19) := 0;");
		addLine("	idTemplateFile		NUMBER(19) := 0;");
		addLine("	idDelimiterType		NUMBER(19) := 0;");
		addLine("	idTemplateParser	NUMBER(19) := 0;");
		addLine("	idTemplateFileType	NUMBER(19) := 0;");

		String[] tmpArray;
		String tmpString;
		count = 0;
		for (String str : lines) {
			tmpArray = str.split(";");
			if (tmpArray[0].startsWith("SECTION_")) {
				tmpString = tmpArray[0].split("_")[1];
				tmpString = tmpString.toLowerCase();
				addLine("	idFM" + tmpString + "			NUMBER(19) := 0;");
				addLine("	seccion_" + count + "			NUMBER(19) := 0;");
				count++;
			}
		}

		addLine("");
		addLine("	idValidatorTypeDate		NUMBER(19)  := 0;");
		addLine("	idValidatorTypeNumber	NUMBER(19)  := 0;");
		addLine("	idValidatorTypeString	NUMBER(19)  := 0;");
		addLine("");
		addLine("BEGIN");
		addLine("");

		// ------------------------------------------------------------------------------------------------------
		// TODO:
		String codeTemplateFileType = prefix;// "BDR"; //Ej: "ANM"
		String descriptionTemplateFileType = desc; // Ej:
													// "Apertura Nominas Masivas"
		String codeValidationSetup = "SETUP-" + prefix; // Ej: "SETUP-ANM"
		String descriptionValidationSetup = "Setup de " + desc; // Ej:
																// "Setup de Nominas Masivas"
		// ------------------------------------------------------------------------------------------------------

		addLine("");
		addLine("	----------------------------------------------INICIO TEMPLATE FILE TYPE----------------------------------------------");
		addLine("	INSERT INTO FM_TEMPLATE_FILE_TYPE(ID,TYPE_CODE,DESCRIPTION,NAME,JMS_DESTINATION_NAME, TEMPLATE_LOAD_TYPE_ID) "
				+ "VALUES(FM_TEMPLATE_FILE_TYPE_SQ.NEXTVAL,'"
				+ codeTemplateFileType
				+ "','"
				+ descriptionTemplateFileType
				+ "','"
				+ descriptionTemplateFileType
				+ "','jms/fileManagerQueue',1);");
		addLine("	----------------------------------------------FIN TEMPLATE FILE TYPE----------------------------------------------");
		addLine("");
		addLine("	-----------------------------------------------INICIO VALIDATION SETUP----------------------------------------------");
		addLine("	SELECT VL_VALIDATION_SETUP_SQ.NEXTVAL INTO idValidationSetup FROM DUAL;");
		addLine("	INSERT INTO VL_VALIDATION_SETUP(ID, E_CODE, DESCRIPTION, E_NAME) "
				+ "VALUES (idValidationSetup, '"
				+ codeValidationSetup
				+ "', '"
				+ descriptionValidationSetup
				+ "', '"
				+ codeValidationSetup
				+ "');");
		addLine("	-----------------------------------------------FIN VALIDATION SETUP----------------------------------------------");
		addLine("");
		addLine("	----------------------------------------------INICIO TEMPLATE_FILE----------------------------------------------");
		addLine("	SELECT FM_TEMPLATE_FILE_SQ.NEXTVAL INTO idTemplateFile FROM DUAL;");
		addLine("	SELECT ID INTO idDelimiterType FROM FM_DELIMITER_TYPE WHERE NAME = 'STRING_FIRST';");
		addLine("	SELECT ID INTO idTemplateParser FROM FM_TEMPLATE_PARSER WHERE PARSER_NAME = 'structured';");
		addLine("	SELECT ID INTO idTemplateFileType FROM FM_TEMPLATE_FILE_TYPE WHERE TYPE_CODE = '"
				+ codeTemplateFileType + "';");
		addLine("	INSERT INTO FM_TEMPLATE_FILE (ID,CREATION_USER,DESCRIPTION,ENABLED,FIELDS_HAVE_START,NAME,SEPARATOR_STR,DELIMITER_TYPE_ID,MODULE_ID,TEMPLATE_FILE_ORIGEN_ID,TEMPLATE_FILE_PARENT_ID,TEMPLATE_PARSER_ID,TEMPLATE_FILE_ID,VALIDATION_SETUP_ID) "
				+ "VALUES(idTemplateFile, 'AVAL', 'Template "
				+ codeTemplateFileType
				+ "', 1, 0, '"
				+ codeTemplateFileType
				+ "', null, idDelimiterType, null, null, null, idTemplateParser, idTemplateFileType, idValidationSetup);");
		addLine("	----------------------------------------------FIN TEMPLATE_FILE----------------------------------------------");

		addLine("");
		addLine("	----------------------------------------------INICIO SECCIONES----------------------------------------------");
		count = 0;
		for (String str : lines) {
			tmpArray = str.split(";");
			if (tmpArray[0].startsWith("SECTION_")) {
				String delimiter = tmpArray.length == 1 ? "null" : tmpArray[1]
						.toString();
				tmpString = tmpArray[0].split("_")[1];
				addLine("	SELECT ID INTO idFM" + tmpString.toLowerCase()
						+ " FROM FM_SECTION  WHERE SECTION_NAME= '"
						+ tmpString.toUpperCase() + "';");
				addLine("	SELECT FM_TEMPLATE_FILE_SECTION_SQ.NEXTVAL INTO seccion_"
						+ count + " FROM DUAL;");
				addLine("	INSERT INTO FM_TEMPLATE_FILE_SECTION (ID,CARDINALITY,DELIMITER_STRING,INDEX_NR,TEMP_FILE_SECTION_PARENT_ID,SECTION_ID,TEMPLATE_FILE_ID,VAL_INSTANCE_ID) "
						+ " VALUES (seccion_"
						+ count
						+ ", 'REQUIRED_SINGLE', "
						+ delimiter
						+ ", 0, null, idFM"
						+ tmpString.toLowerCase() + ", idTemplateFile, null);");
				count++;
			}
		}
		addLine("	-----------------------------------------------FIN SECCIONES-----------------------------------------------");

		// ----------------
		addLine("");
		addLine("	----------------------------------------------INICIO SECCION VALIDACIONES----------------------------------------------");
		// TODO: Query de los otrs validadores
		addLine("	SELECT ID INTO idValidatorTypeString  FROM VL_VALIDATION_DATA_TYPE WHERE DT_CODE = 'string';");
		addLine("");
		count = 0;

		int countSections = -1;
		int countIndexBySections = 0;

		String[] arrayLine, arrayFieldName;
		String fieldName, referenceName, fieldLength, fieldStartPosition, fieldMask;
		for (String str : lines) {
			arrayLine = str.split(";");
			fieldName = arrayLine[0];
			if (!fieldName.startsWith("SECTION_")) {
				fieldStartPosition = arrayLine[1];
				fieldLength = arrayLine[3];
				fieldMask = arrayLine[4];

				arrayFieldName = fieldName.split("_");
				referenceName = "";
				for (String tmpStr : arrayFieldName) {
					referenceName = referenceName
							+ tmpStr.substring(0, 1).toUpperCase();
				}
				referenceName = "'00-" + codeTemplateFileType + "-"
						+ referenceName + "-" + count + "'";

				addLine("	SELECT VL_VALIDATION_INSTANCE_SQ.NEXTVAL INTO idValidatorInstance"
						+ count + " FROM DUAL;");

				addLine("	INSERT INTO VL_VALIDATION_INSTANCE (ID,MASK,REFERENCE_NAME,REQUIRED,VALIDATION_DATA_TYPE_ID,VALIDATION_SETUP_ID) "
						+ "VALUES (idValidatorInstance"
						+ count
						+ ", "
						+ fieldMask
						+ ", "
						+ referenceName
						+ ", 1, idValidatorTypeString, idValidationSetup);");

				addLine("	INSERT INTO FM_TEMPLATE_FIELD (ID,ABSENT_IN_FILE,DEFAULT_VALUE,DESCRIPTION,INDEX_NR,IS_BASE_FIELD,LENGTH,NAME,START_POSITION,FIELD_CATEGORY_ID,TEMPLATE_FILE_SECTION_ID,VAL_INSTANCE_ID) "
						+ "VALUES (FM_TEMPLATE_FIELD_SQ.NEXTVAL, 0, null, '"
						+ fieldName
						+ "', "
						+ countIndexBySections
						+ ", 0, "
						+ fieldLength
						+ ", '"
						+ fieldName
						+ "', "
						+ fieldStartPosition
						+ ", null, seccion_"
						+ countSections + ",idValidatorInstance" + count + ");");
				addLine("");
				count++;
				countIndexBySections++;
			} else {
				countSections++;
				countIndexBySections = 0;
				addLine("	--*******************************************************************************************************");
				addLine("	----------------------------------------------VALIDACIONES NUEVA SECCION----------------------------------------------");
				addLine("	--*******************************************************************************************************");
				addLine("");
			}
		}
		addLine("	----------------------------------------------FIN SECCION VALIDACIONES----------------------------------------------");
		addLine("");
		addLine("COMMIT;");
		addLine("");
		addLine("END;");
		addLine("/");
		try {
			FileUtils.writeStringToFile(new File(_RESULT_PATH + resultFile),
					_fileLines.toString(), false);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void declareMasks() {
		String sConfigFile = "masks.txt";

		InputStream in = InsertFileManager.class.getClassLoader()
				.getResourceAsStream(sConfigFile);
		if (in == null) {
			// File not found! (Manage the problem)
			return;
		}
		try {
			List<String> maskLines = IOUtils.readLines(in, "UTF-8");
			for (String line : maskLines) {
				addLine(line);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
