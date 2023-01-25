package de.quantumrange.verbo.model;

import eu.bitwalker.useragentutils.Browser;
import eu.bitwalker.useragentutils.OperatingSystem;

public record VocView(long timestamp,
					  String answer,
					  int correctness,
					  AnswerClassification classification,
					  int answerDuration,    // in ms
					  LearningMode mode,
					  Browser browser,
					  OperatingSystem os,
					  boolean reversed        /* Answer -> Question */) {

}