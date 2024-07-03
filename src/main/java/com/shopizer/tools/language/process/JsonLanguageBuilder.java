package com.shopizer.tools.language.process;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.translate.AmazonTranslate;
import com.amazonaws.services.translate.AmazonTranslateClientBuilder;
import com.amazonaws.services.translate.model.TranslateTextRequest;
import com.amazonaws.services.translate.model.TranslateTextResult;
import com.fasterxml.jackson.databind.json.JsonMapper;

public class JsonLanguageBuilder {

    public void process(String fullPath, String region, String targetLanguage, String accessKey, String secretKey, boolean isAdmin) {

        try {

            // validate path enJson
            Path pathToFile = Paths.get(fullPath + (isAdmin ? "en.json" : "english.json"));

            if (!Files.exists(pathToFile)) {
                throw new Exception("Path [" + pathToFile.toString() + "] does not exists");
            }

            long count = Arrays.asList(Locale.getISOLanguages()).stream().filter(l -> targetLanguage.equals(l)).count();

            if (count == 0) {
                throw new Exception("Language with isocode [" + targetLanguage + "]");
            }

            // Create credentials using a provider chain. For more information, see
            // https://docs.aws.amazon.com/sdk-for-java/v1/developer-guide/credentials.html
//			AWSCredentialsProvider awsCreds = DefaultAWSCredentialsProviderChain.getInstance();
//
//			AmazonTranslate translate = AmazonTranslateClient.builder()
//					.withCredentials(new AWSStaticCredentialsProvider(awsCreds.getCredentials())).withRegion(region)
//					.build();
            BasicAWSCredentials awsCreds = new BasicAWSCredentials(accessKey, secretKey);

            AmazonTranslate translate = AmazonTranslateClientBuilder.standard()
                    .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                    .withRegion(Regions.DEFAULT_REGION)
                    .build();

            JsonMapper mapper = new JsonMapper();

            // convert JSON string to Map

            @SuppressWarnings("unchecked")
            Map<String, String> map = mapper.readValue(new File(pathToFile.toString()), Map.class);

            System.out.println(map);

            // destination map
            Map<String, String> results = map.entrySet().stream().collect(
                    Collectors.toMap(
                            e -> e.getKey(),
                            e -> mapTranslation(translate, e.getValue(), targetLanguage),
                            (u, v) -> u,
                            LinkedHashMap::new));

            generateTranslationFile(fullPath, targetLanguage, results);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private String mapTranslation(AmazonTranslate translate, String label, String targetLanguage) {

        String text = null;
        try {

            TranslateTextRequest request = new TranslateTextRequest().withText(label).withSourceLanguageCode("en")
                    .withTargetLanguageCode(targetLanguage);
            TranslateTextResult result = translate.translateText(request);
            System.out.println(result.getTranslatedText());
            text = result.getTranslatedText();

        } catch (Exception e) {
            e.printStackTrace();

        }

        return text;
    }

    private void generateTranslationFile(String fullPath, String targetLang, Map<String, String> resuts)
            throws Exception {

        Path newFilePath = Paths.get(fullPath + targetLang + ".json");

        Files.write(newFilePath, () -> resuts.entrySet().stream()
                .<CharSequence>map(e -> transform(e.getKey(), e.getValue())).iterator());

    }

    private String transform(String key, String value) {
        return "\'" + key + "\':\'" + value + "\',";
    }

}
