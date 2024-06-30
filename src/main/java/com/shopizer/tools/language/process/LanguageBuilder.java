package com.shopizer.tools.language.process;

import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.translate.AmazonTranslate;
import com.amazonaws.services.translate.AmazonTranslateClient;
import com.amazonaws.services.translate.AmazonTranslateClientBuilder;
import com.amazonaws.services.translate.model.TranslateTextRequest;
import com.amazonaws.services.translate.model.TranslateTextResult;

public class LanguageBuilder {

    public void process(String fullPath, String targetLanguage, String accessKey, String secretKey) {


        try {

            //validate path
            Path pathToFile = Paths.get(fullPath + ".properties");

            if (!Files.exists(pathToFile)) {
                throw new Exception("Path [" + pathToFile.toString() + "] does not exists");
            }

            long count = Arrays.stream(Locale.getISOLanguages())
                    .filter(targetLanguage::equals).count();

            if (count == 0) {
                throw new Exception("Language with isocode [" + targetLanguage + "]");
            }


            // Create credentials using a provider chain. For more information, see
            // https://docs.aws.amazon.com/sdk-for-java/v1/developer-guide/credentials.html
//			 AWSCredentialsProvider awsCreds = DefaultAWSCredentialsProviderChain.getInstance();
//
//			 AmazonTranslate translate = AmazonTranslateClient.builder()
//			 .withCredentials(new
//			 AWSStaticCredentialsProvider(awsCreds.getCredentials()))
//			 .withRegion(region)
//			 .build();

            BasicAWSCredentials awsCreds = new BasicAWSCredentials(accessKey, secretKey);

            AmazonTranslate translate = AmazonTranslateClientBuilder.standard()
                    .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                    .withRegion(Regions.DEFAULT_REGION)
                    .build();

            //read file
            Properties properties = new Properties();
            properties.load(Files.newInputStream(Paths.get(pathToFile.toString())));
            // add  some properties  here

            //origin map
            Map<String, String> keyValue = new HashMap<String, String>(properties.entrySet()
                    .stream()
                    .collect(Collectors.toMap(e -> e.getKey().toString(),
                            e -> e.getValue().toString())));

            //destination map
            Map<String, String> results = keyValue.entrySet().stream()
                            .collect(Collectors.toMap(
                                    Map.Entry::getKey,
                                    e -> mapTranslation(translate, e.getValue(), targetLanguage)
                            ));

            generateTranslationFile(fullPath, targetLanguage, results);


/*			 Map<String,String> keyValues = new TreeMap<String,String>();
			 for(String key : keyValue.keySet()) {
				 TranslateTextRequest request = new TranslateTextRequest()
				 .withText("Hello, world")
				 .withSourceLanguageCode("en")
				 .withTargetLanguageCode("es");
				 TranslateTextResult result = translate.translateText(request);
				 System.out.println(result.getTranslatedText());
				 keyValues.put(key, result.getTranslatedText());
			 }*/

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private String mapTranslation(AmazonTranslate translate, String label, String targetLanguage) {


        String text = null;
        try {

            TranslateTextRequest request = new TranslateTextRequest()
                    .withText(label)
                    .withSourceLanguageCode("en")
                    .withTargetLanguageCode(targetLanguage);
            TranslateTextResult result = translate.translateText(request);
            System.out.println(result.getTranslatedText());
            text = result.getTranslatedText();

        } catch (Exception e) {
            e.printStackTrace();

        }

        return text;
    }

    private void generateTranslationFile(String fullPath, String targetLang, Map<String, String> resuts) throws Exception {

        Path newFilePath = Paths.get(fullPath + "_" + targetLang + ".properties");

        Files.write
                (newFilePath, () -> resuts.entrySet().stream()
                        .<CharSequence>map(e -> e.getKey() + "=" + e.getValue())
                        .iterator());

    }

}
