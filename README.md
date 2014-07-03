i18n-test - An Easy Add-on for TestNG i18n Testing
==================================================

Introduction
------------

This easy tool is used as a complimentary of TestNG, which aims to help QA write more readable and maintainable i18n test cases with less effort.

To achieve this goal, this i18n test tool is designed and created with thinkings of:
- Maximize the current test case reuse, minimize the code refactoring.
- Drive i18n test with a simple "dictionary", which makes test case separated with i18n data and be easy to manage and expand.

Example
-------
Suppose we have a magic mirror which can answer questions, only if it is asked in English. For example, we can ask "Who will win the World Cup". And the mirror tells you "Costa Rica" (just kidding) by invoking the "answer" method.
```java
public class MirrorMirrorOnTheWall {
    public String answer(String question) { ... }
}
```

Now we want to write a test for the "answer" method to check if it works fine. Here let's just suppose "Costa Rica" is the right result we expect the method to return.
```java
public class MirrorTest {
	@Test(dataProvider = "dp")
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
		return new Object[][] { new Object[] { "Who will win the World Cup", "Costa Rica" } };
	}
}
```
Running the test, we will get the test result:
```
Question: Who will win the World Cup
Answer: Costa Rica
Expected: Costa Rica
PASSED: testProphecyOfMirror("Who will win the World Cup", "Costa Rica")
```

Later we upgrade the mirror and it become so powerful that it can answer questions in different languages. For example, if we input Japanese "誰がワールドカップで勝つのだろうか", the result should be "コスタリカ". Or we ask in Russian, "Кто выиграет чемпионат мира". The answer will be "Коста-Рика". To create the i18n test with this tool, we need to change the test a little bit.
```java
@Listeners({ com.turn.i18ntest.I18nTestAdapter.class })
public class MirrorTest {
	@Test(dataProvider = "dp")
	@I18nTest(enable = true, dictionary = "src/com/turn/i18ntest/example/dictionary.json")
	public void testProphecyOfMirror(String question, String expectedAnswer) { ... }

	@DataProvider(name = "dp")
	public static Object[][] createData() { ... }
}
```

The i18n for the 2 foreign languages are automatically triggered, then we will get such a result:
```
Question: Who will win the World Cup
Answer: Costa Rica
Expected: Costa Rica
22:37:49.907 [main] INFO  I18nTestLogger - I18n tests triggered...
22:37:49.947 [main] INFO  I18nTestLogger - I18n testing language: Japanese
Question: 誰がワールドカップで勝つのだろうか
Answer: コスタリカ
Expected: コスタリカ
22:37:49.947 [main] INFO  I18nTestLogger - I18n testing language: Russian
Question: Кто выиграет чемпионат мира
Answer: Коста-Рика
Expected: Коста-Рика
PASSED: testProphecyOfMirror("Who will win the World Cup", "Costa Rica")
```

If the output does not meet the expectation, the test will fail and the result will be:
```
Question: Who will win the World Cup
Answer: Costa Rica
Expected: Costa Rica
22:45:35.149 [main] INFO  I18nTestLogger - I18n tests triggered...
22:45:35.188 [main] INFO  I18nTestLogger - I18n testing language: Japanese
Question: 誰がワールドカップで勝つのだろうか
Answer: ?????
Expected: コスタリカ
22:45:35.189 [main] ERROR I18nTestLogger - I18n failure: java.lang.AssertionError: expected [コスタリカ] but found [?????]
22:45:35.189 [main] INFO  I18nTestLogger - I18n testing language: Russian
Question: Кто выиграет чемпионат мира
Answer: Коста-Рика
Expected: Коста-Рика
FAILED: testProphecyOfMirror("Who will win the World Cup", "Costa Rica")
```

To perform the i18n test, the changes we made are only:
1. Add **@Listeners({ com.turn.i18ntest.I18nTestAdapter.class })** annotation on the test class to introduce the i18n test feature.
2. Add **@I18nTest(enable = true, dictionary = "src/com/turn/i18ntest/example/dictionary.json")** to indicate this test case will be a i18n test. The @I18nTest annotation has 2 fields:
..*The i18n tests can be disabled if necessary (e.g. debugging test) by setting the "enable" field to false.
..*The "dictionary" field specify the path where the dictionary is put in.
3. The dictionary in JSON format is created to feed as the i18n input. Here is an example:
```json
{
	"languages": ["Japanese", "Russian"],
	"entries": {
		"Who will win the World Cup": {
			"Japanese": "誰がワールドカップで勝つのだろうか",
			"Russian": "Кто выиграет чемпионат мира"
		},
		"Costa Rica": {
			"Japanese": "コスタリカ",
			"Russian": "Коста-Рика"
		}
	}
}
```
The language key specify the languanges need to perform i18n tests. And entries use the English word as the key and objects contains translation as the value.
