/* Copyright 2009-2012 David Hadka
 * 
 * This file is part of the MOEA Framework.
 * 
 * The MOEA Framework is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by 
 * the Free Software Foundation, either version 3 of the License, or (at your 
 * option) any later version.
 * 
 * The MOEA Framework is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public 
 * License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License 
 * along with the MOEA Framework.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.moeaframework.core.comparator;

import java.io.Serializable;

import org.moeaframework.core.Solution;

/**
 * Compares two solutions using the additive &epsilon;-box dominance comparator.
 * This dominance relation divides objective space into boxes with side-length
 * &epsilon; and specifies that only one solution may exist within the same box.
 * If two solutions were to reside in the same box, the solution closer to the
 * box's minimum corner.
 * <p>
 * References:
 * <ol>
 * <li>Laumanns et al. "Combining Convergence and Diversity in Evolutionary
 * Multi-Objective Optimization." Evolutionary Computation. 10(3). 2002
 * <li>Deb et al. "A Fast Multi-Objective Evolutionary Algorithm for Finding
 * Well-Spread Pareto-Optimal Solutions." KanGAL Report No 2003002. Feb 2003.
 * </ol>
 */
public class EpsilonBoxDominanceComparator implements DominanceComparator,
Serializable {

	private static final long serialVersionUID = -5454497496983459905L;

	/**
	 * {@code true} if the the two solutions passed to the previous invocation
	 * of {@code compare} existed within the same &epsilon;-box; {@code false}
	 * otherwise.
	 */
	protected boolean isSameBox;

	/**
	 * The &epsilon; values used by this comparator.
	 */
	protected final double[] epsilons;

	/**
	 * Constructs an additive &epsilon;-box dominance comparator with the 
	 * specified &epsilon; value.
	 * 
	 * @param epsilon the &epsilon; value used by this comparator
	 */
	public EpsilonBoxDominanceComparator(double epsilon) {
		this.epsilons = new double[] { epsilon };
	}

	/**
	 * Constructs an additive &epsilon;-box dominance comparator with the 
	 * specified &epsilon; values.
	 * 
	 * @param epsilons the &epsilon; values used by this comparator
	 */
	public EpsilonBoxDominanceComparator(double[] epsilons) {
		this.epsilons = epsilons.clone();
	}

	/**
	 * Returns {@code true} if the the two solutions passed to the previous
	 * invocation of {@code compare} existed within the same &epsilon;-box;
	 * {@code false} otherwise.
	 * 
	 * @return {@code true} if the the two solutions passed to the previous
	 *         invocation of {@code compare} existed within the same 
	 *         &epsilon;-box; {@code false} otherwise.
	 */
	public boolean isSameBox() {
		return isSameBox;
	}

	/**
	 * Set to {@code true} if the the two solutions passed to the previous
	 * invocation of {@code compare} existed within the same &epsilon;-box;
	 * {@code false} otherwise.
	 * 
	 * @param isSameBox {@code true} if the the two solutions passed to the
	 *        previous invocation of {@code compare} existed within the same
	 *        &epsilon;-box; {@code false} otherwise.
	 */
	protected void setSameBox(boolean isSameBox) {
		this.isSameBox = isSameBox;
	}

	/**
	 * Returns the &epsilon; value used by this comparator for the specified
	 * objective. For cases where {@code (objective >= epsilons.length)}, the
	 * last &epsilon; value in this array is used
	 * {@code (epsilons[epsilons.length-1])}.
	 * 
	 * @return the &epsilon; value used by this comparator for the specified
	 *         objective
	 */
	public double getEpsilon(int objective) {
		return epsilons[objective < epsilons.length ? objective
				: epsilons.length - 1];
	}

	/**
	 * Returns the number of defined &epsilon; values. If {@code getEpsilon} is
	 * invoked with an index larger than the number of defined &epsilon;s, the
	 * value of the last defined &epsilon; is returned.
	 * 
	 * @return the number of defined &epsilon; values
	 */
	public int getNumberOfDefinedEpsilons() {
		return epsilons.length;
	}

	/**
	 * Compares the two solutions using the additive &epsilon;-box dominance
	 * relation.
	 */
	@Override
	public int compare(Solution solution1, Solution solution2) {
		setSameBox(false);

		boolean dominate1 = false;
		boolean dominate2 = false;

		for (int i = 0; i < solution1.getNumberOfObjectives(); i++) {
			double epsilon = getEpsilon(i);

			int index1 = (int)Math.floor(solution1.getObjective(i) / epsilon);
			int index2 = (int)Math.floor(solution2.getObjective(i) / epsilon);

			if (index1 < index2) {
				dominate1 = true;

				if (dominate2) {
					return 0;
				}
			} else if (index1 > index2) {
				dominate2 = true;

				if (dominate1) {
					return 0;
				}
			}
		}

		if (!dominate1 && !dominate2) {
			setSameBox(true);

			double dist1 = 0.0;
			double dist2 = 0.0;

			for (int i = 0; i < solution1.getNumberOfObjectives(); i++) {
				double epsilon = getEpsilon(i);

				int index1 = (int)Math.floor(solution1.getObjective(i)
						/ epsilon);
				int index2 = (int)Math.floor(solution2.getObjective(i)
						/ epsilon);

				dist1 += Math.pow(solution1.getObjective(i) - index1 * epsilon,
						2.0);
				dist2 += Math.pow(solution2.getObjective(i) - index2 * epsilon,
						2.0);
			}

			dist1 = Math.sqrt(dist1);
			dist2 = Math.sqrt(dist2);

			if (dist1 < dist2) {
				return -1;
			} else {
				return 1;
			}
		} else if (dominate1) {
			return -1;
		} else {
			return 1;
		}
	}

}
