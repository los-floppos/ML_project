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
import java.util.Vector;

import tud.ke.ml.project.framework.classifier.ANearestNeighbor;
import tud.ke.ml.project.util.Pair;
import weka.classifiers.bayes.net.search.fixed.FromFile;

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
	private List<Object> testdata;

	// private Map<Object, List<Map<Object, Double>>> vdm = new HashMap<Object,
	// List<Map<Object, Double>>>();

	@Override
	protected Object vote(List<Pair<List<Object>, Double>> subset) {
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
			Object classAtt = subset.get(i).getA().get(getClassAttribute());
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
		this.testdata = testdata;
		if (isNormalizing()) {

			double[][] scalTran = normalizationScaling();
			this.scaling = scalTran[1];
			this.translation = scalTran[0];
			this.traindata.add(testdata);
		}

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

	// private double[] calDifVDM_N(Object o1, Object o2, int attrIndex) {
	// double[] n = { 0, 0 };
	// HashMap<Object, Double> w;
	// for (Object o : vdm.keySet()) {
	// w = (HashMap<Object, Double>) this.vdm.get(o).get(attrIndex);
	//
	// n[0] += w.containsKey(o1) ? w.get(o1) : 0;
	// n[1] += w.containsKey(o2) ? w.get(o2) : 0;
	// }
	// return n;
	// }

	// private double vdmFunc(Object o1, Object o2, int attrIndex) {
	// double[] n = calDifVDM_N(o1, o2, attrIndex);
	// double sum = 0;
	// HashMap<Object, Double> w;
	// for (Object o : vdm.keySet()) {
	// w = (HashMap<Object, Double>) this.vdm.get(o).get(attrIndex);
	// sum += Math.pow(
	// Math.abs(((w.containsKey(o1) ? w.get(o1) : 0) / n[0])
	// - ((w.containsKey(o2) ? w.get(o2) : 0) / n[1])),
	// getkNearest());
	// }
	// return sum;
	// }

	// private void VDM() {
	//
	// for (List<Object> list : traindata) {
	// Object o = list.get(list.size() - 1);
	// checkClass(o);
	// List<Map<Object, Double>> vdmMatrix = this.vdm.get(o);
	//
	// for (int i = 0; i < list.size(); i++) {
	// if (list.get(i) instanceof String)
	// findAddByVDMMatrix(vdmMatrix.get(i), list.get(i));
	// }
	// }
	// }

	// private void checkClass(Object o) {
	//
	// if (!this.vdm.containsKey(o)) {
	// List<Map<Object, Double>> vdmMatrix = new LinkedList<Map<Object,
	// Double>>();
	// for (int i = 0; i < traindata.get(0).size(); i++) {
	// vdmMatrix.add(new HashMap<Object, Double>());
	// }
	// this.vdm.put(o, vdmMatrix);
	// }
	//
	// }

	// private void findAddByVDMMatrix(Map<Object, Double> vdmMatrixAttr, Object
	// o) {
	//
	// if (vdmMatrixAttr.containsKey(o))
	// vdmMatrixAttr.put(o, vdmMatrixAttr.get(o) + 1);
	// else
	// vdmMatrixAttr.put(o, 1.0);
	//
	// }

	@Override
	protected double determineManhattanDistance(List<Object> instance1,
			List<Object> instance2) {
		double result = 0;

		for (int i = 0; i < instance1.size() - 1; i++) {

			if (getInstanceof(instance1.get(i), instance2.get(i)) == 0)
				result += Math.abs(normalized((double) instance1.get(i), i)
						- normalized((double) instance2.get(i), i));
			else if (getInstanceof(instance1.get(i), instance2.get(i)) == 1) {

				result += ((String) instance2.get(i)).equals((String) instance1
						.get(i)) ? 0 : 1;

			} else
				return 0;
		}

		return result;
	}

	private double normalized(double d, int i) {
		if (isNormalizing())
			if (this.translation[i] != 0 && this.scaling[i] != 0)
				if (this.translation[i] != 1 && this.scaling[i] != 1)
					if (this.translation[i] == 1 && this.scaling[i] != 0)
						return (d - this.translation[i]) / (this.scaling[i]);

		return d;
	}

	private int getInstanceof(Object o1, Object o2) {
		if (o1 instanceof Double && o2 instanceof Double)
			return 0;
		else if (o1 instanceof String && o2 instanceof String)

			return 1;
		else
			return -1;
	}

	@Override
	protected double determineEuclideanDistance(List<Object> instance1,
			List<Object> instance2) {

		double result = 0;

		for (int i = 0; i < instance1.size(); i++) {
			if (getInstanceof(instance1.get(i), instance2.get(i)) == 0) {
				result += Math.pow(normalized((double) instance1.get(i), i)
						- normalized((double) instance2.get(i), i), 2);

			} else if (getInstanceof(instance1.get(i), instance2.get(i)) == 1) {
				result += ((String) instance2.get(i)).equals((String) instance1
						.get(i)) ? 0 : 1;

			} else
				return 0;

		}
		return Math.sqrt(result);
	}

	@Override
	protected double[][] normalizationScaling() {

		double[][] result = new double[2][traindata.get(0).size()];

		for (int i = 0; i < testdata.size(); i++) {
			if (testdata.get(i) instanceof Double) {
				if (isNormalizing()) {
					result[0][i] = (double) testdata.get(i);
					result[1][i] = (double) testdata.get(i);
				} else {
					result[0][i] = 1;
					result[1][i] = 1;
				}
			}
		}
		for (int i = 0; i < traindata.size(); i++) {
			for (int j = 0; j < traindata.get(i).size(); j++) {

				if (traindata.get(i).get(j) instanceof Double) {
					if (isNormalizing()) {

						result[0][j] = Math.min((double) traindata.get(i)
								.get(j), result[0][j]);
						result[1][j] = Math.max((double) traindata.get(i)
								.get(j), result[1][j]);
					} else {
						result[0][j] = 1;
						result[1][j] = 1;
					}

				} else {
					result[0][j] = 1;
					result[1][j] = 1;
				}
			}
		}
		return calScaling(result);
	}

	private double[][] calScaling(double[][] result) {
		for (int j = 0; j < result[1].length; j++) {

			if (result[0][j] != result[1][j])
				result[1][j] -= result[0][j];
		}

		return result;

	}

	@Override
	protected String[] getMatrikelNumbers() {

		return new String[] { "1538225", "1538160", "1515743" };
	}

}
