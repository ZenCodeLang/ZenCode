package org.openzen.zenscript.scriptingexample.tests.actual_test.file_names;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.openzen.zenscript.scriptingexample.tests.helpers.ScriptBuilder;
import org.openzen.zenscript.scriptingexample.tests.helpers.ZenCodeTest;

import java.io.File;

public class FileNamesAndSpecialCharacters extends ZenCodeTest {

	/**
	 * All the characters that we test.
	 */
	public static char[] specialCharsToTest() {
		return new char[]{'.', '1', '%', '$', '_', '!', '/', '\\', '\0', '\1', ' ', ';', ':', '-', '|', '[', ']'};
	}

	/**
	 * We disable the debug mode here because some of the filenames are invalid filenames in Windows/Unix.
	 * Since debug mode would try to write them out we disable it here.
	 */
	@BeforeEach
	public void disableDebug() {
		engine.debug = false;
	}

	@ParameterizedTest(name = "[{index}] Checking special Character '{0}'")
	@MethodSource("specialCharsToTest")
	public void TestThatFilenameCanContainCharacter(char characterToTest) {

		ScriptBuilder.create()
				.startNewScript(String.format("t%1$se%1$ss%1$st.zs", characterToTest))
				.add("println('Hello World');")
				.execute(this);

		logger.assertNoWarnings();
		logger.assertNoErrors();
		logger.assertPrintOutputSize(1);
		logger.assertPrintOutput(0, "Hello World");
	}

	@ParameterizedTest
	@MethodSource(value = "specialCharsToTest")
	public void TestThatFilesWithSpecialCharacterFileNameAreAccessibleInsideOtherScripts(char characterToTest) {
		ScriptBuilder.create()
				.startNewScript(String.format("t%1$se%1$ss%1$st_1.zs", characterToTest))
				.add("public function getTheString() as string => 'Hello World';")
				.startNewScript(String.format("t%1$se%1$ss%1$st_2.zs", characterToTest))
				.add("println(getTheString());")
				.execute(this);

		logger.assertNoWarnings();
		logger.assertNoErrors();
		logger.assertPrintOutputSize(1);
		logger.assertPrintOutput(0, "Hello World");
	}

	@ParameterizedTest
	@MethodSource(value = "specialCharsToTest")
	public void TestThatFilesWithSpecialCharactersCanBePutInSubFolders(char characterToTest) {
		//noinspection SpellCheckingInspection
		ScriptBuilder.create()
				.startNewScript(String.format("some%2$sfolder%2$st%1$se%1$ss%1$st_1.zs", characterToTest, File.separatorChar))
				.add("public function getTheString() as string => 'Hello World';")
				.startNewScript(String.format("some%2$sfolder%2$st%1$se%1$ss%1$st_2.zs", characterToTest, File.separatorChar))
				.add("println(getTheString());")
				.execute(this);

		logger.assertNoWarnings();
		logger.assertNoErrors();
		logger.assertPrintOutputSize(1);
		logger.assertPrintOutput(0, "Hello World");
	}
}
