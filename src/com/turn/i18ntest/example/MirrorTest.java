package com.turn.i18ntest.example;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.turn.i18ntest.I18nTest;

@Listeners({ com.turn.i18ntest.I18nTestListener.class })
public class MirrorTest {
	@Test(dataProvider = "dp")
	@I18nTest(enable = true, dictionary = "src/com/turn/i18ntest/example/dictionary.json")
	public void testProphecyOfMirror(String question, String expectedAnswer) {
		MirrorMirrorOnTheWall mirror = new MirrorMirrorOnTheWall();
		String prophecy = mirror.answer(question);
		
		System.out.format("Question: %s\n", question);
		System.out.format("Answer: %s\n", prophecy);
		System.out.format("Expected: %s\n", expectedAnswer);
		
		Assert.assertEquals(prophecy, expectedAnswer);
	}

	@DataProvider(name = "dp")
	public static Object[][] createData() {
		return new Object[][] { new Object[] { "Who will win the World Cup",
				"Costa Rica" } };
	}
}
