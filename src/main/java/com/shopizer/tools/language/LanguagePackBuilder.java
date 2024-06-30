package com.shopizer.tools.language;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.shopizer.tools.language.process.LanguageBuilder;


/**
 * Lang pack generator
 * @author carlsamson
 *
 */
public class LanguagePackBuilder {

	public static final String targetISOLanguage = "zh";
    /**
     * like D:/workspaces/Shopizer/shopizer
     */
	public static final String shopizerRootPath = "D:/workspaces/Shopizer/shopizer";

	private static final String pathToBundlesFiles = "/sm-shop/src/main/resources/bundles";
	private static List<String> bundleFileNames = new ArrayList<>(Arrays.asList("shopizer", "shipping", "payment", "messages"));


	public static void main(String args[]) {

		if (args.length == 0) {
			System.out.println("This software requires 2 arguments " +
                    "(0) absolute root path of shopizer (D:/workspaces/shopizer) \n" +
                    "(1) target language iso code (example es)\n" +
                    "(2) accessKey of aws user(eg.  abc...xyz)\n" +
                    "(3) secretKey of aws user(eg.  123...abc)");
		}

		LanguagePackBuilder langPackApp = new LanguagePackBuilder();

		langPackApp.translateFromExisting(args[0], args[1], args[2], args[3]);
	    langPackApp.printInstructions(args[1], args[0]);


//		langPackApp.translateFromExisting(shopizerRootPath, targetISOLanguage);
//		langPackApp.printInstructions(targetISOLanguage, shopizerRootPath);


	}

	public void translateFromExisting(String shopizerRoot, String targetISOLang, String accessKey, String secretKey) {

		LanguageBuilder builder = new LanguageBuilder();

		for(String file : bundleFileNames) {

			String fullPath = shopizerRoot + pathToBundlesFiles + "/" + file;
			builder.process(fullPath, targetISOLang, accessKey, secretKey);

		}

	}

	public void printInstructions(String language, String path) {
		StringBuilder instructions = new StringBuilder();
		instructions.append("**************************************").append("\r\n");
		instructions.append("Next steps:")
		.append("\r\n")
		.append("\r\n")
		.append("- Make sure these files are generated in " + language + pathToBundlesFiles + ":").append("\r\n");
		for(String file : bundleFileNames) {
			String fullPath = shopizerRootPath + pathToBundlesFiles + "/" + file + "_" + path + ".properties";
			instructions.append(fullPath).append("\r\n");
		}
		instructions.append("\r\n").append("\r\n");
		instructions.append("check if translation makes sense in individual files and fix bad translations if any").append("\r\n").append("\r\n");

		instructions.append("- Run this query in Shopizer database").append("\r\n");
		instructions.append("insert into SALESMANAGER.LANGUAGE('LANGUAGE_ID','DATE_CREATED','DATE_MODIFIED','CODE') values (select SEQ_COUNT from SM_SEQUENCER where SEQ_NAME='LANG_SEQ_NEXT_VAL', CURDATE(), CURDATE(), '" + path + "')").append("\r\n");
		instructions.append("\r\n").append("\r\n");
		instructions.append("Feel free to share your new language with use ! Submit a pull request (PR) to https://github.com/shopizer-ecommerce/shopizer");
		System.out.println(instructions);
	}

}
