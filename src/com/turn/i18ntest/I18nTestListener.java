package com.turn.i18ntest;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Scanner;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.testng.IClass;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

public class I18nTestListener extends TestListenerAdapter {
	private static Logger logger = LogManager.getLogger("I18nTestLogger");

	static {
		setLoggerLevel(logger, Level.INFO);
	}

	@Override
	public void onTestSuccess(ITestResult tr) {
		ITestNGMethod currentTestNGMethod = tr.getMethod();
		IClass currentTestNGClass = tr.getTestClass();
		Class<?> testClass = currentTestNGClass.getRealClass();
		I18nTest i18nAnnotation = getI18nTestAnnotation(currentTestNGMethod);

		String dictionary = null;
		if (i18nAnnotation.enable() == true) {
			try {
				dictionary = readDictionaryFileToString(i18nAnnotation
						.dictionary());
			} catch (FileNotFoundException e) {
				String err = String.format(
						"No dictionary found for %s. I18n tests skipped",
						currentTestNGMethod.getMethodName());
				logger.warn(err);
				return;
			}

			logger.info("I18n tests triggered...");

			String[] languages = getLanguages(dictionary);
			Method dataProviderMethod = getDataProviderMethod(currentTestNGMethod, currentTestNGClass);
			Object[][] input = null;
			try {
				input = (Object[][]) dataProviderMethod.invoke(
						testClass.newInstance(), (Object[]) null);
			} catch (Exception e) {
				logger.error(e.getMessage());
			}

			Method method = currentTestNGMethod.getConstructorOrMethod()
					.getMethod();

			for (String language : languages) {
				logger.info("I18n testing language: " + language);

				Object[] translatedInput = getTranslatedData(input, language,
						dictionary);

				try {
					method.invoke(testClass.newInstance(), translatedInput);
				} catch (InvocationTargetException ex) {
					tr.setStatus(ITestResult.FAILURE);
					logger.error("I18n failure: " + ex.getCause());
				} catch (Exception e) {
					logger.error(e.getMessage());
				}
			}
		}
	}

	private static Level setLoggerLevel(Logger log, Level level) {
		LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
		Configuration conf = ctx.getConfiguration();
		LoggerConfig lconf = conf.getLoggerConfig(log.getName());
		Level oldLevel = lconf.getLevel();
		lconf.setLevel(level);
		ctx.updateLoggers(conf);
		return oldLevel;
	}

	private Annotation[] getTestNGMethodAnnotations(ITestNGMethod testMethod) {
		Method method = testMethod.getConstructorOrMethod().getMethod();
		return method.getAnnotations();
	}

	private I18nTest getI18nTestAnnotation(ITestNGMethod testMethod) {
		for (Annotation annotation : getTestNGMethodAnnotations(testMethod)) {
			if (annotation instanceof I18nTest) {
				return (I18nTest) annotation;
			}
		}
		return null;
	}

	private Method getDataProviderMethod(ITestNGMethod testNGMethod, IClass testNGClass) {
		String name = getDataProviderName(testNGMethod);
		Class<?> testClass = testNGClass.getRealClass();
		return getDataProviderMethodByName(name, testClass);
	}

	private Method getDataProviderMethodByName(String name, Class<?> testClass) {
		for (Method method : testClass.getMethods()) {
			for (Annotation annotation : method.getAnnotations()) {
				if (annotation instanceof DataProvider) {
					String dataProviderName = ((DataProvider) annotation)
							.name();
					if (name.equals(dataProviderName)) {
						return method;
					}
				}
			}
		}

		return null;
	}

	private String getDataProviderName(ITestNGMethod testMethod) {
		for (Annotation annotation : getTestNGMethodAnnotations(testMethod)) {
			if (annotation instanceof Test) {
				return ((Test) annotation).dataProvider();
			}
		}

		return null;
	}

	private Object[] getTranslatedData(Object[][] input, String language,
			String dictionary) {
		Object[] translatedData = new Object[input[0].length];

		for (int i = 0; i < input[0].length; i++) {
			if (input[0][i] instanceof String) {
				String translation = translate((String) input[0][i], language,
						dictionary);
				translatedData[i] = translation;
			} else {
				translatedData[i] = input[0][i];
			}
		}

		return translatedData;
	}

	private String readDictionaryFileToString(String path)
			throws FileNotFoundException {
		String output = null;
		output = new Scanner(new File(path), "UTF-8").useDelimiter("\\Z")
				.next();
		return output;
	}

	private JsonObject parseStringToJsonObject(String str) {
		JsonParser parser = new JsonParser();
		return (JsonObject) parser.parse(str);
	}

	private String[] getLanguages(String dictionary) {
		JsonObject dictionaryJsonObject = parseStringToJsonObject(dictionary);
		JsonArray languageJsonObject = dictionaryJsonObject
				.getAsJsonArray("languages");
		Gson converter = new Gson();
		return converter.fromJson(languageJsonObject, String[].class);
	}

	private String translate(String keyword, String language, String dictionary) {
		JsonObject dictionaryjsonObject = parseStringToJsonObject(dictionary);
		JsonObject entriesJsonObject = dictionaryjsonObject
				.getAsJsonObject("entries");
		JsonObject entryJsonObject = entriesJsonObject.getAsJsonObject(keyword);
		JsonPrimitive translationJsonObject = entryJsonObject
				.getAsJsonPrimitive(language);
		return translationJsonObject.getAsString();
	}
}
