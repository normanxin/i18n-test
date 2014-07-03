package com.turn.i18ntest.example;

public class MirrorMirrorOnTheWall {
	public String answer(String question) {
		if ("Who will win the World Cup".equals(question)) {
			return "Costa Rica";
		}
		if ("誰がワールドカップで勝つのだろうか".equals(question)) {
			return "コスタリカ";
		}
		if ("Кто выиграет чемпионат мира".equals(question)) {
			return "Коста-Рика";
		}
		
		return null;
	}
}
