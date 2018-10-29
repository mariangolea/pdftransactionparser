package com.mariangolea.fintracker.banks.csvparser.preferences;

import com.mariangolea.fintracker.banks.csvparser.preferences.UserPreferences.Timeframe;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public enum UserPreferencesHandler {

    INSTANCE;

    private static final String COMMENTS = "Automatically generated. DO NOT EDIT YOURSELF!!!";
    private static final String SUB_FOLDER = "preferences/";
    private static final String USER_PREFERENCES_FILE_DEFAULT_NAME = "userprefs.properties";
    private static final String COMPANY_NAMES_FILE_DEFAULT_NAME = "companynames.properties";
    private static final String CATEGORIES_FILE_DEFAULT_NAME = "categories.properties";

    private static final String CATEGORY_NAMES = "categories";
    private static final String INPUT_FOLDER = "inputFolder";
    private static final String TRANSACTION_GROUPING_TIMEFRAME = "timeFrame";
    private static final String SEPARATOR = ";";

    private final Properties userPrefsFile = new Properties();
    private final Properties companyNamesFile = new Properties();
    private final Properties categoriesFile = new Properties();
    private UserPreferences userPreferences;

    private UserPreferencesHandler() {
    }

    public UserPreferences getPreferences() {
        if (userPreferences == null) {
            userPreferences = new UserPreferences();
            loadPreferences();
        }
        return userPreferences;
    }

    private void loadPreferences() {
        loadUserPrefsFile();
        loadCompanyNamesFile();
        loadCategoryNamesFile();
    }

    public boolean storePreferences() {
        boolean success = storeUserPrefsFile();
        success &= storeCompanyNamesFile();
        success &= storeCategoriesFile();
        return success;
    }

    public boolean deletePreferences() {
        boolean success = deletePreferencesFile(USER_PREFERENCES_FILE_DEFAULT_NAME);
        success |= deletePreferencesFile(COMPANY_NAMES_FILE_DEFAULT_NAME);
        success |= deletePreferencesFile(CATEGORIES_FILE_DEFAULT_NAME);
        userPreferences = null;
        return success;
    }

    protected void loadUserPrefsFile() {
        userPrefsFile.putAll(loadProperties(USER_PREFERENCES_FILE_DEFAULT_NAME));
        userPreferences.setCSVInputFolder(userPrefsFile.getProperty(INPUT_FOLDER));
        Timeframe timeFrame = Timeframe.valueOf(userPrefsFile.getProperty(TRANSACTION_GROUPING_TIMEFRAME, Timeframe.MONTH.name()));
        userPreferences.setTransactionGroupingTimeframe(timeFrame);
    }

    protected boolean storeUserPrefsFile() {
        if (userPreferences.getCSVInputFolder() != null) {
            userPrefsFile.setProperty(INPUT_FOLDER, userPreferences.getCSVInputFolder());
        }
        userPrefsFile.setProperty(TRANSACTION_GROUPING_TIMEFRAME, userPreferences.getTransactionGroupingTimeframe().name());
        return storeProperties(USER_PREFERENCES_FILE_DEFAULT_NAME, userPrefsFile, COMMENTS);
    }

    protected void loadCompanyNamesFile() {
        companyNamesFile.putAll(loadProperties(COMPANY_NAMES_FILE_DEFAULT_NAME));
        companyNamesFile.keySet().forEach(categoryName -> {
            userPreferences.setCompanyDisplayName(categoryName.toString(), companyNamesFile.get(categoryName).toString());
        });
    }

    protected void loadCategoryNamesFile() {
        categoriesFile.putAll(loadProperties(CATEGORIES_FILE_DEFAULT_NAME));
        String categoryNamesString = categoriesFile.getProperty(CATEGORY_NAMES);
        Set<String> categoryNames = new HashSet<>(
                convertPersistedStringToList(categoryNamesString, SEPARATOR));
        categoryNames.forEach((categoryName) -> {
            Set<String> categories = new HashSet<>(
                    convertPersistedStringToList(categoriesFile.getProperty(categoryName), SEPARATOR));
            userPreferences.appendDefinition(categoryName, categories);
        });
    }

    protected boolean storeCompanyNamesFile() {
        userPreferences.getCompanyIdentifierStrings().forEach((companyKey) -> {
            companyNamesFile.setProperty(companyKey, userPreferences.getCompanyDisplayName(companyKey));
        });

        return storeProperties(COMPANY_NAMES_FILE_DEFAULT_NAME, companyNamesFile, COMMENTS);
    }

    protected boolean storeCategoriesFile() {
        final Collection<String> categoryNames = userPreferences.getTopMostCategories();
        String categoryNamesValue = convertStringsForStorage(categoryNames, SEPARATOR);
        if (categoryNamesValue != null && !categoryNamesValue.isEmpty()) {
            categoriesFile.setProperty(CATEGORY_NAMES, categoryNamesValue);
            categoryNames.forEach((categoryName) -> {
                storeTopMostCategoryName(categoryName);
            });
        }

        return storeProperties(CATEGORIES_FILE_DEFAULT_NAME, categoriesFile, COMMENTS);
    }

    private void storeTopMostCategoryName(final String topMostCategory) {
        Collection<String> subCategories = userPreferences.getSubCategories(topMostCategory);
        if (subCategories != null) {
            categoriesFile.setProperty(topMostCategory, convertStringsForStorage(subCategories, SEPARATOR));
            for (String category : subCategories) {
                storeTopMostCategoryName(category);
            }
        }
    }

    protected String convertStringsForStorage(final Collection<String> strings, final String separator) {
        String soleString = "";
        if (strings == null || strings.isEmpty()) {
            return soleString;
        }
        soleString = strings.stream().map((categoryName) -> categoryName + separator).reduce(soleString, String::concat);
        soleString = soleString.substring(0, soleString.length() - 1);

        return soleString;
    }

    protected List<String> convertPersistedStringToList(final String string, final String separator) {
        List<String> strings = new ArrayList<>();
        if (string == null || string.isEmpty()) {
            return strings;
        }
        String[] split = string.split(separator);
        strings.addAll(Arrays.asList(split));
        return strings;
    }
    
    private boolean storeProperties(final String filePath, final Properties properties, final String comments) {
        File propertiesFile = new File(getPreferencesFolder(), filePath);
        try {
            if (!propertiesFile.exists() && !propertiesFile.createNewFile()) {
                return false;
            }
            try (FileWriter writer = new FileWriter(propertiesFile)) {
                properties.store(writer, comments);
            }
            return true;

        } catch (IOException ex) {
            Logger.getLogger(UserPreferencesHandler.class.getName()).log(Level.SEVERE, null, ex);
        }

        return false;
    }

    
    private Properties loadProperties(final String filePath) {
        File propertiesFile = new File(getPreferencesFolder(), filePath);
        Properties properties = new Properties();
        try {
            if (!propertiesFile.exists()) {
                propertiesFile.createNewFile();
            }
            try (FileReader reader = new FileReader(propertiesFile)) {
                properties.load(reader);
            }
        } catch (IOException ex) {
            Logger.getLogger(UserPreferencesHandler.class.getName()).log(Level.SEVERE, null, ex);
        }

        return properties;
    }

    private boolean deletePreferencesFile(final String filePath) {
        File propertiesFile = new File(getPreferencesFolder(), filePath);
        try {
            Files.deleteIfExists(propertiesFile.toPath());
            return true;
        } catch (IOException ex) {
            Logger.getLogger(UserPreferencesHandler.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    private File getPreferencesFolder() {
        File folder = new File(SUB_FOLDER);
        if (!folder.isDirectory()) {
            folder.mkdir();
        }
        return folder;
    }
}
