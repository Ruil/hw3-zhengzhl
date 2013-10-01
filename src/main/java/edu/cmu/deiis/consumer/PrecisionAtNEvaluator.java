/**
 * 
 */
package edu.cmu.deiis.consumer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CasConsumer_ImplBase;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceProcessException;
import org.apache.uima.util.ProcessTrace;

import edu.cmu.deiis.types.Answer;
import edu.cmu.deiis.types.Question;

/**
 * @author hector
 * 
 */
public class PrecisionAtNEvaluator extends CasConsumer_ImplBase {
	double totalScore = 0.0;
	int documentCount = 0;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.uima.collection.base_cpm.CasObjectProcessor#processCas(org
	 * .apache.uima.cas.CAS)
	 */
	@Override
	public void processCas(CAS aCas) throws ResourceProcessException {
		JCas aJCas = null;
		try {
			aJCas = aCas.getJCas();
		} catch (CASException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		List<Answer> answers = new ArrayList<Answer>(JCasUtil.select(aJCas,
				Answer.class));
		Question question = JCasUtil.selectSingle(aJCas, Question.class);
		double totalCorrect = 0.0;

		for (Answer answer : answers) {
			if (answer.getIsCorrect()) {
				totalCorrect += 1;
			}
		}

		Collections.sort(answers, new Comparator<Answer>() {
			@Override
			public int compare(Answer ans1, Answer ans2) {
				return ans1.getConfidence() > ans2.getConfidence() ? -1 : 1;
			}
		});

		int numCorrect = 0;

		for (int i = 0; i < totalCorrect; i++) {
			if (answers.get(i).getIsCorrect()) {
				numCorrect++;
			}
		}

		double precisionAtN = numCorrect / totalCorrect;

		System.out.println(String.format("Question: %s",
				question.getCoveredText()));

		for (Answer answer : answers) {
			String correctInd = answer.getIsCorrect() ? "+" : "-";
			System.out.println(String.format("%s %.2f %s", correctInd,
					answer.getConfidence(), answer.getCoveredText()));
		}

		System.out.println(String.format("Precision at %d %.2f ",
				(int) totalCorrect, precisionAtN));
		System.out.println();

		totalScore += precisionAtN;
		documentCount += 1;
	}

	/**
	 * Basically only output the average precision
	 */
	@Override
	public void collectionProcessComplete(ProcessTrace aTrace)
			throws ResourceProcessException, IOException {
		System.out.println("Average precision " + totalScore / documentCount);
	}

}
