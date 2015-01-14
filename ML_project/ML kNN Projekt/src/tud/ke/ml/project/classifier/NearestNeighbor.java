package tud.ke.ml.project.classifier;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

import tud.ke.ml.project.framework.classifier.ANearestNeighbor;
import tud.ke.ml.project.util.Pair;

/**
 * This implementation assumes the class attribute is always available (but
 * probably not set)
 * 
 * @author cwirth
 *
 */
public class NearestNeighbor extends ANearestNeighbor {

	protected double[] scaling;
	protected double[] translation;
	private List<List<Object>> traindata;
	private Map<Object, List<List<Pair<Object, Double>>>> vdm = new HashMap<Object, List<List<Pair<Object, Double>>>>();

	@Override
	protected Object vote(List<Pair<List<Object>, Double>> subset) {
		System.out.println(subset.size());
		return getWinner(isInverseWeighting() ? getWeightedVotes(subset)
				: getUnweightedVotes(subset));

	}

	@Override
	protected void learnModel(List<List<Object>> traindata) {
		this.traindata = traindata;
	}

	@Override
	protected Map<Object, Double> getUnweightedVotes(
			List<Pair<List<Object>, Double>> subset) {

		return weightedVotesFun(subset, 0);
		// Map<Object, Double> map = new HashMap<Object, Double>();
		// for (int i = 0; i < subset.size(); i++) {
		// String classAtt = (String) subset.get(i).getA()
		// .get(subset.get(i).getA().size() - 1);
		// if (map.containsKey(classAtt))
		// map.put(classAtt, map.get(classAtt) + subset.get(i).getB());
		// else
		// map.put(classAtt, subset.get(i).getB());
		//
		// }
		//
		// return map;
	}

	@Override
	protected Map<Object, Double> getWeightedVotes(
			List<Pair<List<Object>, Double>> subset) {

		Map<Object, Double> map = weightedVotesFun(subset, 1);
		Map<Object, Double> result = getUnweightedVotes(subset);
		double sum = 0;
		for (Object o : map.keySet()) {
			sum += map.get(o);
		}
		for (Object o : map.keySet()) {
			result.put(o, result.get(o) * (map.get(o) / sum));

		}
		return result;

	}

	private Map<Object, Double> weightedVotesFun(
			List<Pair<List<Object>, Double>> subset, int func) {
		Map<Object, Double> map = new HashMap<Object, Double>();
		double funcResult = 0;
		for (int i = 0; i < subset.size(); i++) {
			Object classAtt = subset.get(i).getA()
					.get(subset.get(i).getA().size() - 1);
			if (map.containsKey(classAtt)) {

				switch (func) {
				case 1: // inverseDistanceWeighting
					funcResult = 1.0 / Math.pow(subset.get(i).getB(), 2);
					break;
				default: // UnweightedVotes
					funcResult = subset.get(i).getB();
					break;
				}
				map.put(classAtt, map.get(classAtt) + funcResult);
			} else
				map.put(classAtt, subset.get(i).getB());

		}

		return map;
	}

	@Override
	protected Object getWinner(Map<Object, Double> votesFor) {
		Object tempString = null;
		double tmpValue = Double.MAX_VALUE;

		for (Object s : votesFor.keySet()) {
			System.out.println(s + "+++" + votesFor.get(s));
			if (tempString == null) {
				tempString = s;
				tmpValue = votesFor.get(s);
			} else {
				if (tmpValue > votesFor.get(s)) {
					tempString = s;
					tmpValue = votesFor.get(s);
				}
			}
		}
		System.out.println(tempString);
		return tempString;
	}

	@Override
	protected List<Pair<List<Object>, Double>> getNearest(List<Object> testdata) {
		List<Pair<List<Object>, Double>> pairs = new ArrayList<Pair<List<Object>, Double>>();
		// VDM(traindata);
		for (int i = 0; i < traindata.size(); i++)
			pairs.add(new Pair<List<Object>, Double>(traindata.get(i),
					getMetric() == 0 ? determineManhattanDistance(
							traindata.get(i), testdata)
							: determineEuclideanDistance(traindata.get(i),
									testdata)));

		Collections.sort(pairs, new Comparator<Pair<List<Object>, Double>>() {

			@Override
			public int compare(Pair<List<Object>, Double> o1,
					Pair<List<Object>, Double> o2) {
				double a = (o1.getB() - o2.getB());

				return a < 0 ? -1 : a > 0 ? 1 : 0;
			}
		});
		List<Pair<List<Object>, Double>> result = new LinkedList<Pair<List<Object>, Double>>();

		if (getkNearest() < pairs.size())
			for (int i = 0; i < getkNearest(); i++) {
				result.add(pairs.get(i));
			}
		else
			return pairs;

		return result;
	}

	private void VDM(List<List<Object>> traindata2) {
		List<List<Map<Object, Double>>> vdmMatrix = new LinkedList<List<Map<Object, Double>>>();
		for (int i = 0; i < traindata2.get(0).size(); i++) {
			vdmMatrix.add(new LinkedList<Map<Object, Double>>());
		}
		for (List<Object> list : traindata2) {
			for (int i = 0; i < list.size(); i++) {
				if (list.get(i) instanceof String)
					findAddByVDMMatrix(vdmMatrix.get(i), list.get(i));
			}
		}
	}

	private void findAddByVDMMatrix(List<Map<Object, Double>> vdmMatrixAttr,
			Object o) {
		for (int i = 0; i < vdmMatrixAttr.size(); i++) {
			if (vdmMatrixAttr.get(i) != null){
				if (vdmMatrixAttr.get(i).containsKey(o)) 
					vdmMatrixAttr.get(i).
			}				
			else 
		}
	}

	@Override
	protected double determineManhattanDistance(List<Object> instance1,
			List<Object> instance2) {
		double result = 0;

		for (int i = 0; i < instance1.size() - 1; i++) {

			if (instance1.get(i) instanceof Double
					&& instance2.get(i) instanceof Double)
				result += Math.abs((double) instance1.get(i)
						- (double) instance2.get(i));
			else if (instance1.get(i) instanceof String
					&& instance2.get(i) instanceof String) {

				result += ((String) instance2.get(i)).equals((String) instance1
						.get(i)) ? 0 : 1;

			} else
				return 0;
		}

		return result;
	}

	@Override
	protected double determineEuclideanDistance(List<Object> instance1,
			List<Object> instance2) {

		double result = 0;

		for (int i = 0; i < instance1.size(); i++) {

			if (instance1.get(i) instanceof Double
					&& instance2.get(i) instanceof Double) {
				result += Math.pow((double) instance1.get(i)
						- (double) instance2.get(i), 2);

			} else if (instance1.get(i) instanceof String
					&& instance2.get(i) instanceof String) {
				// result += ((String) instance2.get(i)).equals((String)
				// instance1
				// .get(i)) ? 0 : 1;
				result += 0;
			} else
				return 0;

		}
		return Math.sqrt(result);
	}

	@Override
	protected double[][] normalizationScaling() {
		// TODO Auto-generated method stub

		return null;
	}

	@Override
	protected String[] getMatrikelNumbers() {

		return new String[] { "1538225", "1538160", "1515743" };
	}

}
