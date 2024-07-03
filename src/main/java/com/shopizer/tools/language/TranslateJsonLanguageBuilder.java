package com.shopizer.tools.language;

import com.shopizer.tools.language.process.JsonLanguageBuilder;

/**
 * use this Builder need exchange main-class in pom.xml
 */
public class TranslateJsonLanguageBuilder {


    public static final String targetISOLanguage = "fr";

    private static final String filePath = "/Users/username/Documents/dev/workspace/shopizer/shop/shopizer/packages/theme/lang/en.json";

    private static final String adminPath = "src/assets/i18n/";

    private static final String webPath = "src/translations/";

    private static final String accessKey = "";

    private static final String secretKey = "";

    public static void main(String args[]) {

        if (args.length == 0) {
            System.out.println("This software requires 5 arguments (1) absolute root path of json file (example /temp/app/shopizer-admin)\n" +
                    " (2) target language iso code (example es)\n" +
                    "(3) aws accessKey\n" +
                    "(4) aws secretKey\n" +
                    "(5) isAdmin 'true' or 'false'");
        }

        TranslateJsonLanguageBuilder langPackApp = new TranslateJsonLanguageBuilder();
        boolean isAdmin;
        if (args[4] == null || args[4].trim().length() < 1) {
            isAdmin = false;
        } else {
            isAdmin = Boolean.parseBoolean(args[4]);

        }

        langPackApp.translateFromExisting(args[0], args[1], args[2], args[3], isAdmin);
        langPackApp.printInstructions(args[0], args[1]);


//        langPackApp.translateFromExisting(filePath, targetISOLanguage, accessKey, secretKey, true);
//        langPackApp.printInstructions(filePath, targetISOLanguage);


    }

    public void translateFromExisting(String path, String targetISOLang, String accessKey, String secretKey, boolean isAdmin) {

        JsonLanguageBuilder builder = new JsonLanguageBuilder();
        String enJsonPath = path + "/" +  (isAdmin ? adminPath : webPath);
        builder.process(enJsonPath, "us-east-1", targetISOLang, accessKey, secretKey, isAdmin);


    }

    public void printInstructions(String path, String targetLanguage) {
        StringBuilder instructions = new StringBuilder();
        instructions.append("**************************************").append("\r\n");
        instructions.append("Next steps:")
                .append("\r\n")
                .append("\r\n")
                .append("- Make sure the file is generated in " + path + " and has .json extension").append("\r\n");

        instructions.append(path).append("\r\n");

        instructions.append("\r\n").append("\r\n");
        instructions.append("check if translation makes sense in individual files and fix bad translations if any").append("\r\n").append("\r\n");

        instructions.append("- Run this query in Shopizer database").append("\r\n");
        instructions.append("insert into SALESMANAGER.LANGUAGE('LANGUAGE_ID','DATE_CREATED','DATE_MODIFIED','CODE') values (select SEQ_COUNT from SM_SEQUENCER where SEQ_NAME='LANG_SEQ_NEXT_VAL', CURDATE(), CURDATE(), '" + targetLanguage + "')").append("\r\n");
        instructions.append("\r\n").append("\r\n");
        instructions.append("Feel free to share your new language with use ! Submit a pull request (PR) to https://github.com/shopizer-ecommerce/shopizer");
        System.out.println(instructions);
    }

}
