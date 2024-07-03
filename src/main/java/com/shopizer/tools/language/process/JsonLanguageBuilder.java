package com.shopizer.tools.language.process;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.translate.AmazonTranslate;
import com.amazonaws.services.translate.AmazonTranslateClientBuilder;
import com.amazonaws.services.translate.model.TranslateTextRequest;
import com.amazonaws.services.translate.model.TranslateTextResult;
import com.fasterxml.jackson.databind.json.JsonMapper;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

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

            Map<String, Object> map = mapper.readValue(new File(pathToFile.toString()), Map.class);
            System.out.println(map);

            // destination map
//            Map<String, String> results = map.entrySet().stream().collect(
//                    Collectors.toMap(
//                            e -> e.getKey(),
//                            e -> mapTranslation(translate, e.getValue(), targetLanguage),
//                            (u, v) -> u,
//                            LinkedHashMap::new));
            Map<String, Object> results = new LinkedHashMap<>();
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                results.put(entry.getKey(), translateNested(translate, entry.getValue(), targetLanguage));
            }

            generateTranslationFile(fullPath, targetLanguage, results, mapper);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private Object translateNested(AmazonTranslate translate, Object value, String targetLanguage) {
        if (value instanceof String) {
            return mapTranslation(translate, (String) value, targetLanguage);
        } else if (value instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) value;
            Map<Object, Object> translatedMap = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                translatedMap.put(entry.getKey(), translateNested(translate, entry.getValue(), targetLanguage));
            }
            return translatedMap;
        } else if (value instanceof List) {
            List<?> list = (List<?>) value;
            List<Object> translatedList = new ArrayList<>(list.size());
            for (Object item : list) {
                translatedList.add(translateNested(translate, item, targetLanguage));
            }
            return translatedList;
        } else {
            // 对于非字符串、Map、List类型的值，直接返回原值
            return value;
        }
    }

    /**
     * maybe just support eazy json
     *
     * @param translate
     * @param label
     * @param targetLanguage
     * @return
     */
    @Deprecated
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

    private void generateTranslationFile(String fullPath, String targetLang, Map<String, Object> resuts, JsonMapper mapper)
            throws Exception {

        Path newFilePath = Paths.get(fullPath + targetLang + ".json");

//        Files.write(newFilePath, () -> resuts.entrySet().stream()
//                .<CharSequence>map(e -> transform(e.getKey(), e.getValue())).iterator());
        Files.write(newFilePath, mapper.writeValueAsBytes(resuts));
    }

    private String transform(String key, String value) {
        return "\'" + key + "\':\'" + value + "\',";
    }

}
